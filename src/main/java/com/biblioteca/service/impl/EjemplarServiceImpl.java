package com.biblioteca.service.impl;

import com.biblioteca.dto.request.AdquirirMaterialRequest;
import com.biblioteca.dto.response.EjemplarItemResponse;
import com.biblioteca.dto.response.InventarioKpisResponse;
import com.biblioteca.entity.Ejemplar;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.enums.EstadoEjemplar;
import com.biblioteca.repository.EjemplarRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.service.EjemplarService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class EjemplarServiceImpl implements EjemplarService {

    private final EjemplarRepository ejemplarRepository;
    private final LibroRepository libroRepository;

    public EjemplarServiceImpl(EjemplarRepository ejemplarRepository, LibroRepository libroRepository) {
        this.ejemplarRepository = ejemplarRepository;
        this.libroRepository = libroRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioKpisResponse obtenerKpisInventario() {
        long ejemplaresFisicos = ejemplarRepository.count();
        long totalTitulos = libroRepository.count();
        long nuevasAdquisiciones = ejemplaresFisicos;
        return new InventarioKpisResponse(totalTitulos, ejemplaresFisicos, nuevasAdquisiciones);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EjemplarItemResponse> listarInventario(String busqueda, String estado) {
        List<Ejemplar> ejemplares = ejemplarRepository.findAll();
        final String q = busqueda == null ? null : busqueda.trim().toLowerCase(Locale.ROOT);
        EstadoEjemplar estadoFiltro = null;
        if (estado != null && !estado.trim().isEmpty() && !"todos".equalsIgnoreCase(estado.trim())) {
            String e = estado.trim().toUpperCase(Locale.ROOT);
            try {
                if ("EN_PRESTAMO".equals(e)) {
                    estadoFiltro = EstadoEjemplar.PRESTADO;
                } else {
                    estadoFiltro = EstadoEjemplar.valueOf(e);
                }
            } catch (Exception ignored) {
            }
        }
        final EstadoEjemplar ef = estadoFiltro;

        List<Ejemplar> filtrados = ejemplares.stream().filter(ej -> {
            if (ef != null && ej.getEstado() != ef) {
                return false;
            }
            if (q == null || q.isEmpty()) {
                return true;
            }
            Libro libro = ej.getLibro();
            if (libro == null) {
                return false;
            }
            String isbn = libro.getIsbn() == null ? "" : libro.getIsbn().toLowerCase(Locale.ROOT);
            String titulo = libro.getTitulo() == null ? "" : libro.getTitulo().toLowerCase(Locale.ROOT);
            String autor = libro.getAutor() == null ? "" : libro.getAutor().toLowerCase(Locale.ROOT);
            String cb = ej.getCodigoBarra() == null ? "" : ej.getCodigoBarra().toLowerCase(Locale.ROOT);
            String rfid = ej.getTagRfid() == null ? "" : ej.getTagRfid().toLowerCase(Locale.ROOT);
            return isbn.contains(q) || titulo.contains(q) || autor.contains(q) || cb.contains(q) || rfid.contains(q);
        }).collect(Collectors.toList());

        List<EjemplarItemResponse> resultado = new ArrayList<>();
        for (Ejemplar ej : filtrados) {
            Libro libro = ej.getLibro();
            String estadoStr = ej.getEstado() == null ? "" : mapEstado(ej.getEstado());
            resultado.add(new EjemplarItemResponse(
                    libro != null ? libro.getIsbn() : null,
                    ej.getCodigoBarra(),
                    ej.getTagRfid(),
                    libro != null ? libro.getTitulo() : null,
                    libro != null ? libro.getAutor() : null,
                    libro != null ? libro.getAnioPublicacion() : null,
                    estadoStr
            ));
        }
        return resultado;
    }

    private String mapEstado(EstadoEjemplar estado) {
        if (estado == EstadoEjemplar.PRESTADO) {
            return "En Préstamo";
        }
        if (estado == EstadoEjemplar.DISPONIBLE) {
            return "Disponible";
        }
        if (estado == EstadoEjemplar.PERDIDO || estado == EstadoEjemplar.EN_REPARACION) {
            return "Baja";
        }
        if (estado == EstadoEjemplar.RESERVADO) {
            return "Reservado";
        }
        return estado.name();
    }

    @Override
    @Transactional
    public void adquirirMaterial(AdquirirMaterialRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Solicitud inválida");
        }
        String isbn = request.getIsbn() == null ? null : request.getIsbn().trim();
        if (isbn == null || isbn.isEmpty()) {
            throw new IllegalArgumentException("ISBN requerido");
        }
        Libro libro = libroRepository.findById(isbn).orElse(null);
        if (libro == null) {
            libro = new Libro();
            libro.setIsbn(isbn);
            libro.setTitulo(request.getTitulo() == null ? "" : request.getTitulo().trim());
            libro.setAutor(request.getAutor() == null ? "" : request.getAutor().trim());
            libro.setAnioPublicacion(request.getAnioPublicacion());
            libro.setEditorial(request.getEditorial());
            libro = libroRepository.save(libro);
        }

        List<String> codigos = request.getCodigosBarra() == null ? new ArrayList<>() : request.getCodigosBarra();
        List<String> rfids = request.getTagsRfid() == null ? new ArrayList<>() : request.getTagsRfid();
        int size = Math.max(codigos.size(), rfids.size());
        if (size == 0) {
            throw new IllegalArgumentException("Se requiere al menos un ejemplar");
        }
        for (int i = 0; i < size; i++) {
            String cb = i < codigos.size() && codigos.get(i) != null ? codigos.get(i).trim() : null;
            String rfid = i < rfids.size() && rfids.get(i) != null ? rfids.get(i).trim().toUpperCase(Locale.ROOT) : null;
            if (cb == null || cb.isEmpty()) {
                throw new IllegalArgumentException("Código de barra requerido");
            }
            if (ejemplarRepository.existsById(cb)) {
                throw new IllegalArgumentException("Código de barra ya asignado");
            }
            if (rfid != null && !rfid.isEmpty()) {
                if (ejemplarRepository.findByTagRfid(rfid).isPresent()) {
                    throw new IllegalArgumentException("Tag RFID ya asignado");
                }
            }
            Ejemplar ej = new Ejemplar(cb, rfid, null, EstadoEjemplar.DISPONIBLE, libro);
            ejemplarRepository.save(ej);
        }
    }

    @Override
    @Transactional
    public void cambiarEstadoEjemplar(String codigoBarra, String nuevoEstado) {
        Ejemplar ej = ejemplarRepository.findById(codigoBarra).orElseThrow(() -> new IllegalArgumentException("Ejemplar no encontrado"));
        String e = nuevoEstado == null ? "" : nuevoEstado.trim().toUpperCase(Locale.ROOT);
        if ("BAJA".equals(e)) {
            ej.setEstado(EstadoEjemplar.PERDIDO);
        } else if ("EN_PRESTAMO".equals(e)) {
            ej.setEstado(EstadoEjemplar.PRESTADO);
        } else if ("DISPONIBLE".equals(e)) {
            ej.setEstado(EstadoEjemplar.DISPONIBLE);
        } else {
            try {
                ej.setEstado(EstadoEjemplar.valueOf(e));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Estado inválido");
            }
        }
        ejemplarRepository.save(ej);
    }
}
