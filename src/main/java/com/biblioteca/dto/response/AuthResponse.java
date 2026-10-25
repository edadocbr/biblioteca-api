package com.biblioteca.dto.response;

public class AuthResponse {
    private Long idUsuario;
    private String nombreCompleto;
    private String email;
    private String rol;
    private String codigoEstudiante;
    private Integer limiteReservas;
    
    public AuthResponse() {}
    
    public AuthResponse(Long idUsuario, String nombreCompleto, String email, String rol, String codigoEstudiante, Integer limiteReservas) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.rol = rol;
        this.codigoEstudiante = codigoEstudiante;
        this.limiteReservas = limiteReservas;
    }
    
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public String getCodigoEstudiante() { return codigoEstudiante; }
    public void setCodigoEstudiante(String codigoEstudiante) { this.codigoEstudiante = codigoEstudiante; }
    
    public Integer getLimiteReservas() { return limiteReservas; }
    public void setLimiteReservas(Integer limiteReservas) { this.limiteReservas = limiteReservas; }
}
