package com.biblioteca.dto.response;

public record LibroResponse(
    String isbn,
    String titulo,
    String autor,
    String editorial,
    Integer anioPublicacion,
    long ejemplaresDisponibles
) {} 
