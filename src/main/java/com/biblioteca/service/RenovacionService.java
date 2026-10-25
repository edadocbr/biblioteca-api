package com.biblioteca.service;

import com.biblioteca.dto.request.RegistrarRenovacionRequest;
import com.biblioteca.dto.response.RenovacionItemResponse;
import com.biblioteca.dto.response.RenovacionKpisResponse;

import java.util.List;

public interface RenovacionService {
    RenovacionKpisResponse obtenerKpis();
    List<RenovacionItemResponse> listarPrestamosParaRenovacion(String busqueda);
    RenovacionItemResponse solicitarRenovacion(RegistrarRenovacionRequest request);
}
