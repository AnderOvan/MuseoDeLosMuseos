package com.museodelosmuseos.museo_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.museodelosmuseos.museo_backend.model.ProductoModel;
import com.museodelosmuseos.museo_backend.service.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductoModel> listar(@RequestParam(required = false) String categoria) {
        return service.listar(categoria);
    }

    @GetMapping("/{id}")
    public ProductoModel obtener(@PathVariable String id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoModel crear(@Valid @RequestBody ProductoModel nuevo) {
        return service.crear(nuevo);
    }

    @PutMapping("/{id}")
    public ProductoModel actualizar(@PathVariable String id, @Valid @RequestBody ProductoModel datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}