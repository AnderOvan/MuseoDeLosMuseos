package com.museodelosmuseos.museo_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.museodelosmuseos.museo_backend.model.MuseoModel;

public interface MuseoRepository extends JpaRepository<MuseoModel, String> {

    List<MuseoModel> findByCategoriaIgnoreCase(String categoria);
}