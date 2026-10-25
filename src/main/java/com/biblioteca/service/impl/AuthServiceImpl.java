package com.biblioteca.service.impl;

import com.biblioteca.dto.request.LoginRequest;
import com.biblioteca.dto.request.RegistroLectorRequest;
import com.biblioteca.dto.response.AuthResponse;
import com.biblioteca.entity.Bibliotecario;
import com.biblioteca.entity.Lector;
import com.biblioteca.entity.enums.EstadoUsuario;
import com.biblioteca.repository.LectorRepository;
import com.biblioteca.repository.UsuarioRepository;
import com.biblioteca.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final LectorRepository lectorRepository;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, LectorRepository lectorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.lectorRepository = lectorRepository;
    }

    @Override
    @Transactional
    public AuthResponse registrarLector(RegistroLectorRequest request) {
        if (usuarioRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("El DNI ya se encuentra registrado");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya se encuentra registrado");
        }
        if (lectorRepository.existsByCodigoEstudiante(request.getCodigoEstudiante())) {
            throw new IllegalArgumentException("El código de estudiante ya se encuentra registrado");
        }

        Lector lector = new Lector(
                request.getDni(),
                request.getNombreCompleto(),
                request.getEmail(),
                EstadoUsuario.ACTIVO,
                request.getPassword(),
                request.getCodigoEstudiante()
        );
        lector.setLimiteReservas(3);
        lector.setEstado(EstadoUsuario.ACTIVO);

        Lector saved = lectorRepository.save(lector);

        return new AuthResponse(
                saved.getIdUsuario(),
                saved.getNombreCompleto(),
                saved.getEmail(),
                "LECTOR",
                saved.getCodigoEstudiante(),
                saved.getLimiteReservas()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        var usuarioOpt = usuarioRepository.findByEmail(request.getEmail());
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        var usuario = usuarioOpt.get();
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new IllegalArgumentException("Usuario inactivo");
        }
        if (usuario.getPassword() == null || !usuario.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String rol = "USUARIO";
        String codigoEstudiante = null;
        Integer limiteReservas = null;

        if (usuario instanceof Lector lector) {
            rol = "LECTOR";
            codigoEstudiante = lector.getCodigoEstudiante();
            limiteReservas = lector.getLimiteReservas();
        } else if (usuario instanceof Bibliotecario) {
            rol = "BIBLIOTECARIO";
        }

        return new AuthResponse(
                usuario.getIdUsuario(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                rol,
                codigoEstudiante,
                limiteReservas
        );
    }
}
