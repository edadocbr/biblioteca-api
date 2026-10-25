package com.biblioteca.config;

import com.biblioteca.entity.*;
import com.biblioteca.entity.enums.*;
import com.biblioteca.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    public CommandLineRunner seedDatabase(
            LibroRepository libroRepository,
            EjemplarRepository ejemplarRepository,
            LectorRepository lectorRepository,
            BibliotecarioRepository bibliotecarioRepository,
            PrestamoRepository prestamoRepository,
            ReservaRepository reservaRepository,
            SancionRepository sancionRepository
    ) {
        return args -> {
            if (libroRepository.count() == 0 && ejemplarRepository.count() == 0 && 
                lectorRepository.count() == 0 && bibliotecarioRepository.count() == 0) {
                
                // 1. Crear Bibliotecarios
                Bibliotecario biblio1 = new Bibliotecario("12345678", "María González Pérez", "maria.gonzalez@biblioteca.edu.pe", EstadoUsuario.ACTIVO, "EMP-001", "Mañana");
                Bibliotecario biblio2 = new Bibliotecario("87654321", "Carlos Rodríguez López", "carlos.rodriguez@biblioteca.edu.pe", EstadoUsuario.ACTIVO, "EMP-002", "Tarde");
                bibliotecarioRepository.save(biblio1);
                bibliotecarioRepository.save(biblio2);
                
                // 2. Crear Lectores
                Lector lector1 = new Lector("11111111", "Lucía Fernández", "lucia.fernandez@estudiante.edu.pe", EstadoUsuario.ACTIVO, "L-001");
                lector1.setLimiteReservas(3);
                Lector lector2 = new Lector("22222222", "Mateo Rivas", "mateo.rivas@estudiante.edu.pe", EstadoUsuario.ACTIVO, "L-002");
                lector2.setLimiteReservas(3);
                Lector lector3 = new Lector("33333333", "Valentina Cruz", "valentina.cruz@estudiante.edu.pe", EstadoUsuario.ACTIVO, "L-003");
                lector3.setLimiteReservas(3);
                Lector lector4 = new Lector("44444444", "Santiago Paredes", "santiago.paredes@estudiante.edu.pe", EstadoUsuario.ACTIVO, "L-004");
                lector4.setLimiteReservas(3);
                Lector lector5 = new Lector("55555555", "Camila Ortega", "camila.ortega@estudiante.edu.pe", EstadoUsuario.ACTIVO, "L-005");
                lector5.setLimiteReservas(3);
                
                lectorRepository.save(lector1);
                lectorRepository.save(lector2);
                lectorRepository.save(lector3);
                lectorRepository.save(lector4);
                lectorRepository.save(lector5);
                
                // 3. Crear Libros (basados en mocks del frontend)
                List<Libro> libros = new ArrayList<>();
                
                Libro libro1 = new Libro("978-84-123456-01-1", "Las Formas del Espacio", "Adolf Loos", "Gustavo Gili", 1931);
                Libro libro2 = new Libro("978-84-123456-02-8", "Teoría de la Forma", "Johannes Itten", "Alianza Editorial", 1921);
                Libro libro3 = new Libro("978-84-123456-03-5", "El Mundo como Voluntad y Representación", "Arthur Schopenhauer", "Ediciones Akal", 1819);
                Libro libro4 = new Libro("978-84-123456-04-2", "Hacia una Arquitectura", "Le Corbusier", "Editorial Poseidón", 1923);
                Libro libro5 = new Libro("978-84-123456-05-9", "La Tipografía Moderna", "Jan Tschichold", "Editorial Gustavo Gili", 1928);
                Libro libro6 = new Libro("978-84-123456-06-6", "El Ensayo como Forma", "Theodor W. Adorno", "Ediciones Siruela", 1958);
                Libro libro7 = new Libro("978-84-123456-07-3", "Espacio, Tiempo y Arquitectura", "Sigfried Giedion", "Doubleday", 1941);
                Libro libro8 = new Libro("978-84-123456-08-0", "Notas sobre lo Obvio", "Vilém Flusser", "Siruela", 1993);
                Libro libro9 = new Libro("978-84-123456-09-7", "Grid Systems in Graphic Design", "Josef Müller-Brockmann", "Niggli Verlag", 1981);
                Libro libro10 = new Libro("978-84-123456-10-3", "La Genealogía de la Moral", "Friedrich Nietzsche", "Alianza Editorial", 1887);
                
                libros.add(libro1); libros.add(libro2); libros.add(libro3); libros.add(libro4); libros.add(libro5);
                libros.add(libro6); libros.add(libro7); libros.add(libro8); libros.add(libro9); libros.add(libro10);
                
                for (Libro libro : libros) {
                    libroRepository.save(libro);
                }
                
                // 4. Crear Ejemplares
                Ejemplar ej1 = new Ejemplar("BC-000101", "A1B2C3D4", "EST-01-A", EstadoEjemplar.PRESTADO, libro1);
                Ejemplar ej2 = new Ejemplar("BC-000102", "A1B2C3D5", "EST-01-B", EstadoEjemplar.PRESTADO, libro2);
                Ejemplar ej3 = new Ejemplar("BC-000103", "A1B2C3D6", "EST-01-C", EstadoEjemplar.PRESTADO, libro3);
                Ejemplar ej4 = new Ejemplar("BC-000104", "A1B2C3D7", "EST-01-D", EstadoEjemplar.PRESTADO, libro4);
                Ejemplar ej5 = new Ejemplar("BC-000105", "A1B2C3D8", "EST-02-A", EstadoEjemplar.DISPONIBLE, libro5);
                Ejemplar ej6 = new Ejemplar("BC-000106", "A1B2C3D9", "EST-02-B", EstadoEjemplar.PRESTADO, libro6);
                Ejemplar ej7 = new Ejemplar("BC-000107", "A1B2C3DA", "EST-02-C", EstadoEjemplar.DISPONIBLE, libro7);
                Ejemplar ej8 = new Ejemplar("BC-000108", "A1B2C3DB", "EST-02-D", EstadoEjemplar.DISPONIBLE, libro8);
                Ejemplar ej9 = new Ejemplar("BC-000109", "A1B2C3DC", "EST-03-A", EstadoEjemplar.EN_REPARACION, libro9);
                Ejemplar ej10 = new Ejemplar("BC-000110", "A1B2C3DD", "EST-03-B", EstadoEjemplar.DISPONIBLE, libro10);
                
                ejemplarRepository.save(ej1); ejemplarRepository.save(ej2); ejemplarRepository.save(ej3); ejemplarRepository.save(ej4); ejemplarRepository.save(ej5);
                ejemplarRepository.save(ej6); ejemplarRepository.save(ej7); ejemplarRepository.save(ej8); ejemplarRepository.save(ej9); ejemplarRepository.save(ej10);
                
                // 5. Crear Prestamos
                LocalDateTime now = LocalDateTime.now();
                // Préstamos vencidos y activos
                Prestamo p1 = new Prestamo();
                p1.setLector(lector2); p1.setEjemplar(ej4); p1.setBibliotecario(biblio1);
                p1.setFechaInicio(now.minusDays(24)); p1.setFechaDevolucionProgramada(now.minusDays(9));
                p1.setContadorRenovaciones(0); p1.setEstado(EstadoPrestamo.VENCIDO); p1.setFechaDevolucionReal(null);
                prestamoRepository.save(p1);
                
                Prestamo p2 = new Prestamo();
                p2.setLector(lector1); p2.setEjemplar(ej2); p2.setBibliotecario(biblio1);
                p2.setFechaInicio(now.minusDays(18)); p2.setFechaDevolucionProgramada(now.minusDays(3));
                p2.setContadorRenovaciones(1); p2.setEstado(EstadoPrestamo.VENCIDO); p2.setFechaDevolucionReal(null);
                prestamoRepository.save(p2);
                
                Prestamo p3 = new Prestamo();
                p3.setLector(lector2); p3.setEjemplar(ej6); p3.setBibliotecario(biblio1);
                p3.setFechaInicio(now.minusDays(10)); p3.setFechaDevolucionProgramada(now.plusDays(5));
                p3.setContadorRenovaciones(0); p3.setEstado(EstadoPrestamo.ACTIVO); p3.setFechaDevolucionReal(null);
                prestamoRepository.save(p3);
                
                Prestamo p4 = new Prestamo();
                p4.setLector(lector4); p4.setEjemplar(ej1); p4.setBibliotecario(biblio1);
                p4.setFechaInicio(now.minusDays(4)); p4.setFechaDevolucionProgramada(now.plusDays(11));
                p4.setContadorRenovaciones(0); p4.setEstado(EstadoPrestamo.ACTIVO); p4.setFechaDevolucionReal(null);
                prestamoRepository.save(p4);
                
                Prestamo p5 = new Prestamo();
                p5.setLector(lector5); p5.setEjemplar(ej3); p5.setBibliotecario(biblio1);
                p5.setFechaInicio(now.minusDays(1)); p5.setFechaDevolucionProgramada(now.plusDays(14));
                p5.setContadorRenovaciones(0); p5.setEstado(EstadoPrestamo.ACTIVO); p5.setFechaDevolucionReal(null);
                prestamoRepository.save(p5);
                
                Prestamo p6 = new Prestamo();
                p6.setLector(lector2); p6.setEjemplar(ej7); p6.setBibliotecario(biblio2);
                p6.setFechaInicio(now.minusDays(30)); p6.setFechaDevolucionProgramada(now.minusDays(15));
                p6.setContadorRenovaciones(0); p6.setEstado(EstadoPrestamo.DEVUELTO); p6.setFechaDevolucionReal(now.minusDays(16));
                prestamoRepository.save(p6);
                
                // 6. Crear Reservas pendientes
                Reserva r1 = new Reserva();
                r1.setLector(lector2); r1.setEjemplar(ej6); r1.setFechaSolicitud(now.minusHours(2));
                r1.setFechaExpiracion(now.plusHours(46)); r1.setEstado(EstadoReserva.PENDIENTE);
                reservaRepository.save(r1);
                
                Reserva r2 = new Reserva();
                r2.setLector(lector5); r2.setEjemplar(ej3); r2.setFechaSolicitud(now.minusHours(1));
                r2.setFechaExpiracion(now.plusHours(47)); r2.setEstado(EstadoReserva.PENDIENTE);
                reservaRepository.save(r2);
                
                // 7. Crear Sanción
                Sancion sancion1 = new Sancion();
                sancion1.setPrestamo(p2); sancion1.setLector(lector1);
                sancion1.setFechaEmision(now); sancion1.setDiasSuspension(0);
                sancion1.setMontoMulta(new BigDecimal("15.00")); sancion1.setPagado(false);
                sancionRepository.save(sancion1);
            }
        };
    }
}
