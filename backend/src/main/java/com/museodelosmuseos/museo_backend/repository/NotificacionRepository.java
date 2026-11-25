package com.museodelosmuseos.museo_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.museodelosmuseos.museo_backend.model.NotificacionModel;

public interface NotificacionRepository extends JpaRepository<NotificacionModel, Long> {

    List<NotificacionModel> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}