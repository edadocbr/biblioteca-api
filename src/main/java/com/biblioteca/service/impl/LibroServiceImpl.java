package com.biblioteca.service.impl;

import com.biblioteca.dto.response.LibroResponse;
import com.biblioteca.entity.enums.EstadoEjemplar;
import com.biblioteca.repository.EjemplarRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.service.LibroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibroServiceImpl implements LibroService{
    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;

    public LibroServiceImpl(LibroRepository libroRepository, EjemplarRepository ejemplarRepository) {
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
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
}
