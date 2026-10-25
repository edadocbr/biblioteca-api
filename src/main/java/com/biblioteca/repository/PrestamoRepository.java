package com.biblioteca.repository;

import com.biblioteca.entity.Prestamo;
import com.biblioteca.entity.enums.EstadoPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    List<Prestamo> findByLectorIdUsuario(Long idLector);
    List<Prestamo> findByLectorIdUsuarioAndEstado(Long idLector, EstadoPrestamo estado);
    List<Prestamo> findByEjemplarCodigoBarraAndEstado(String codigoBarra, EstadoPrestamo estado);
    long countByLectorIdUsuarioAndEstado(Long idLector, EstadoPrestamo estado);

    @Query("SELECT p FROM Prestamo p WHERE (p.ejemplar.codigoBarra = :codigo OR p.ejemplar.tagRfid = :codigo) AND p.fechaDevolucionReal IS NULL AND p.estado IN (com.biblioteca.entity.enums.EstadoPrestamo.ACTIVO, com.biblioteca.entity.enums.EstadoPrestamo.RENOVADO, com.biblioteca.entity.enums.EstadoPrestamo.VENCIDO)")
    List<Prestamo> findActivosByCodigoEjemplar(@Param("codigo") String codigo);

    List<Prestamo> findByFechaDevolucionRealIsNotNullOrderByFechaDevolucionRealDesc();
}
