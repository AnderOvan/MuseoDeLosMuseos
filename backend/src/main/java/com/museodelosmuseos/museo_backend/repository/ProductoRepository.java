package com.museodelosmuseos.museo_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.museodelosmuseos.museo_backend.model.ProductoModel;

public interface ProductoRepository extends JpaRepository<ProductoModel, String> {

    List<ProductoModel> findByCategoriaIgnoreCase(String categoria);
}