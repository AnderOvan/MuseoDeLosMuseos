package com.museodelosmuseos.museo_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.museodelosmuseos.museo_backend.model.UsuarioModel;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByRut(String rut);
}