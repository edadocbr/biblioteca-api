package com.biblioteca.service.impl;

import com.biblioteca.dto.request.RegistrarRenovacionRequest;
import com.biblioteca.dto.response.RenovacionItemResponse;
import com.biblioteca.dto.response.RenovacionKpisResponse;
import com.biblioteca.entity.Ejemplar;
import com.biblioteca.entity.Lector;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Prestamo;
import com.biblioteca.entity.Reserva;
import com.biblioteca.entity.enums.EstadoPrestamo;
import com.biblioteca.entity.enums.EstadoReserva;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.service.RenovacionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class RenovacionServiceImpl implements RenovacionService {

    private final PrestamoRepository prestamoRepository;
    private final ReservaRepository reservaRepository;

    public RenovacionServiceImpl(PrestamoRepository prestamoRepository, ReservaRepository reservaRepository) {
        this.prestamoRepository = prestamoRepository;
        this.reservaRepository = reservaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RenovacionKpisResponse obtenerKpis() {
        List<Prestamo> activos = prestamoRepository.findByFechaDevolucionRealIsNull();
        LocalDate hoy = LocalDate.now();

        long renovacionesHoy = activos.stream()
                .filter(p -> p.getContadorRenovaciones() != null && p.getContadorRenovaciones() > 0)
                .count();

        long prestamosPorVencer = activos.stream()
                .filter(p -> p.getFechaDevolucionProgramada() != null)
                .filter(p -> {
                    LocalDate pactada = p.getFechaDevolucionProgramada().toLocalDate();
                    return !pactada.isBefore(hoy) && !pactada.isAfter(hoy.plusDays(3));
                })
                .count();

        long prestamosRenovados = activos.stream()
                .filter(p -> p.getContadorRenovaciones() != null && p.getContadorRenovaciones() > 0)
                .count();

        return new RenovacionKpisResponse(renovacionesHoy, prestamosPorVencer, prestamosRenovados);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RenovacionItemResponse> listarPrestamosParaRenovacion(String busqueda) {
        List<Prestamo> activos = prestamoRepository.findByFechaDevolucionRealIsNull();
        String q = busqueda == null ? null : busqueda.trim().toLowerCase(Locale.ROOT);

        List<RenovacionItemResponse> lista = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (Prestamo p : activos) {
            Ejemplar ej = p.getEjemplar();
            Libro libro = ej != null ? ej.getLibro() : null;
            Lector lector = p.getLector();

            String idStr = String.valueOf(p.getIdPrestamo());
            String preIdStr = "PRE-" + p.getIdPrestamo();
            String codBarra = ej != null ? ej.getCodigoBarra() : "";
            String titulo = libro != null ? libro.getTitulo() : "";
            String nombreLector = lector != null ? lector.getNombreCompleto() : "";
            String codEstudiante = lector != null ? lector.getCodigoEstudiante() : "";

            if (q != null && !q.isEmpty()) {
                boolean coincide = idStr.toLowerCase(Locale.ROOT).contains(q)
                        || preIdStr.toLowerCase(Locale.ROOT).contains(q)
                        || codBarra.toLowerCase(Locale.ROOT).contains(q)
                        || titulo.toLowerCase(Locale.ROOT).contains(q)
                        || nombreLector.toLowerCase(Locale.ROOT).contains(q)
                        || codEstudiante.toLowerCase(Locale.ROOT).contains(q);

                if (!coincide) {
                    continue;
                }
            }

            LocalDate fechaPactada = p.getFechaDevolucionProgramada() != null ? p.getFechaDevolucionProgramada().toLocalDate() : null;
            boolean estaVencido = fechaPactada == null || hoy.isAfter(fechaPactada);

            boolean tieneReservaTerceros = false;
            if (ej != null && ej.getCodigoBarra() != null) {
                List<Reserva> reservas = reservaRepository.findByEjemplarCodigoBarraAndEstado(ej.getCodigoBarra(), EstadoReserva.PENDIENTE);
                tieneReservaTerceros = reservas.stream().anyMatch(r -> r.getLector() != null
                        && (lector == null || !r.getLector().getIdUsuario().equals(lector.getIdUsuario())));
            }

            int renovaciones = p.getContadorRenovaciones() != null ? p.getContadorRenovaciones() : 0;
            boolean esRenovable = !estaVencido && renovaciones < 2 && !tieneReservaTerceros;

            lista.add(new RenovacionItemResponse(
                    p.getIdPrestamo(),
                    codBarra,
                    titulo,
                    nombreLector,
                    codEstudiante,
                    p.getFechaInicio() != null ? p.getFechaInicio().toLocalDate() : null,
                    fechaPactada,
                    renovaciones,
                    esRenovable,
                    tieneReservaTerceros
            ));
        }

        return lista;
    }

    @Override
    @Transactional
    public RenovacionItemResponse solicitarRenovacion(RegistrarRenovacionRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud de renovación es requerida");
        }

        Prestamo prestamo = null;

        // 1. Buscar por ID de préstamo si está presente
        if (request.getIdPrestamo() != null) {
            Optional<Prestamo> opt = prestamoRepository.findById(request.getIdPrestamo());
            if (opt.isPresent() && opt.get().getFechaDevolucionReal() == null) {
                prestamo = opt.get();
            }
        }

        // 2. Buscar por código de barra / término si no se encontró por ID
        if (prestamo == null && request.getCodigoBarra() != null && !request.getCodigoBarra().trim().isEmpty()) {
            String cod = request.getCodigoBarra().trim();
            List<Prestamo> candidatos = prestamoRepository.findActivosByCodigoEjemplar(cod);
            if (!candidatos.isEmpty()) {
                prestamo = candidatos.get(0);
            } else {
                // Intentar parsear "PRE-1042" o "1042"
                String cleanId = cod.toUpperCase(Locale.ROOT).replace("PRE-", "").trim();
                try {
                    Long id = Long.parseLong(cleanId);
                    Optional<Prestamo> opt = prestamoRepository.findById(id);
                    if (opt.isPresent() && opt.get().getFechaDevolucionReal() == null) {
                        prestamo = opt.get();
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (prestamo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se encontró un préstamo activo asociado");
        }

        // 2. Validar fecha vencida
        LocalDate hoy = LocalDate.now();
        LocalDate fechaPactada = prestamo.getFechaDevolucionProgramada().toLocalDate();
        if (hoy.isAfter(fechaPactada) || prestamo.getEstado() == EstadoPrestamo.VENCIDO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede renovar un préstamo que ya se encuentra vencido");
        }

        // 3. Validar límite de renovaciones
        int renovacionesActuales = prestamo.getContadorRenovaciones() != null ? prestamo.getContadorRenovaciones() : 0;
        if (renovacionesActuales >= 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El préstamo ha alcanzado el límite máximo de renovaciones permitidas");
        }

        // 4. Validar reservas pendientes de terceros
        Prestamo prestamoFinal = prestamo;
        Ejemplar ej = prestamoFinal.getEjemplar();
        boolean tieneReservaTerceros = false;
        if (ej != null && ej.getCodigoBarra() != null) {
            List<Reserva> reservas = reservaRepository.findByEjemplarCodigoBarraAndEstado(ej.getCodigoBarra(), EstadoReserva.PENDIENTE);
            tieneReservaTerceros = reservas.stream().anyMatch(r -> r.getLector() != null
                    && (prestamoFinal.getLector() == null || !r.getLector().getIdUsuario().equals(prestamoFinal.getLector().getIdUsuario())));
        }

        if (tieneReservaTerceros) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El material no se puede renovar porque tiene reservas pendientes en espera");
        }

        // 5. Extender plazo
        int dias = request.getDiasExtension();
        prestamo.setFechaDevolucionProgramada(prestamo.getFechaDevolucionProgramada().plusDays(dias));

        // 6. Incrementar contador de renovaciones
        prestamo.setContadorRenovaciones(renovacionesActuales + 1);
        prestamo.setEstado(EstadoPrestamo.RENOVADO);
        prestamoRepository.save(prestamo);

        Libro libro = ej != null ? ej.getLibro() : null;
        Lector lector = prestamo.getLector();

        boolean esRenovableNuevo = (renovacionesActuales + 1) < 2;

        return new RenovacionItemResponse(
                prestamo.getIdPrestamo(),
                ej != null ? ej.getCodigoBarra() : null,
                libro != null ? libro.getTitulo() : null,
                lector != null ? lector.getNombreCompleto() : null,
                lector != null ? lector.getCodigoEstudiante() : null,
                prestamo.getFechaInicio() != null ? prestamo.getFechaInicio().toLocalDate() : null,
                prestamo.getFechaDevolucionProgramada().toLocalDate(),
                prestamo.getContadorRenovaciones(),
                esRenovableNuevo,
                false
        );
    }
}
