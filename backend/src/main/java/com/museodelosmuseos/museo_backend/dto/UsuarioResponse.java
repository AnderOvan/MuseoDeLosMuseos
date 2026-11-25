package com.museodelosmuseos.museo_backend.dto;

import com.museodelosmuseos.museo_backend.model.Rol;

public record UsuarioResponse(
        Long id,
        String nombre,
        String rut,
        String correo,
        String despacho,
        Rol rol,
        int avatar,
        long fechaRegistro) {
}