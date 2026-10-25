package com.biblioteca.controller;

import com.biblioteca.dto.request.AdquirirMaterialRequest;
import com.biblioteca.dto.response.EjemplarItemResponse;
import com.biblioteca.dto.response.InventarioKpisResponse;
import com.biblioteca.service.EjemplarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ejemplares")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class EjemplarController {

    private final EjemplarService ejemplarService;

    public EjemplarController(EjemplarService ejemplarService) {
        this.ejemplarService = ejemplarService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<InventarioKpisResponse> obtenerKpis() {
        return ResponseEntity.ok(ejemplarService.obtenerKpisInventario());
    }

    @GetMapping
    public ResponseEntity<List<EjemplarItemResponse>> listarInventario(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) String estado) {
        String query = q != null ? q : busqueda;
        return ResponseEntity.ok(ejemplarService.listarInventario(query, estado));
    }

    @PostMapping("/adquirir")
    public ResponseEntity<Void> adquirirMaterial(@RequestBody AdquirirMaterialRequest request) {
        ejemplarService.adquirirMaterial(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{codigoBarra}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable String codigoBarra, @RequestParam String nuevoEstado) {
        ejemplarService.cambiarEstadoEjemplar(codigoBarra, nuevoEstado);
        return ResponseEntity.ok().build();
    }
}
