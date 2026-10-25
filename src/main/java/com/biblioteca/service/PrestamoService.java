package com.biblioteca.service;

import com.biblioteca.dto.request.RegistrarPrestamoRequest;
import com.biblioteca.dto.response.PrestamoItemResponse;
import com.biblioteca.dto.response.PrestamoKpisResponse;

import java.util.List;

public interface PrestamoService {
    PrestamoKpisResponse obtenerKpis();
    List<PrestamoItemResponse> listarPrestamos(String busqueda, String estado);
    void registrarPrestamo(RegistrarPrestamoRequest request);
}
