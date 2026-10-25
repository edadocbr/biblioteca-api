package com.biblioteca.controller;

import com.biblioteca.dto.request.RegistrarRenovacionRequest;
import com.biblioteca.dto.response.RenovacionItemResponse;
import com.biblioteca.dto.response.RenovacionKpisResponse;
import com.biblioteca.service.RenovacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/renovaciones")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class RenovacionController {

    private final RenovacionService renovacionService;

    public RenovacionController(RenovacionService renovacionService) {
        this.renovacionService = renovacionService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<RenovacionKpisResponse> obtenerKpis() {
        return ResponseEntity.ok(renovacionService.obtenerKpis());
    }

    @GetMapping
    public ResponseEntity<List<RenovacionItemResponse>> listarPrestamosParaRenovacion(
            @RequestParam(value = "q", required = false) String q) {
        return ResponseEntity.ok(renovacionService.listarPrestamosParaRenovacion(q));
    }

    @PostMapping
    public ResponseEntity<RenovacionItemResponse> solicitarRenovacion(
            @Valid @RequestBody RegistrarRenovacionRequest request) {
        return ResponseEntity.ok(renovacionService.solicitarRenovacion(request));
    }
}
