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

import com.museodelosmuseos.museo_backend.model.MuseoModel;
import com.museodelosmuseos.museo_backend.service.MuseoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/museos")
public class MuseoController {

    private final MuseoService service;

    public MuseoController(MuseoService service) {
        this.service = service;
    }

    @GetMapping
    public List<MuseoModel> listar(@RequestParam(required = false) String categoria) {
        return service.listar(categoria);
    }

    @GetMapping("/{id}")
    public MuseoModel obtener(@PathVariable String id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MuseoModel crear(@Valid @RequestBody MuseoModel nuevo) {
        return service.crear(nuevo);
    }

    @PutMapping("/{id}")
    public MuseoModel actualizar(@PathVariable String id, @Valid @RequestBody MuseoModel datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}