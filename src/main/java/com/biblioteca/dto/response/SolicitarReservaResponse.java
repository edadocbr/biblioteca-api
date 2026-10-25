package com.biblioteca.dto.response;

import java.time.LocalDate;

public class SolicitarReservaResponse {
    private String estado;
    private String mensaje;
    private LocalDate fechaLimiteRecojo;

    public SolicitarReservaResponse() {}

    public SolicitarReservaResponse(String estado, String mensaje, LocalDate fechaLimiteRecojo) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.fechaLimiteRecojo = fechaLimiteRecojo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDate getFechaLimiteRecojo() {
        return fechaLimiteRecojo;
    }

    public void setFechaLimiteRecojo(LocalDate fechaLimiteRecojo) {
        this.fechaLimiteRecojo = fechaLimiteRecojo;
    }
}
