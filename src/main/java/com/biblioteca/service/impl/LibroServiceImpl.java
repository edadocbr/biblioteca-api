package com.biblioteca.service.impl;

import com.biblioteca.dto.response.CatalogoLibroResponse;
import com.biblioteca.dto.response.LibroResponse;
import com.biblioteca.entity.Reserva;
import com.biblioteca.entity.enums.EstadoEjemplar;
import com.biblioteca.entity.enums.EstadoReserva;
import com.biblioteca.repository.BibliotecarioRepository;
import com.biblioteca.repository.EjemplarRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.service.LibroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LibroServiceImpl implements LibroService{
    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    private final ReservaRepository reservaRepository;
    private final BibliotecarioRepository bibliotecarioRepository;

    public LibroServiceImpl(LibroRepository libroRepository,
                            EjemplarRepository ejemplarRepository,
                            ReservaRepository reservaRepository,
                            BibliotecarioRepository bibliotecarioRepository) {
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.reservaRepository = reservaRepository;
        this.bibliotecarioRepository = bibliotecarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibroResponse> listarCatalogo() {
        return libroRepository.findAll().stream().map(libro -> {
            long disponibles = ejemplarRepository.countByLibroIsbnAndEstado(
                libro.getIsbn(), 
                EstadoEjemplar.DISPONIBLE
            );

            return new LibroResponse(
                libro.getIsbn(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getEditorial(),
                libro.getAnioPublicacion(),
                disponibles
            );
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoLibroResponse> listarCatalogoPublico(Long idLector) {
        Map<String, String> reservasActivasPorLibro = new HashMap<>();
        if (idLector != null && !bibliotecarioRepository.existsById(idLector)) {
            List<Reserva> reservasActivas = reservaRepository.findActivasByLector(idLector);
            for (Reserva r : reservasActivas) {
                if (r.getLibro() == null || r.getLibro().getIsbn() == null) {
                    continue;
                }
                String isbn = r.getLibro().getIsbn();
                String estadoActual = reservasActivasPorLibro.get(isbn);
                EstadoReserva nuevo = r.getEstado();
                // Priorizar LISTA_PARA_RECOJO sobre EN_ESPERA si hay varias
                if (estadoActual == null || EstadoReserva.LISTA_PARA_RECOJO.equals(nuevo)) {
                    reservasActivasPorLibro.put(isbn, nuevo.name());
                }
            }
        }

        return libroRepository.findAll().stream().map(libro -> {
            String isbn = libro.getIsbn();
            long total = ejemplarRepository.countByLibroIsbn(isbn);
            long disponibles = ejemplarRepository.countByLibroIsbnAndEstado(
                isbn, EstadoEjemplar.DISPONIBLE);

            return new CatalogoLibroResponse(
                isbn,
                libro.getTitulo(),
                libro.getAutor(),
                libro.getAnioPublicacion(),
                (int) total,
                (int) disponibles,
                reservasActivasPorLibro.get(isbn)
            );
        }).toList();
    }
}
