package com.museodelosmuseos.museo_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.museodelosmuseos.museo_backend.exception.ConflictoException;
import com.museodelosmuseos.museo_backend.exception.RecursoNoEncontradoException;
import com.museodelosmuseos.museo_backend.model.ProductoModel;
import com.museodelosmuseos.museo_backend.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<ProductoModel> listar(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return repository.findAll();
        }
        return repository.findByCategoriaIgnoreCase(categoria);
    }

    public ProductoModel obtener(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto " + id + " no encontrado"));
    }

    public ProductoModel crear(ProductoModel nuevo) {
        if (repository.existsById(nuevo.getId())) {
            throw new ConflictoException("Ya existe un producto con id " + nuevo.getId());
        }
        return repository.save(nuevo);
    }

    public ProductoModel actualizar(String id, ProductoModel datos) {
        obtener(id);
        datos.setId(id);
        return repository.save(datos);
    }

    public void eliminar(String id) {
        repository.delete(obtener(id));
    }
}