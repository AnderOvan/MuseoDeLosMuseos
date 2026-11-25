package com.museodelosmuseos.museo_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.museodelosmuseos.museo_backend.exception.ConflictoException;
import com.museodelosmuseos.museo_backend.exception.RecursoNoEncontradoException;
import com.museodelosmuseos.museo_backend.model.MuseoModel;
import com.museodelosmuseos.museo_backend.repository.MuseoRepository;

@Service
public class MuseoService {

    private final MuseoRepository repository;

    public MuseoService(MuseoRepository repository) {
        this.repository = repository;
    }

    public List<MuseoModel> listar(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return repository.findAll();
        }
        return repository.findByCategoriaIgnoreCase(categoria);
    }

    public MuseoModel obtener(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Museo " + id + " no encontrado"));
    }

    public MuseoModel crear(MuseoModel nuevo) {
        if (repository.existsById(nuevo.getId())) {
            throw new ConflictoException("Ya existe un museo con id " + nuevo.getId());
        }
        return repository.save(nuevo);
    }

    public MuseoModel actualizar(String id, MuseoModel datos) {
        obtener(id);          // si no existe, lanza el error 404
        datos.setId(id);      // el id manda la URL, no el JSON
        return repository.save(datos);
    }

    public void eliminar(String id) {
        repository.delete(obtener(id));
    }
}