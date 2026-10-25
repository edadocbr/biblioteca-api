package com.biblioteca.repository;

import com.biblioteca.entity.Lector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LectorRepository extends JpaRepository<Lector, Long> {
    Optional<Lector> findByCodigoEstudiante(String codigoEstudiante);
    boolean existsByCodigoEstudiante(String codigoEstudiante);
}
