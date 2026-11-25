package com.museodelosmuseos.museo_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.museodelosmuseos.museo_backend.dto.EstadoPedidoRequest;
import com.museodelosmuseos.museo_backend.model.PedidoModel;
import com.museodelosmuseos.museo_backend.service.PedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PedidoModel> listar(@RequestParam(required = false) Long usuarioId) {
        return service.listar(usuarioId);
    }

    @GetMapping("/{id}")
    public PedidoModel obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoModel crear(@Valid @RequestBody PedidoModel nuevo) {
        return service.crear(nuevo);
    }

    @PutMapping("/{id}")
    public PedidoModel actualizar(@PathVariable Long id, @Valid @RequestBody PedidoModel datos) {
        return service.actualizar(id, datos);
    }

    @PatchMapping("/{id}/estado")
    public PedidoModel cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoPedidoRequest req) {
        return service.cambiarEstado(id, req.estado());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}