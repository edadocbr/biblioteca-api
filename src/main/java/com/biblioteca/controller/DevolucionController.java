package com.biblioteca.controller;

import com.biblioteca.dto.request.RegistrarDevolucionRequest;
import com.biblioteca.dto.response.DevolucionItemResponse;
import com.biblioteca.dto.response.DevolucionKpisResponse;
import com.biblioteca.service.DevolucionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/devoluciones")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<DevolucionKpisResponse> obtenerKpis() {
        return ResponseEntity.ok(devolucionService.obtenerKpis());
    }

    @GetMapping
    public ResponseEntity<List<DevolucionItemResponse>> listarHistorial(
            @RequestParam(value = "q", required = false) String q) {
        return ResponseEntity.ok(devolucionService.listarHistorialDevoluciones(q));
    }

    @PostMapping
    public ResponseEntity<DevolucionItemResponse> registrarDevolucion(@RequestBody RegistrarDevolucionRequest request) {
        return ResponseEntity.ok(devolucionService.registrarDevolucion(request));
    }
}
