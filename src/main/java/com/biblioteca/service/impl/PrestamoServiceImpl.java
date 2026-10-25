package com.biblioteca.service.impl;

import com.biblioteca.dto.request.RegistrarPrestamoRequest;
import com.biblioteca.dto.response.PrestamoItemResponse;
import com.biblioteca.dto.response.PrestamoKpisResponse;
import com.biblioteca.entity.Bibliotecario;
import com.biblioteca.entity.Ejemplar;
import com.biblioteca.entity.Lector;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Prestamo;
import com.biblioteca.entity.enums.EstadoEjemplar;
import com.biblioteca.entity.enums.EstadoPrestamo;
import com.biblioteca.entity.enums.EstadoUsuario;
import com.biblioteca.repository.BibliotecarioRepository;
import com.biblioteca.repository.EjemplarRepository;
import com.biblioteca.repository.LectorRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.service.PrestamoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PrestamoServiceImpl implements PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LectorRepository lectorRepository;
    private final EjemplarRepository ejemplarRepository;
    private final BibliotecarioRepository bibliotecarioRepository;

    public PrestamoServiceImpl(PrestamoRepository prestamoRepository, LectorRepository lectorRepository, EjemplarRepository ejemplarRepository, BibliotecarioRepository bibliotecarioRepository) {
        this.prestamoRepository = prestamoRepository;
        this.lectorRepository = lectorRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.bibliotecarioRepository = bibliotecarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PrestamoKpisResponse obtenerKpis() {
        List<Prestamo> prestamos = prestamoRepository.findAll();
        LocalDate hoy = LocalDate.now();
        long activos = prestamos.stream().filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO || p.getEstado() == EstadoPrestamo.RENOVADO).count();
        long vencidos = prestamos.stream().filter(p -> {
            if (p.getEstado() != EstadoPrestamo.ACTIVO && p.getEstado() != EstadoPrestamo.RENOVADO) {
                return false;
            }
            if (p.getFechaDevolucionProgramada() == null) {
                return false;
            }
            return p.getFechaDevolucionProgramada().toLocalDate().isBefore(hoy);
        }).count();
        long prestamosHoy = prestamos.stream().filter(p -> p.getFechaInicio() != null && p.getFechaInicio().toLocalDate().isEqual(hoy)).count();
        return new PrestamoKpisResponse(activos, vencidos, prestamosHoy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoItemResponse> listarPrestamos(String busqueda, String estado) {
        List<Prestamo> prestamos = prestamoRepository.findAll();
        String q = busqueda == null ? null : busqueda.trim().toLowerCase(Locale.ROOT);
        EstadoPrestamo estadoFiltro = null;
        if (estado != null && !estado.trim().isEmpty() && !"todos".equalsIgnoreCase(estado.trim())) {
            String e = estado.trim().toUpperCase(Locale.ROOT);
            try {
                if ("VENCIDO".equals(e)) {
                    estadoFiltro = EstadoPrestamo.VENCIDO;
                } else if ("ACTIVO".equals(e) || "EN_PRESTAMO".equals(e) || "PRESTADO".equals(e)) {
                    estadoFiltro = EstadoPrestamo.ACTIVO;
                } else {
                    estadoFiltro = EstadoPrestamo.valueOf(e);
                }
            } catch (Exception ignored) {
            }
        }
        final EstadoPrestamo ef = estadoFiltro;
        final LocalDate hoy = LocalDate.now();

        List<PrestamoItemResponse> resultado = new ArrayList<>();
        for (Prestamo p : prestamos) {
            EstadoPrestamo est = p.getEstado();
            if (ef != null) {
                if (ef == EstadoPrestamo.ACTIVO) {
                    if (!(est == EstadoPrestamo.ACTIVO || est == EstadoPrestamo.RENOVADO)) {
                        continue;
                    }
                } else if (est != ef) {
                    continue;
                }
            }
            if (q != null && !q.isEmpty()) {
                Lector lector = p.getLector();
                Ejemplar ej = p.getEjemplar();
                Libro libro = ej != null ? ej.getLibro() : null;
                String codEst = lector != null && lector.getCodigoEstudiante() != null ? lector.getCodigoEstudiante().toLowerCase(Locale.ROOT) : "";
                String nomL = lector != null && lector.getNombreCompleto() != null ? lector.getNombreCompleto().toLowerCase(Locale.ROOT) : "";
                String cb = ej != null && ej.getCodigoBarra() != null ? ej.getCodigoBarra().toLowerCase(Locale.ROOT) : "";
                String tit = libro != null && libro.getTitulo() != null ? libro.getTitulo().toLowerCase(Locale.ROOT) : "";
                boolean match = codEst.contains(q) || nomL.contains(q) || cb.contains(q) || tit.contains(q);
                if (!match) {
                    continue;
                }
            }
            Lector lector = p.getLector();
            Ejemplar ej = p.getEjemplar();
            Libro libro = ej != null ? ej.getLibro() : null;
            Bibliotecario bib = p.getBibliotecario();
            String estadoResp = (est == EstadoPrestamo.ACTIVO || est == EstadoPrestamo.RENOVADO) && p.getFechaDevolucionProgramada() != null && p.getFechaDevolucionProgramada().toLocalDate().isBefore(hoy) ? "VENCIDO" : est.name();
            LocalDate fechaPrestamo = p.getFechaInicio() == null ? null : p.getFechaInicio().toLocalDate();
            LocalDate fechaPactada = p.getFechaDevolucionProgramada() == null ? null : p.getFechaDevolucionProgramada().toLocalDate();
            resultado.add(new PrestamoItemResponse(
                    p.getIdPrestamo(),
                    lector != null ? lector.getCodigoEstudiante() : null,
                    lector != null ? lector.getNombreCompleto() : null,
                    ej != null ? ej.getCodigoBarra() : null,
                    libro != null ? libro.getTitulo() : null,
                    fechaPrestamo,
                    fechaPactada,
                    estadoResp,
                    bib != null ? bib.getNombreCompleto() : null
            ));
        }
        return resultado;
    }

    @Override
    @Transactional
    public void registrarPrestamo(RegistrarPrestamoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud inválida");
        }
        String idLector = request.getIdentificadorLector() == null ? "" : request.getIdentificadorLector().trim();
        String codBarra = request.getCodigoBarra() == null ? "" : request.getCodigoBarra().trim();
        if (idLector.isEmpty() || codBarra.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador de lector y código de barra son obligatorios");
        }

        Lector lector = lectorRepository.findByCodigoEstudiante(idLector).orElse(null);
        if (lector == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El lector no existe o está inactivo");
        }
        if (lector.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El lector no existe o está inactivo");
        }

        long activos = prestamoRepository.countByLectorIdUsuarioAndEstado(lector.getIdUsuario(), EstadoPrestamo.ACTIVO);
        long renovados = prestamoRepository.countByLectorIdUsuarioAndEstado(lector.getIdUsuario(), EstadoPrestamo.RENOVADO);
        long totalActivos = activos + renovados;
        int limite = lector.getLimiteReservas() == null ? 3 : lector.getLimiteReservas();
        if (totalActivos >= limite) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El lector ha alcanzado su límite de préstamos permitidos");
        }

        Ejemplar ejemplar = ejemplarRepository.findById(codBarra).orElse(null);
        if (ejemplar == null || ejemplar.getEstado() != EstadoEjemplar.DISPONIBLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ejemplar no está disponible para préstamo");
        }

        Bibliotecario bibliotecario = null;
        if (request.getIdBibliotecario() != null) {
            bibliotecario = bibliotecarioRepository.findById(request.getIdBibliotecario()).orElse(null);
        }

        int dias = request.getDiasPrestamo() == null || request.getDiasPrestamo() <= 0 ? 7 : request.getDiasPrestamo();
        LocalDateTime now = LocalDateTime.now();
        Prestamo prestamo = new Prestamo();
        prestamo.setFechaInicio(now);
        prestamo.setFechaDevolucionProgramada(now.plusDays(dias));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        prestamo.setContadorRenovaciones(0);
        prestamo.setLector(lector);
        prestamo.setEjemplar(ejemplar);
        prestamo.setBibliotecario(bibliotecario);

        ejemplar.setEstado(EstadoEjemplar.PRESTADO);
        ejemplarRepository.save(ejemplar);
        prestamoRepository.save(prestamo);
    }
}
