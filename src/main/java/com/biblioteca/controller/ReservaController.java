package com.biblioteca.controller;

import com.biblioteca.dto.request.SolicitarReservaRequest;
import com.biblioteca.dto.response.SolicitarReservaResponse;
import com.biblioteca.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<SolicitarReservaResponse> solicitarReserva(@Valid @RequestBody SolicitarReservaRequest request) {
        return ResponseEntity.ok(reservaService.solicitarReserva(request));
    }
}
