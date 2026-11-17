package com.logistica.ms_logistica.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.logistica.ms_logistica.domain.Envio;
import com.logistica.ms_logistica.domain.EstadoEnvio;

import jakarta.persistence.LockModeType;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Envio e where e.id = :id")
    Optional<Envio> buscarParaActualizar(@Param("id") Long id);

    @Query("""
            select distinct e
            from Envio e
            left join fetch e.asignacion a
            left join fetch e.ruta r
            left join fetch r.paradas p
            left join fetch p.envioPaquete ep
            where e.id = :id
            """)
    Optional<Envio> buscarConRuta(@Param("id") Long id);

    @Query("""
            select distinct e
            from Envio e
            left join fetch e.paquetes p
            left join fetch e.asignacion a
            where (:estado is null or e.estado = :estado)
              and (:sector is null or upper(e.sector) = upper(:sector))
            order by e.fechaCreacion desc
            """)
    List<Envio> buscarConFiltros(
            @Param("estado") EstadoEnvio estado,
            @Param("sector") String sector
    );

    @Query("""
            select distinct e
            from Envio e
            left join fetch e.paquetes p
            left join fetch e.asignacion a
            left join fetch e.ruta r
            where e.id = :id
            """)
    Optional<Envio> buscarParaConsulta(@Param("id") Long id);

    @Query("""
            select distinct e
            from Envio e
            left join fetch e.paquetes p
            left join fetch e.asignacion a
            left join fetch e.ruta r
            where e.id = :id
            """)
    Optional<Envio> buscarContextoReparto(@Param("id") Long id);

    @Query("""
            select distinct e
            from Envio e
            join fetch e.asignacion a
            join fetch e.ruta r
            left join fetch r.paradas p
            left join fetch p.envioPaquete ep
            where a.repartidorSub = :repartidorSub
              and e.estado in :estados
            """)
    List<Envio> buscarRutasDelRepartidor(
            @Param("repartidorSub") String repartidorSub,
            @Param("estados") List<EstadoEnvio> estados
    );
}
