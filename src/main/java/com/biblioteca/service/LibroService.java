package com.biblioteca.service;

import com.biblioteca.dto.response.LibroResponse;
import java.util.List;

public interface LibroService {
    List<LibroResponse> listarCatalogo();
}
