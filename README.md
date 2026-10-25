# biblioteca-api

Tarea para la UNMSM de demostración aplicado al curso de **Diseño de Sistemas de Información (DSI)**.

## De qué trata

`biblioteca-api` es un **Sistema de Gestión de Biblioteca Universitaria (SGBU)** cliente-servidor: una SPA en **Vue 3** consume una API REST en **Spring Boot** que persiste en **MySQL**. El eje del proyecto es el **cumplimiento riguroso de las reglas de negocio** modeladas en los Casos de Uso **CUS-01 a CUS-05**: reservas con cola de espera FIFO, formalización de préstamos en ventanilla, devoluciones con reasignación automática, renovaciones controladas y administración de catálogo, ejemplares y usuarios.

El proyecto aborda, de manera concreta, los principios propios del DSI:
- **Separación de responsabilidades** por capas (presentación, controlador, servicio, repositorio, dominio).
- **Modelado de datos** con entidades persistentes (`Libro`, `Ejemplar`, `Prestamo`, `Reserva`, `Sancion`, `Usuario` → `Lector` / `Bibliotecario`).
- **Máquinas de estado explícitas** para ejemplares, reservas y préstamos, gobernadas únicamente desde la capa de servicio.
- **Respuestas HTTP explícitas** (`200 OK`, `400 Bad Request` con cuerpo JSON `{ "message": ... }`).

### Alcance Técnico

- **Reglas de negocio en `@Service` transaccionales:** cada operación (reservar, prestar, devolver, renovar) se ejecuta dentro de una transacción, de modo que el cambio de estado de ejemplar, reserva y préstamo es atómico.
- **Máquinas de estado con enums:** `EstadoEjemplar` (`DISPONIBLE`, `RESERVADO`, `PRESTADO`, `EN_REPARACION`, `PERDIDO`), `EstadoReserva` (`EN_ESPERA`, `LISTA_PARA_RECOJO`, `COMPLETADA`, `CANCELADA`, `EXPIRADA`, `PENDIENTE`) y `EstadoPrestamo` (`ACTIVO`, `RENOVADO`, `VENCIDO`, `DEVUELTO`). El estado `PRESTADO` se muestra como `EN_PRESTAMO` en el panel.
- **Manejo global de errores (`GlobalExceptionHandler`):** toda excepción de negocio (`ResponseStatusException`) se traduce a un JSON con un campo `message` legible, que el frontend muestra al usuario sin volcados crudos.
- **Herencia de usuarios:** `Usuario` es la base de `Lector` y `Bibliotecario`, lo que permite un padrón diferenciado y el aislamiento de roles en el backend.
- **Autenticación multi-guard en el cliente:** el Dashboard y el Catálogo mantienen sesiones independientes en `localStorage` (`sgbu_admin_user` y `sgbu_lector_user`), gestionadas por los composables `useAdminAuth` y `useLectorAuth`. Cerrar sesión en uno no afecta al otro.
- **Seeder programático (`DatabaseSeeder`):** al arrancar siembra un administrador, un catálogo de 10 títulos y sus ejemplares.
- **Build integrado:** `frontend-maven-plugin` compila Vue durante el build de Maven y copia `frontend/dist` a `static/`, de modo que un único JAR sirve API y cliente.

## Casos de Uso y su solución en la arquitectura

### CUS-01: Búsqueda, Catálogo y Solicitud de Reserva

**Actor:** Lector (exclusivo; bloqueado para bibliotecarios y administradores).
**Implementación:** `ReservaServiceImpl.solicitarReserva` · `POST /api/reservas/solicitar`

| Aspecto | Solución |
|---|---|
| Aislamiento de roles | El servicio rechaza con `400` ("Operación exclusiva para lectores") a quien no sea un `Lector` activo. En `Home.vue` el botón se reemplaza por la etiqueta **Modo Administrador** y, sin sesión de lector, se redirige a `/login?redirect=/`. |
| Con stock disponible | Toma el primer ejemplar `DISPONIBLE` → pasa a `RESERVADO`; la reserva nace `LISTA_PARA_RECOJO` con expiración a **48 horas**. |
| Sin stock disponible | La reserva se crea con `ejemplar = null` y estado `EN_ESPERA` (cola FIFO por `fechaSolicitud`). |
| Préstamos vencidos | Bloquea la solicitud si el lector tiene préstamos con fecha pactada vencida sin devolver. |
| Tope de 3 libros | Suma préstamos activos + reservas en curso; con 3 o más rechaza la solicitud. |
| Duplicados | Rechaza si el lector ya tiene una reserva activa sobre el mismo título. |

