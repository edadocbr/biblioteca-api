package com.biblioteca.service;

import com.biblioteca.dto.request.LoginRequest;
import com.biblioteca.dto.request.RegistroLectorRequest;
import com.biblioteca.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registrarLector(RegistroLectorRequest request);
    AuthResponse login(LoginRequest request);
}
