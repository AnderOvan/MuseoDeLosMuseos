package com.museodelosmuseos.museo_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.museodelosmuseos.museo_backend.exception.RecursoNoEncontradoException;
import com.museodelosmuseos.museo_backend.model.EstadoPedido;
import com.museodelosmuseos.museo_backend.model.PedidoModel;
import com.museodelosmuseos.museo_backend.repository.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository repository;

    public PedidoService(PedidoRepository repository) {
        this.repository = repository;
    }

    public List<PedidoModel> listar(Long usuarioId) {
        if (usuarioId == null) {
            return repository.findAll();
        }
        return repository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public PedidoModel obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido " + id + " no encontrado"));
    }

    @Transactional
    public PedidoModel crear(PedidoModel nuevo) {
        nuevo.setId(null);                              // el id lo genera la base
        nuevo.setFecha(System.currentTimeMillis());
        nuevo.setEstado(EstadoPedido.PENDIENTE);        // todo pedido nace PENDIENTE
        PedidoModel guardado = repository.save(nuevo);
        guardado.setNumero(String.format("MDM-%05d", guardado.getId()));
        return repository.save(guardado);
    }

    public PedidoModel actualizar(Long id, PedidoModel datos) {
        PedidoModel existente = obtener(id);            // 404 si no existe
        existente.setUsuarioId(datos.getUsuarioId());
        existente.setClienteNombre(datos.getClienteNombre());
        existente.setResumen(datos.getResumen());
        existente.setTotal(datos.getTotal());
        existente.setDestino(datos.getDestino());
        existente.setTipoEnvio(datos.getTipoEnvio());
        if (datos.getEstado() != null) {
            existente.setEstado(datos.getEstado());
        }
        return repository.save(existente);
    }

    public PedidoModel cambiarEstado(Long id, EstadoPedido estado) {
        PedidoModel existente = obtener(id);
        existente.setEstado(estado);
        return repository.save(existente);
    }

    public void eliminar(Long id) {
        repository.delete(obtener(id));
    }
}