### CUS-02: Formalización de Salidas y Préstamos en Ventanilla

**Actor:** Bibliotecario / Operador de ventanilla.
**Implementación:** `PrestamoServiceImpl.registrarPrestamo` · `POST /api/admin/prestamos`

| Estado del ejemplar | Resultado |
|---|---|
| `DISPONIBLE` | Se formaliza el préstamo directo. |
| `RESERVADO` | Se valida que la reserva (`LISTA_PARA_RECOJO`) pertenezca al lector que acude a ventanilla. Si coincide: la reserva pasa a `COMPLETADA`, el ejemplar a `PRESTADO` (`EN_PRESTAMO`) y se emite el préstamo `ACTIVO`. Si pertenece a otro lector: `400` ("El ejemplar está reservado para otro lector"). |
| `PRESTADO`, `EN_REPARACION`, `PERDIDO` (baja) | Se rechaza con `400`. |

### CUS-03: Devoluciones y Reasignación Automática a Cola de Espera

**Actor:** Bibliotecario.
**Implementación:** `DevolucionServiceImpl.registrarDevolucion` · `POST /api/admin/devoluciones`

1. **Cierre de ciclo:** el préstamo pasa a `DEVUELTO` con `fechaDevolucionReal`. Si hay retraso respecto a la fecha pactada, se calculan los días y se genera una `Sancion` (suspensión y multa).
2. **Interconexión con CUS-01:** se buscan reservas `EN_ESPERA` del mismo libro ordenadas por `fechaSolicitud ASC`.
   - **Hay cola:** la más antigua recibe el ejemplar devuelto, pasa a `LISTA_PARA_RECOJO` (48 h) y el ejemplar se mantiene `RESERVADO`.
   - **No hay cola:** el ejemplar vuelve a `DISPONIBLE` (o a `PERDIDO` si se reporta dañado).

### CUS-04: Renovación de Préstamos

**Actor:** Lector / Bibliotecario.
**Implementación:** `RenovacionServiceImpl.solicitarRenovacion` · `POST /api/admin/renovaciones`

La prórroga solo se concede si se cumplen **todas** estas condiciones; de lo contrario responde `400` con el motivo:

- El préstamo está vigente (`ACTIVO` o `RENOVADO`) y no devuelto.
- No está vencido.
- No superó el tope de **2 renovaciones**.
- El libro **no tiene lectores `EN_ESPERA`** (regla de conflicto con CUS-01).

### CUS-05: Administración de Catálogo, Ejemplares y Usuarios

**Actor:** Bibliotecario / Administrador.
**Implementación:** `EjemplarServiceImpl`, `LibroServiceImpl`, `AuthServiceImpl` · `/api/admin/ejemplares`, `/api/libros`, `/api/auth`

- **Títulos:** alta y consulta de libros por ISBN.
- **Ejemplares físicos:** control unitario por `codigoBarra`, `tagRfid` y `ubicacionTopografica`, con cambio de estado (`PATCH /api/admin/ejemplares/{codigoBarra}/estado`) y adquisición de nuevo material.
- **Padrón:** `Lector` (código de estudiante, límite de reservas) y `Bibliotecario` (código de empleado) se modelan como especializaciones de `Usuario`; el registro de lectores se hace en `POST /api/auth/register`.

## Estructura del Proyecto

```
src/main/java/com/biblioteca
├── config/
│   ├── CorsConfig.java
│   └── DatabaseSeeder.java          # Seeder (admin, libros, ejemplares)
├── controller/                      # Auth, Catalogo, Libro, Ejemplar, Reserva,
│                                    # Prestamo, Devolucion, Renovacion
├── dto/
│   ├── request/                     # Contratos de entrada
│   └── response/                    # Contratos de salida
├── entity/
│   ├── enums/                       # EstadoEjemplar, EstadoReserva, EstadoPrestamo, EstadoUsuario
│   └── ...                          # Usuario, Lector, Bibliotecario, Libro, Ejemplar,
│                                    # Prestamo, Reserva, Sancion
├── exception/
│   └── GlobalExceptionHandler.java  # Errores de negocio → JSON { message }
├── repository/                      # JpaRepository por entidad
├── service/
│   ├── ...                          # Interfaces
│   └── impl/                        # Reglas de negocio CUS-01 a CUS-05
└── BibliotecaApiApplication.java    # Punto de entrada

frontend/src
├── composables/
│   └── useAuth.js                   # useAdminAuth / useLectorAuth (multi-guard)
├── layouts/AdminLayout.vue          # Shell del Dashboard
├── router/index.js                  # Guards de rutas
└── views/
    ├── Home.vue                     # Catálogo y reservas (Lector)
    ├── Login.vue / Register.vue
    └── admin/                       # Inventario, Préstamos, Devoluciones, Renovaciones
```

