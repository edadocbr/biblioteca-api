package com.biblioteca.repository;

import com.biblioteca.entity.Reserva;
import com.biblioteca.entity.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByLectorIdUsuario(Long idLector);
    List<Reserva> findByEjemplarCodigoBarraAndEstado(String codigoBarra, EstadoReserva estado);
    long countByLectorIdUsuarioAndEstado(Long idLector, EstadoReserva estado);
}
