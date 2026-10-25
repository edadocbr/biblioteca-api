package com.biblioteca.service;

import com.biblioteca.dto.request.SolicitarReservaRequest;
import com.biblioteca.dto.response.SolicitarReservaResponse;

public interface ReservaService {
    SolicitarReservaResponse solicitarReserva(SolicitarReservaRequest request);
}
