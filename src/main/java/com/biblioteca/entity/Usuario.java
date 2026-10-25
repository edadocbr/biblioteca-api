package com.biblioteca.entity;

import com.biblioteca.entity.enums.EstadoUsuario;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(length = 20, unique = true, nullable = false)
    private String dni;

    @Column(name = "nombre_completo", length = 150, nullable = false)
    private String nombreCompleto;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Column(length = 255)
    private String password;

    public Usuario() {}

    public Usuario(String dni, String nombreCompleto, String email, EstadoUsuario estado) {
        this.dni = dni;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.estado = estado;
    }

    public Usuario(String dni, String nombreCompleto, String email, EstadoUsuario estado, String password) {
        this.dni = dni;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.estado = estado;
        this.password = password;
    }

    // Getters y Setters
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
