package com.logistica.ms_paquetes;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_paquetes.domain.*;
import com.logistica.ms_paquetes.domain.estado.*;
import com.logistica.ms_paquetes.repository.PaqueteRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaqueteEstadoIntegrationTest {
    private static final List<String> ESTADOS =
            List.of("PENDIENTE", "ASIGNADO", "EN_CAMINO", "ENTREGADO", "INCIDENCIA");

    @Autowired MockMvc mvc;
    @Autowired EntityManager em;
    @Autowired PaqueteRepository repository;

    static Stream<Arguments> cambiosHttp() {
        return Stream.of("/paquetes/", "/interno/logistica/paquetes/")
                .flatMap(ruta -> ESTADOS.stream().flatMap(origen -> ESTADOS.stream()
                        .map(destino -> Arguments.of(ruta, origen, destino,
                                origen.equals(destino)
                                || origen.equals("PENDIENTE") && destino.equals("ASIGNADO")
                                || origen.equals("ASIGNADO") && destino.equals("EN_CAMINO")
                                || origen.equals("EN_CAMINO") && List.of("ENTREGADO", "INCIDENCIA").contains(destino)))));
    }

    @ParameterizedTest
    @MethodSource("cambiosHttp")
    void conservaTransicionesContratoYPersistencia(String ruta, String origen, String destino,
            boolean permitida) throws Exception {
        Long id = persistirPaquete(origen);
        String rol = ruta.startsWith("/interno") ? "ROLE_MS-LOGISTICA" : "ROLE_ADMINISTRADOR";
        var respuesta = mvc.perform(patch(ruta + id + "/estado")
                .with(jwt().authorities(new SimpleGrantedAuthority(rol)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"" + destino + "\"}"));
        respuesta.andExpect(status().is(permitida ? 200 : 422));
        if (permitida) respuesta.andExpect(jsonPath("$.estado").value(destino));
        em.flush();
        em.clear();
        String esperado = permitida ? destino : origen;
        Paquete recargado = repository.findById(id).orElseThrow();
        assertEquals(EstadoPaquete.desdeNombre(esperado).getClass(), recargado.getEstado().getClass());
        assertEquals(esperado, em.createNativeQuery("select estado from paquete where id = :id")
                .setParameter("id", id).getSingleResult());
    }

    @Test
    void reconstruyeElEstadoDeDatosExistentesYPermiteContinuar() {
        Long id = persistirPaquete("PENDIENTE");
        em.createNativeQuery("update paquete set estado = 'ASIGNADO' where id = :id")
                .setParameter("id", id).executeUpdate();
        em.clear();
        Paquete paquete = repository.findById(id).orElseThrow();
        assertInstanceOf(EstadoAsignado.class, paquete.getEstado());
        paquete.enCamino();
        em.flush();
        em.clear();
        assertInstanceOf(EstadoEnCamino.class, repository.findById(id).orElseThrow().getEstado());
    }

    @Test
    void filtraPorEstadoYSectorConLaMismaRepresentacionHttp() throws Exception {
        Long id = persistirPaquete("ASIGNADO");
        persistirPaquete("ENTREGADO");
        for (String filtro : List.of("?estado=ASIGNADO", "?estado=ASIGNADO&sector=CENTRAL")) {
            mvc.perform(get("/paquetes" + filtro)
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(id.intValue()))
                    .andExpect(jsonPath("$[0].estado").value("ASIGNADO"));
        }
        mvc.perform(get("/paquetes?estado=INEXISTENTE")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaEstadosInvalidosYConservaEl404() throws Exception {
        Long id = persistirPaquete("PENDIENTE");
        for (String cuerpo : List.of("{}", "{\"estado\":null}", "{\"estado\":\"INEXISTENTE\"}")) {
            mvc.perform(patch("/paquetes/" + id + "/estado")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                    .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(patch("/paquetes/9223372036854775807/estado")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"ASIGNADO\"}"))
                .andExpect(status().isNotFound());
    }

    private Long persistirPaquete(String estado) {
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente prueba");
        em.persist(cliente);
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre("Central prueba");
        sucursal.setDireccion("Dirección prueba");
        sucursal.setLatitud(BigDecimal.ZERO);
        sucursal.setLongitud(BigDecimal.ZERO);
        em.persist(sucursal);
        Paquete paquete = new Paquete();
        paquete.setCliente(cliente);
        paquete.setSucursal(sucursal);
        paquete.setDireccionEnvio("Destino prueba");
        paquete.setLatitud(BigDecimal.ZERO);
        paquete.setLongitud(BigDecimal.ZERO);
        paquete.setAltoCm(BigDecimal.ONE);
        paquete.setAnchoCm(BigDecimal.ONE);
        paquete.setLargoCm(BigDecimal.ONE);
        paquete.setPesoKg(BigDecimal.ONE);
        if (!estado.equals("PENDIENTE")) paquete.asignar();
        if (List.of("EN_CAMINO", "ENTREGADO", "INCIDENCIA").contains(estado)) paquete.enCamino();
        if (estado.equals("ENTREGADO")) paquete.entregar();
        if (estado.equals("INCIDENCIA")) paquete.registrarIncidencia();
        em.persist(paquete);
        em.flush();
        Long id = paquete.getId();
        em.clear();
        return id;
    }
}
