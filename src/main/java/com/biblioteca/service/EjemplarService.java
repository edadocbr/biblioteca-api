package com.biblioteca.service;

import com.biblioteca.dto.request.AdquirirMaterialRequest;
import com.biblioteca.dto.response.EjemplarItemResponse;
import com.biblioteca.dto.response.InventarioKpisResponse;

import java.util.List;

public interface EjemplarService {
    InventarioKpisResponse obtenerKpisInventario();
    List<EjemplarItemResponse> listarInventario(String busqueda, String estado);
    void adquirirMaterial(AdquirirMaterialRequest request);
    void cambiarEstadoEjemplar(String codigoBarra, String nuevoEstado);
}
