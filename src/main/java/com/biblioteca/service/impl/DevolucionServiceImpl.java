package com.biblioteca.service.impl;

import com.biblioteca.dto.request.RegistrarDevolucionRequest;
import com.biblioteca.dto.response.DevolucionItemResponse;
import com.biblioteca.dto.response.DevolucionKpisResponse;
import com.biblioteca.entity.Ejemplar;
import com.biblioteca.entity.Lector;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Prestamo;
import com.biblioteca.entity.Reserva;
import com.biblioteca.entity.Sancion;
import com.biblioteca.entity.enums.EstadoEjemplar;
import com.biblioteca.entity.enums.EstadoPrestamo;
import com.biblioteca.entity.enums.EstadoReserva;
import com.biblioteca.repository.BibliotecarioRepository;
import com.biblioteca.repository.EjemplarRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.repository.SancionRepository;
import com.biblioteca.service.DevolucionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class DevolucionServiceImpl implements DevolucionService {

    private final PrestamoRepository prestamoRepository;
    private final EjemplarRepository ejemplarRepository;
    private final BibliotecarioRepository bibliotecarioRepository;
    private final SancionRepository sancionRepository;
    private final ReservaRepository reservaRepository;

    public DevolucionServiceImpl(PrestamoRepository prestamoRepository,
                                 EjemplarRepository ejemplarRepository,
                                 BibliotecarioRepository bibliotecarioRepository,
                                 SancionRepository sancionRepository,
                                 ReservaRepository reservaRepository) {
        this.prestamoRepository = prestamoRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.bibliotecarioRepository = bibliotecarioRepository;
        this.sancionRepository = sancionRepository;
        this.reservaRepository = reservaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DevolucionKpisResponse obtenerKpis() {
        List<Prestamo> devueltos = prestamoRepository.findByFechaDevolucionRealIsNotNullOrderByFechaDevolucionRealDesc();
        LocalDate hoy = LocalDate.now();

        long devolucionesHoy = devueltos.stream()
                .filter(p -> p.getFechaDevolucionReal() != null && p.getFechaDevolucionReal().toLocalDate().isEqual(hoy))
                .count();

        long devolucionesEnFecha = devueltos.stream()
                .filter(p -> p.getFechaDevolucionReal() != null && p.getFechaDevolucionProgramada() != null
                        && !p.getFechaDevolucionReal().toLocalDate().isAfter(p.getFechaDevolucionProgramada().toLocalDate()))
                .count();

        long devolucionesConRetraso = devueltos.stream()
                .filter(p -> p.getFechaDevolucionReal() != null && p.getFechaDevolucionProgramada() != null
                        && p.getFechaDevolucionReal().toLocalDate().isAfter(p.getFechaDevolucionProgramada().toLocalDate()))
                .count();

        return new DevolucionKpisResponse(devolucionesHoy, devolucionesEnFecha, devolucionesConRetraso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DevolucionItemResponse> listarHistorialDevoluciones(String busqueda) {
        List<Prestamo> devueltos = prestamoRepository.findByFechaDevolucionRealIsNotNullOrderByFechaDevolucionRealDesc();
        String q = busqueda == null ? null : busqueda.trim().toLowerCase(Locale.ROOT);

        List<DevolucionItemResponse> list = new ArrayList<>();
        for (Prestamo p : devueltos) {
            Ejemplar ej = p.getEjemplar();
            Libro libro = ej != null ? ej.getLibro() : null;
            Lector lector = p.getLector();

            if (q != null && !q.isEmpty()) {
                String cod = ej != null && ej.getCodigoBarra() != null ? ej.getCodigoBarra().toLowerCase(Locale.ROOT) : "";
                String tit = libro != null && libro.getTitulo() != null ? libro.getTitulo().toLowerCase(Locale.ROOT) : "";
                String nom = lector != null && lector.getNombreCompleto() != null ? lector.getNombreCompleto().toLowerCase(Locale.ROOT) : "";
                String est = lector != null && lector.getCodigoEstudiante() != null ? lector.getCodigoEstudiante().toLowerCase(Locale.ROOT) : "";
                String idStr = String.valueOf(p.getIdPrestamo());
                if (!cod.contains(q) && !tit.contains(q) && !nom.contains(q) && !est.contains(q) && !idStr.contains(q)) {
                    continue;
                }
            }

            LocalDate pactada = p.getFechaDevolucionProgramada() != null ? p.getFechaDevolucionProgramada().toLocalDate() : null;
            LocalDate real = p.getFechaDevolucionReal() != null ? p.getFechaDevolucionReal().toLocalDate() : null;
            int diasRetraso = 0;
            if (real != null && pactada != null && real.isAfter(pactada)) {
                diasRetraso = (int) ChronoUnit.DAYS.between(pactada, real);
            }
            String estadoResp = diasRetraso > 0 ? "CON_RETRASO" : "A_TIEMPO";

            list.add(new DevolucionItemResponse(
                    p.getIdPrestamo(),
                    ej != null ? ej.getCodigoBarra() : null,
                    libro != null ? libro.getTitulo() : null,
                    lector != null ? lector.getNombreCompleto() : null,
                    lector != null ? lector.getCodigoEstudiante() : null,
                    p.getFechaInicio() != null ? p.getFechaInicio().toLocalDate() : null,
                    pactada,
                    real,
                    estadoResp,
                    diasRetraso
            ));
        }
        return list;
    }

    @Override
    @Transactional
    public DevolucionItemResponse registrarDevolucion(RegistrarDevolucionRequest request) {
        if (request == null || request.getCodigoBarra() == null || request.getCodigoBarra().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código de barras del ejemplar es obligatorio");
        }

        String cod = request.getCodigoBarra().trim();
        List<Prestamo> activos = prestamoRepository.findActivosByCodigoEjemplar(cod);
        if (activos.isEmpty()) {
            String cleanId = cod.toUpperCase(Locale.ROOT).replace("PRE-", "").trim();
            try {
                Long id = Long.parseLong(cleanId);
                Optional<Prestamo> pOpt = prestamoRepository.findById(id);
                if (pOpt.isPresent() && pOpt.get().getFechaDevolucionReal() == null) {
                    activos = List.of(pOpt.get());
                }
            } catch (NumberFormatException ignored) {}
        }

        if (activos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se encontró ningún préstamo activo para el ejemplar indicado");
        }

        Prestamo prestamo = activos.get(0);
        LocalDateTime now = LocalDateTime.now();
        prestamo.setFechaDevolucionReal(now);

        LocalDate pactada = prestamo.getFechaDevolucionProgramada().toLocalDate();
        LocalDate real = now.toLocalDate();
        int diasRetraso = 0;
        if (real.isAfter(pactada)) {
            diasRetraso = (int) ChronoUnit.DAYS.between(pactada, real);
        }

        if (diasRetraso > 0) {
            Sancion sancion = new Sancion();
            sancion.setPrestamo(prestamo);
            sancion.setLector(prestamo.getLector());
            sancion.setFechaEmision(now);
            sancion.setDiasSuspension(diasRetraso);
            sancion.setMontoMulta(new BigDecimal("2.50").multiply(BigDecimal.valueOf(diasRetraso)));
            sancion.setPagado(false);
            sancionRepository.save(sancion);
        }

        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        if (request.getIdBibliotecario() != null) {
            bibliotecarioRepository.findById(request.getIdBibliotecario()).ifPresent(prestamo::setBibliotecario);
        }
        prestamoRepository.save(prestamo);

        Ejemplar ejemplar = prestamo.getEjemplar();
        if (ejemplar != null) {
            String isbnLibro = ejemplar.getLibro() != null ? ejemplar.getLibro().getIsbn() : null;

            // Antes de liberar el ejemplar, verificar si hay reservas en espera para este libro
            boolean asignadoAReservaEspera = false;
            if (isbnLibro != null) {
                List<Reserva> reservasEspera = reservaRepository
                        .findEsperaByLibroIsbnOrderByFechaSolicitudAsc(isbnLibro);
                if (!reservasEspera.isEmpty()) {
                    Reserva reservaEspera = reservasEspera.get(0);
                    LocalDateTime fechaLimiteRecojo = LocalDateTime.now().plusHours(48);
                    reservaEspera.setEjemplar(ejemplar);
                    reservaEspera.setEstado(EstadoReserva.LISTA_PARA_RECOJO);
                    reservaEspera.setFechaExpiracion(fechaLimiteRecojo);
                    reservaRepository.save(reservaEspera);
                    ejemplar.setEstado(EstadoEjemplar.RESERVADO);
                    asignadoAReservaEspera = true;
                }
            }

            if (!asignadoAReservaEspera) {
                String conservacion = request.getEstadoConservacion() != null ? request.getEstadoConservacion().trim().toUpperCase(Locale.ROOT) : "BUENO";
                if ("DANADO".equals(conservacion) || "DAÑADO".equals(conservacion) || "BAJA".equals(conservacion)) {
                    ejemplar.setEstado(EstadoEjemplar.PERDIDO);
                } else {
                    ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
                }
            }
            ejemplarRepository.save(ejemplar);
        }

        String estadoResp = diasRetraso > 0 ? "CON_RETRASO" : "A_TIEMPO";
        return new DevolucionItemResponse(
                prestamo.getIdPrestamo(),
                ejemplar != null ? ejemplar.getCodigoBarra() : null,
                ejemplar != null && ejemplar.getLibro() != null ? ejemplar.getLibro().getTitulo() : null,
                prestamo.getLector() != null ? prestamo.getLector().getNombreCompleto() : null,
                prestamo.getLector() != null ? prestamo.getLector().getCodigoEstudiante() : null,
                prestamo.getFechaInicio() != null ? prestamo.getFechaInicio().toLocalDate() : null,
                pactada,
                real,
                estadoResp,
                diasRetraso
        );
    }
}
