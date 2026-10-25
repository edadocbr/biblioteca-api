package com.biblioteca.controller;

import com.biblioteca.dto.request.RegistrarPrestamoRequest;
import com.biblioteca.dto.response.PrestamoItemResponse;
import com.biblioteca.dto.response.PrestamoKpisResponse;
import com.biblioteca.service.PrestamoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/prestamos")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<PrestamoKpisResponse> obtenerKpis() {
        return ResponseEntity.ok(prestamoService.obtenerKpis());
    }

    @GetMapping
    public ResponseEntity<List<PrestamoItemResponse>> listarPrestamos(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) String estado) {
        String query = q != null ? q : busqueda;
        return ResponseEntity.ok(prestamoService.listarPrestamos(query, estado));
    }

    @PostMapping
    public ResponseEntity<Void> registrarPrestamo(@RequestBody RegistrarPrestamoRequest request) {
        prestamoService.registrarPrestamo(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
