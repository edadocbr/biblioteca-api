package com.biblioteca.repository;

import com.biblioteca.entity.Sancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SancionRepository extends JpaRepository<Sancion, Long> {
    List<Sancion> findByLectorIdUsuario(Long idLector);
    List<Sancion> findByLectorIdUsuarioAndPagadoFalse(Long idLector);
    boolean existsByLectorIdUsuarioAndPagadoFalse(Long idLector);
}
