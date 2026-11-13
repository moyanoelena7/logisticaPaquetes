package com.logistica.ms_paquetes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_paquetes.domain.Cliente;
import com.logistica.ms_paquetes.dto.ClienteRequest;
import com.logistica.ms_paquetes.dto.ClienteResponse;
import com.logistica.ms_paquetes.exception.RecursoNoEncontradoException;
import com.logistica.ms_paquetes.repository.ClienteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;

    @Transactional
    public ClienteResponse crear(ClienteRequest req) {
        Cliente cliente = new Cliente();
        aplicar(cliente, req);
        return ClienteResponse.from(repository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String nombre) {
        List<Cliente> clientes = (nombre == null || nombre.isBlank())
                ? repository.findAll()
                : repository.findByNombreContainingIgnoreCase(nombre.trim());
        return clientes.stream().map(ClienteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return ClienteResponse.from(buscar(id));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest req) {
        Cliente cliente = buscar(id);
        aplicar(cliente, req);
        return ClienteResponse.from(cliente);
    }

    /** Lo usará Paquete para validar el cliente que referencia. */
    @Transactional(readOnly = true)
    public Cliente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
    }

    private void aplicar(Cliente c, ClienteRequest req) {
        c.setNombre(req.nombre().trim());
        c.setTelefono(vacioANull(req.telefono()));
        c.setEmail(vacioANull(req.email()));
    }

    private String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}