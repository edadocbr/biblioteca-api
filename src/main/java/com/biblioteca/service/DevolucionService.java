package com.biblioteca.service;

import com.biblioteca.dto.request.RegistrarDevolucionRequest;
import com.biblioteca.dto.response.DevolucionItemResponse;
import com.biblioteca.dto.response.DevolucionKpisResponse;

import java.util.List;

public interface DevolucionService {
    DevolucionKpisResponse obtenerKpis();
    List<DevolucionItemResponse> listarHistorialDevoluciones(String busqueda);
    DevolucionItemResponse registrarDevolucion(RegistrarDevolucionRequest request);
}
