package com.museodelosmuseos.museo_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.museodelosmuseos.museo_backend.exception.RecursoNoEncontradoException;
import com.museodelosmuseos.museo_backend.model.NotificacionModel;
import com.museodelosmuseos.museo_backend.repository.NotificacionRepository;
import com.museodelosmuseos.museo_backend.repository.UsuarioRepository;

@Service
public class NotificacionService {

    private final NotificacionRepository repository;
    private final UsuarioRepository usuarios;

    public NotificacionService(NotificacionRepository repository, UsuarioRepository usuarios) {
        this.repository = repository;
        this.usuarios = usuarios;
    }

    public List<NotificacionModel> listar(Long usuarioId) {
        if (usuarioId == null) {
            return repository.findAll();
        }
        return repository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public NotificacionModel obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación " + id + " no encontrada"));
    }

    public NotificacionModel crear(NotificacionModel nueva) {
        verificarUsuario(nueva.getUsuarioId());
        nueva.setId(null);                              // el id lo genera la base
        nueva.setFecha(System.currentTimeMillis());
        nueva.setLeida(false);                          // toda notificación nace sin leer
        return repository.save(nueva);
    }

    public NotificacionModel actualizar(Long id, NotificacionModel datos) {
        NotificacionModel existente = obtener(id);      // 404 si no existe
        verificarUsuario(datos.getUsuarioId());         // 404 si el usuario no existe
        existente.setUsuarioId(datos.getUsuarioId());
        existente.setTipo(datos.getTipo());
        existente.setTitulo(datos.getTitulo());
        existente.setMensaje(datos.getMensaje());
        existente.setLeida(datos.isLeida());
        return repository.save(existente);
    }

    public NotificacionModel marcarLeida(Long id) {
        NotificacionModel existente = obtener(id);
        existente.setLeida(true);
        return repository.save(existente);
    }

    public void eliminar(Long id) {
        repository.delete(obtener(id));
    }

    private void verificarUsuario(Long usuarioId) {
        if (!usuarios.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("Usuario " + usuarioId + " no encontrado");
        }
    }
}