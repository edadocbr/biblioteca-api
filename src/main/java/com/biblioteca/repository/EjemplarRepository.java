package com.biblioteca.repository;

import com.biblioteca.entity.Ejemplar;
import com.biblioteca.entity.enums.EstadoEjemplar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository 
public interface EjemplarRepository extends JpaRepository<Ejemplar, String> {
    Optional<Ejemplar> findByTagRfid(String tagRfid);
    List<Ejemplar> findByLibroIsbn(String isbn);
    List<Ejemplar> findByEstado(EstadoEjemplar estado);
    long countByLibroIsbnAndEstado(String isbn, EstadoEjemplar estado);
}
