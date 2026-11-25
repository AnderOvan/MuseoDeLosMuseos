package com.museodelosmuseos.museo_backend.dto;

import com.museodelosmuseos.museo_backend.model.EstadoPedido;

import jakarta.validation.constraints.NotNull;

public record EstadoPedidoRequest(
        @NotNull(message = "El estado es obligatorio") EstadoPedido estado) {
}