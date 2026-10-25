package com.biblioteca.dto.response;

public class EjemplarItemResponse {
    private String isbn;
    private String codigoBarra;
    private String tagRfid;
    private String titulo;
    private String autor;
    private Integer anioPublicacion;
    private String estado;

    public EjemplarItemResponse() {}

    public EjemplarItemResponse(String isbn, String codigoBarra, String tagRfid, String titulo, String autor, Integer anioPublicacion, String estado) {
        this.isbn = isbn;
        this.codigoBarra = codigoBarra;
        this.tagRfid = tagRfid;
        this.titulo = titulo;
        this.autor = autor;
        this.anioPublicacion = anioPublicacion;
        this.estado = estado;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }

    public String getTagRfid() {
        return tagRfid;
    }

    public void setTagRfid(String tagRfid) {
        this.tagRfid = tagRfid;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
