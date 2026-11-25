package com.museodelosmuseos.museo_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.museodelosmuseos.museo_backend.model.PedidoModel;

public interface PedidoRepository extends JpaRepository<PedidoModel, Long> {

    List<PedidoModel> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}