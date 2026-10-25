package com.biblioteca.repository;

import com.biblioteca.entity.Reserva;
import com.biblioteca.entity.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByLectorIdUsuario(Long idLector);
    List<Reserva> findByEjemplarCodigoBarraAndEstado(String codigoBarra, EstadoReserva estado);
    List<Reserva> findByEjemplarCodigoBarraAndEstadoIn(String codigoBarra, List<EstadoReserva> estados);
    long countByLectorIdUsuarioAndEstado(Long idLector, EstadoReserva estado);

    @Query("SELECT r FROM Reserva r LEFT JOIN FETCH r.libro WHERE r.lector.idUsuario = :idLector AND r.estado IN (com.biblioteca.entity.enums.EstadoReserva.EN_ESPERA, com.biblioteca.entity.enums.EstadoReserva.LISTA_PARA_RECOJO)")
    List<Reserva> findActivasByLector(@Param("idLector") Long idLector);

    @Query("SELECT r FROM Reserva r WHERE r.libro.isbn = :isbn AND r.estado = com.biblioteca.entity.enums.EstadoReserva.EN_ESPERA ORDER BY r.fechaSolicitud ASC")
    List<Reserva> findEsperaByLibroIsbnOrderByFechaSolicitudAsc(@Param("isbn") String isbn);

    @Query("SELECT count(r) > 0 FROM Reserva r WHERE r.lector.idUsuario = :idLector AND r.libro.isbn = :isbn AND r.estado IN (com.biblioteca.entity.enums.EstadoReserva.EN_ESPERA, com.biblioteca.entity.enums.EstadoReserva.LISTA_PARA_RECOJO)")
    boolean existsActivaByLectorAndLibro(@Param("idLector") Long idLector, @Param("isbn") String isbn);
}
