package com.biblioteca.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistroLectorRequest {
    
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150)
    private String nombreCompleto;
    
    @NotBlank(message = "El DNI es obligatorio")
    @Size(max = 20)
    private String dni;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 100)
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    @NotBlank(message = "El código de estudiante es obligatorio")
    @Size(max = 50)
    private String codigoEstudiante;
    
    public RegistroLectorRequest() {}
    
    public RegistroLectorRequest(String nombreCompleto, String dni, String email, String password, String codigoEstudiante) {
        this.nombreCompleto = nombreCompleto;
        this.dni = dni;
        this.email = email;
        this.password = password;
        this.codigoEstudiante = codigoEstudiante;
    }
    
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getCodigoEstudiante() { return codigoEstudiante; }
    public void setCodigoEstudiante(String codigoEstudiante) { this.codigoEstudiante = codigoEstudiante; }
}
