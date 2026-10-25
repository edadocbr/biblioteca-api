package com.biblioteca.service.impl;

import com.biblioteca.dto.request.SolicitarReservaRequest;
import com.biblioteca.dto.response.SolicitarReservaResponse;
import com.biblioteca.entity.Ejemplar;
import com.biblioteca.entity.Lector;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Prestamo;
import com.biblioteca.entity.Reserva;
import com.biblioteca.entity.enums.EstadoEjemplar;
import com.biblioteca.entity.enums.EstadoPrestamo;
import com.biblioteca.entity.enums.EstadoReserva;
import com.biblioteca.entity.enums.EstadoUsuario;
import com.biblioteca.repository.BibliotecarioRepository;
import com.biblioteca.repository.EjemplarRepository;
import com.biblioteca.repository.LectorRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaServiceImpl implements ReservaService {

    private static final int LIMITE_LIBROS_POR_LECTOR = 3;

    private final LectorRepository lectorRepository;
    private final BibliotecarioRepository bibliotecarioRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final EjemplarRepository ejemplarRepository;
    private final ReservaRepository reservaRepository;

    public ReservaServiceImpl(LectorRepository lectorRepository,
                              BibliotecarioRepository bibliotecarioRepository,
                              LibroRepository libroRepository,
                              PrestamoRepository prestamoRepository,
                              EjemplarRepository ejemplarRepository,
                              ReservaRepository reservaRepository) {
        this.lectorRepository = lectorRepository;
        this.bibliotecarioRepository = bibliotecarioRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.reservaRepository = reservaRepository;
    }

    @Override
    @Transactional
    public SolicitarReservaResponse solicitarReserva(SolicitarReservaRequest request) {
        if (request == null || request.getIdLector() == null || request.getIdLibro() == null
                || request.getIdLibro().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud de reserva inválida");
        }

        String isbn = request.getIdLibro().trim();
        Long idLector = request.getIdLector();

        if (bibliotecarioRepository.existsById(idLector)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operación exclusiva para lectores");
        }

        Lector lector = lectorRepository.findById(idLector)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operación exclusiva para lectores"));

        if (lector.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operación exclusiva para lectores activos");
        }

        Libro libro = libroRepository.findById(isbn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Libro no encontrado"));

        LocalDate hoy = LocalDate.now();

        // 1. Validar préstamos vencidos (fecha pactada vencida y aún no devuelto)
        List<Prestamo> prestamos = prestamoRepository.findByLectorIdUsuario(idLector);
        boolean tieneVencido = prestamos.stream().anyMatch(p ->
                p.getFechaDevolucionReal() == null
                        && p.getFechaDevolucionProgramada() != null
                        && p.getFechaDevolucionProgramada().toLocalDate().isBefore(hoy));
        if (tieneVencido) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tienes préstamos vencidos pendientes de devolución");
        }

        // 2. Límite de libros: préstamos activos + reservas activas >= 3
        long prestamosActivos = prestamos.stream()
                .filter(p -> p.getFechaDevolucionReal() == null).count();

        List<Reserva> reservasActivas = reservaRepository.findActivasByLector(idLector);
        long reservasActivasCount = reservasActivas.size();

        if (prestamosActivos + reservasActivasCount >= LIMITE_LIBROS_POR_LECTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Has alcanzado el límite máximo de 3 libros permitidos");
        }

        // 3. Ya posee reserva activa para el mismo libro
        if (reservaRepository.existsActivaByLectorAndLibro(idLector, isbn)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ya cuentas con una solicitud en curso para este libro");
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaLimiteRecojo = ahora.plusHours(48);

        // 4. Evaluar ejemplares y stock
        List<Ejemplar> disponibles = ejemplarRepository.findByLibroIsbnAndEstado(isbn, EstadoEjemplar.DISPONIBLE);

        Reserva reserva = new Reserva();
        reserva.setLector(lector);
        reserva.setLibro(libro);
        reserva.setFechaSolicitud(ahora);
        reserva.setFechaExpiracion(fechaLimiteRecojo);

        if (!disponibles.isEmpty()) {
            Ejemplar ejemplar = disponibles.get(0);
            ejemplar.setEstado(EstadoEjemplar.RESERVADO);
            ejemplarRepository.save(ejemplar);
            reserva.setEjemplar(ejemplar);
            reserva.setEstado(EstadoReserva.LISTA_PARA_RECOJO);
        } else {
            reserva.setEjemplar(null);
            reserva.setEstado(EstadoReserva.EN_ESPERA);
        }

        reservaRepository.save(reserva);

        String mensaje = reserva.getEstado() == EstadoReserva.LISTA_PARA_RECOJO
                ? "Reserva lista para recojo en ventanilla"
                : "Ejemplar sin stock. Quedaste en lista de espera";

        return new SolicitarReservaResponse(
                reserva.getEstado().name(),
                mensaje,
                reserva.getEstado() == EstadoReserva.LISTA_PARA_RECOJO ? fechaLimiteRecojo.toLocalDate() : null
        );
    }
}
