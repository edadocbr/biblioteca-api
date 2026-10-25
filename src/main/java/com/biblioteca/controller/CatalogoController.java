package com.biblioteca.controller;

import com.biblioteca.dto.response.CatalogoLibroResponse;
import com.biblioteca.service.LibroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class CatalogoController {

    private final LibroService libroService;

    public CatalogoController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping("/libros")
    public ResponseEntity<List<CatalogoLibroResponse>> listarLibros(
            @RequestParam(value = "idLector", required = false) Long idLector) {
        return ResponseEntity.ok(libroService.listarCatalogoPublico(idLector));
    }
}
