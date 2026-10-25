package com.biblioteca.entity;

import com.biblioteca.entity.enums.EstadoUsuario;
import jakarta.persistence.*;

@Entity
@Table(name = "lectores")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Lector extends Usuario {
    @Column(name = "codigo_estudiante", length = 50, unique = true, nullable = false)
    private String codigoEstudiante;

    @Column(name = "limite_reservas", nullable = false)
    private Integer limiteReservas = 3;

    public Lector() {
        super();
    }

    public Lector(String dni, String nombreCompleto, String email, EstadoUsuario estado, String codigoEstudiante) {
        super(dni, nombreCompleto, email, estado);
        this.codigoEstudiante = codigoEstudiante;
        this.limiteReservas = 3;
    }

    public Lector(String dni, String nombreCompleto, String email, EstadoUsuario estado, String password, String codigoEstudiante) {
        super(dni, nombreCompleto, email, estado);
        this.setPassword(password);
        this.codigoEstudiante = codigoEstudiante;
        this.limiteReservas = 3;
    }

    public String getCodigoEstudiante() { return codigoEstudiante; }
    public void setCodigoEstudiante(String codigoEstudiante) { this.codigoEstudiante = codigoEstudiante; }

    public Integer getLimiteReservas() { return limiteReservas; }
    public void setLimiteReservas(Integer limiteReservas) { this.limiteReservas = limiteReservas; }
}
