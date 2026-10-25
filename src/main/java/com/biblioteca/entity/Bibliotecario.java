package com.biblioteca.entity;

import com.biblioteca.entity.enums.EstadoUsuario;
import jakarta.persistence.*;

@Entity
@Table(name = "bibliotecarios")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Bibliotecario extends Usuario {
    @Column(name = "codigo_empleado", length = 50, unique = true, nullable = false)
    private String codigoEmpleado;

    @Column(length = 50)
    private String turno;

    public Bibliotecario() {
        super();
    }

    public Bibliotecario(String dni, String nombreCompleto, String email, EstadoUsuario estado, String codigoEmpleado, String turno) {
        super(dni, nombreCompleto, email, estado);
        this.codigoEmpleado = codigoEmpleado;
        this.turno = turno;
    }

    public Bibliotecario(String dni, String nombreCompleto, String email, EstadoUsuario estado, String password, String codigoEmpleado, String turno) {
        super(dni, nombreCompleto, email, estado);
        this.setPassword(password);
        this.codigoEmpleado = codigoEmpleado;
        this.turno = turno;
    }

    public String getCodigoEmpleado() { return codigoEmpleado; }
    public void setCodigoEmpleado(String codigoEmpleado) { this.codigoEmpleado = codigoEmpleado; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
}
