package com.biblioteca.dto.request;

import java.util.List;

public class AdquirirMaterialRequest {
    private String isbn;
    private String titulo;
    private String autor;
    private Integer anioPublicacion;
    private String categoria;
    private String editorial;
    private List<String> codigosBarra;
    private List<String> tagsRfid;

    public AdquirirMaterialRequest() {}

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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public List<String> getCodigosBarra() {
        return codigosBarra;
    }

    public void setCodigosBarra(List<String> codigosBarra) {
        this.codigosBarra = codigosBarra;
    }

    public List<String> getTagsRfid() {
        return tagsRfid;
    }

    public void setTagsRfid(List<String> tagsRfid) {
        this.tagsRfid = tagsRfid;
    }
}