## Stack

| Componente | Tecnología |
|---|---|
| Lenguaje (backend) | Java 25 |
| Framework | Spring Boot (Spring Data JPA, Validation) |
| Build | Maven |
| Base de datos | MySQL |
| Frontend | Vue 3 + Vue Router + Vite |
| Estilos | Tailwind CSS |

## Clonar el repositorio

```bash
git clone https://github.com/edadocbr/biblioteca-api
cd biblioteca-api
```

## Ejecutar en local

### 1. Base de datos

Crear la base en MySQL (las tablas las genera Hibernate al arrancar):

```sql
CREATE DATABASE sgbu_db;
```

Las credenciales se configuran en `src/main/resources/application.properties` (`spring.datasource.username` / `spring.datasource.password`).

### 2. Backend

Desde la raíz del proyecto (`biblioteca-api/`):

```bash
./mvnw spring-boot:run
```

En Windows:

```bat
mvnw.cmd spring-boot:run
```

El build de Maven también compila el frontend y lo sirve desde `http://localhost:8080/`.

### 3. Frontend en modo desarrollo (opcional)

```bash
cd frontend
npm install
npm run dev
```

Disponible en `http://localhost:5173`.

Al iniciar, el seeder persiste el administrador por defecto:

| Email | Password | Rol |
|---|---|---|
| admin@sgbu.com | admin123 | BIBLIOTECARIO |

Los lectores se crean desde `/register`.

## Endpoints y Accesos Clave

| Recurso | Método | Descripción | CUS |
|---|---|---|---|
| `/` | GET | Catálogo público (Lector) | 01 |
| `/dashboard/login` | GET | Acceso al Dashboard (Bibliotecario) | 05 |
| `/api/catalogo/libros` | GET | Catálogo con stock y estado de solicitud | 01 |
| `/api/reservas/solicitar` | POST | Solicitar préstamo / reserva | 01 |
| `/api/admin/prestamos` | GET / POST | Listar y formalizar préstamos | 02 |
| `/api/admin/devoluciones` | GET / POST | Listar y registrar devoluciones | 03 |
| `/api/admin/renovaciones` | GET / POST | Listar y solicitar renovaciones | 04 |
| `/api/admin/ejemplares` | GET | Inventario de ejemplares | 05 |
| `/api/admin/ejemplares/adquirir` | POST | Alta de material | 05 |
| `/api/admin/ejemplares/{codigoBarra}/estado` | PATCH | Cambio de estado de un ejemplar | 05 |
| `/api/auth/register` | POST | Registro de lector | 05 |
| `/api/auth/login` | POST | Inicio de sesión | 05 |

### Al solicitar una reserva

**Con stock (200 OK):**

```json
{
  "estado": "LISTA_PARA_RECOJO",
  "mensaje": "Reserva lista para recojo en ventanilla",
  "fechaLimiteRecojo": "2026-10-27"
}
```

**Sin stock (200 OK):**

```json
{
  "estado": "EN_ESPERA",
  "mensaje": "Ejemplar sin stock. Quedaste en lista de espera",
  "fechaLimiteRecojo": null
}
```

**Regla de negocio incumplida (400 Bad Request):**

```json
{
  "timestamp": "2026-10-25T10:15:30.123",
  "status": 400,
  "message": "Has alcanzado el límite máximo de 3 libros permitidos"
}
```

> **Nota:** `application.properties` define `spring.jpa.hibernate.ddl-auto=create`, por lo que el esquema se recrea y el seeder vuelve a poblar los datos en cada arranque. Cambiar a `update` para conservar la información entre ejecuciones.
