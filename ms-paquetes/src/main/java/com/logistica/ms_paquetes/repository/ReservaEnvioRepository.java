package com.logistica.ms_paquetes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.logistica.ms_paquetes.domain.ReservaEnvio;

import jakarta.persistence.LockModeType;

public interface ReservaEnvioRepository extends JpaRepository<ReservaEnvio, Long> {

    Optional<ReservaEnvio> findByEnvioId(Long envioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReservaEnvio r where r.envioId = :envioId")
    Optional<ReservaEnvio> bloquearPorEnvioId(@Param("envioId") Long envioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct r from ReservaEnvio r join r.paquetes rp "
            + "where r.activa = true and rp.paquete.id in :paqueteIds")
    List<ReservaEnvio> bloquearReservasActivasParaPaquetes(@Param("paqueteIds") List<Long> paqueteIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReservaEnvio r where r.activa = true and r.vehiculo.id = :vehiculoId")
    List<ReservaEnvio> bloquearReservasActivasParaVehiculo(@Param("vehiculoId") Long vehiculoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReservaEnvio r where r.activa = true and r.repartidor.id = :repartidorId")
    List<ReservaEnvio> bloquearReservasActivasParaRepartidor(@Param("repartidorId") Long repartidorId);

    @Query("select distinct r.vehiculo.id from ReservaEnvio r where r.activa = true")
    List<Long> findIdsVehiculosReservados();

    @Query("select distinct r.repartidor.id from ReservaEnvio r where r.activa = true")
    List<Long> findIdsRepartidoresReservados();
}
