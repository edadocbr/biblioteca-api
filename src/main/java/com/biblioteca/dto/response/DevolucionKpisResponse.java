package com.biblioteca.dto.response;

public class DevolucionKpisResponse {
    private Long devolucionesHoy;
    private Long devolucionesEnFecha;
    private Long devolucionesConRetraso;

    public DevolucionKpisResponse() {}

    public DevolucionKpisResponse(Long devolucionesHoy, Long devolucionesEnFecha, Long devolucionesConRetraso) {
        this.devolucionesHoy = devolucionesHoy;
        this.devolucionesEnFecha = devolucionesEnFecha;
        this.devolucionesConRetraso = devolucionesConRetraso;
    }

    public Long getDevolucionesHoy() {
        return devolucionesHoy;
    }

    public void setDevolucionesHoy(Long devolucionesHoy) {
        this.devolucionesHoy = devolucionesHoy;
    }

    public Long getDevolucionesEnFecha() {
        return devolucionesEnFecha;
    }

    public void setDevolucionesEnFecha(Long devolucionesEnFecha) {
        this.devolucionesEnFecha = devolucionesEnFecha;
    }

    public Long getDevolucionesConRetraso() {
        return devolucionesConRetraso;
    }

    public void setDevolucionesConRetraso(Long devolucionesConRetraso) {
        this.devolucionesConRetraso = devolucionesConRetraso;
    }
}
