package com.biblioteca.service;

import com.biblioteca.dto.response.CatalogoLibroResponse;
import com.biblioteca.dto.response.LibroResponse;

import java.util.List;

public interface LibroService {
    List<LibroResponse> listarCatalogo();
    List<CatalogoLibroResponse> listarCatalogoPublico(Long idLector);
}
