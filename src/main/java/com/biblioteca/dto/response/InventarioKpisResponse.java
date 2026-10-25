package com.biblioteca.dto.response;

public class InventarioKpisResponse {
    private Long totalTitulos;
    private Long ejemplaresFisicos;
    private Long nuevasAdquisiciones;

    public InventarioKpisResponse() {}

    public InventarioKpisResponse(Long totalTitulos, Long ejemplaresFisicos, Long nuevasAdquisiciones) {
        this.totalTitulos = totalTitulos;
        this.ejemplaresFisicos = ejemplaresFisicos;
        this.nuevasAdquisiciones = nuevasAdquisiciones;
    }

    public Long getTotalTitulos() {
        return totalTitulos;
    }

    public void setTotalTitulos(Long totalTitulos) {
        this.totalTitulos = totalTitulos;
    }

    public Long getEjemplaresFisicos() {
        return ejemplaresFisicos;
    }

    public void setEjemplaresFisicos(Long ejemplaresFisicos) {
        this.ejemplaresFisicos = ejemplaresFisicos;
    }

    public Long getNuevasAdquisiciones() {
        return nuevasAdquisiciones;
    }

    public void setNuevasAdquisiciones(Long nuevasAdquisiciones) {
        this.nuevasAdquisiciones = nuevasAdquisiciones;
    }
}
