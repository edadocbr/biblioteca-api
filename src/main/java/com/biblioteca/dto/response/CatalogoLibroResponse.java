package com.biblioteca.dto.response;

public class CatalogoLibroResponse {
    private String isbn;
    private String titulo;
    private String autor;
    private Integer anioPublicacion;
    private int stockTotal;
    private int stockDisponible;
    private String estadoSolicitud; // null, 'LISTA_PARA_RECOJO', 'EN_ESPERA'

    public CatalogoLibroResponse() {}

    public CatalogoLibroResponse(String isbn, String titulo, String autor, Integer anioPublicacion,
                                 int stockTotal, int stockDisponible, String estadoSolicitud) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.anioPublicacion = anioPublicacion;
        this.stockTotal = stockTotal;
        this.stockDisponible = stockDisponible;
        this.estadoSolicitud = estadoSolicitud;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Integer getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(Integer anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    public int getStockTotal() {
        return stockTotal;
    }

    public void setStockTotal(int stockTotal) {
        this.stockTotal = stockTotal;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(int stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public String getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public void setEstadoSolicitud(String estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }
}
