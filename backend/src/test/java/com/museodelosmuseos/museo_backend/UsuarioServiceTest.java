package com.museodelosmuseos.museo_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.museodelosmuseos.museo_backend.dto.LoginRequest;
import com.museodelosmuseos.museo_backend.dto.UsuarioRequest;
import com.museodelosmuseos.museo_backend.dto.UsuarioResponse;
import com.museodelosmuseos.museo_backend.exception.ConflictoException;
import com.museodelosmuseos.museo_backend.exception.CredencialesInvalidasException;
import com.museodelosmuseos.museo_backend.exception.RecursoNoEncontradoException;
import com.museodelosmuseos.museo_backend.model.Rol;
import com.museodelosmuseos.museo_backend.model.UsuarioModel;
import com.museodelosmuseos.museo_backend.repository.UsuarioRepository;
import com.museodelosmuseos.museo_backend.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UsuarioService service;

    @Test
    void registrarConCorreoDuplicadoLanzaConflicto() {
        UsuarioRequest req = new UsuarioRequest("Ana", "11111111-1", "ana@mail.cl", "secreto1", "Calle 1", null, 0);
        when(repository.existsByCorreo("ana@mail.cl")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.crear(req));
        verify(repository, never()).save(any());
    }

    @Test
    void registrarConRutDuplicadoLanzaConflicto() {
        UsuarioRequest req = new UsuarioRequest("Ana", "11111111-1", "ana@mail.cl", "secreto1", "Calle 1", null, 0);
        when(repository.existsByRut("11111111-1")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.crear(req));
        verify(repository, never()).save(any());
    }

    @Test
    void registrarGuardaLaContrasenaEncriptada() {
        UsuarioRequest req = new UsuarioRequest("Ana", "11111111-1", "ana@mail.cl", "secreto1", "Calle 1", null, 0);
        when(encoder.encode("secreto1")).thenReturn("HASH");
        when(repository.save(any(UsuarioModel.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse res = service.crear(req);

        ArgumentCaptor<UsuarioModel> captor = ArgumentCaptor.forClass(UsuarioModel.class);
        verify(repository).save(captor.capture());
        assertEquals("HASH", captor.getValue().getClave());
        assertNotEquals("secreto1", captor.getValue().getClave());
        assertEquals(Rol.CLIENTE, res.rol());     // sin rol en la petición, queda como CLIENTE
    }

    @Test
    void obtenerUsuarioInexistenteLanzaNoEncontrado() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(999L));
    }

    @Test
    void loginCorrectoDevuelveUsuarioSinContrasena() {
        UsuarioModel u = new UsuarioModel();
        u.setId(1L);
        u.setNombre("Ana");
        u.setCorreo("ana@mail.cl");
        u.setClave("HASH");
        u.setRol(Rol.CLIENTE);
        when(repository.findByCorreo("ana@mail.cl")).thenReturn(Optional.of(u));
        when(encoder.matches("secreto1", "HASH")).thenReturn(true);

        UsuarioResponse res = service.login(new LoginRequest("ana@mail.cl", "secreto1"));

        assertEquals("Ana", res.nombre());
        assertEquals(Rol.CLIENTE, res.rol());
    }

    @Test
    void loginConContrasenaIncorrectaLanzaCredencialesInvalidas() {
        UsuarioModel u = new UsuarioModel();
        u.setClave("HASH");
        when(repository.findByCorreo("ana@mail.cl")).thenReturn(Optional.of(u));
        when(encoder.matches("mala", "HASH")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("ana@mail.cl", "mala")));
    }

    @Test
    void loginConCorreoInexistenteLanzaCredencialesInvalidas() {
        when(repository.findByCorreo("nadie@mail.cl")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("nadie@mail.cl", "secreto1")));
    }
}