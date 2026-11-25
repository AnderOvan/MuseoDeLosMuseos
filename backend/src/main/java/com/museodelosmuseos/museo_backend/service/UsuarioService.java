@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(this::aRespuesta).toList();
    }

    public UsuarioResponse obtener(Long id) { return aRespuesta(buscar(id)); }

    public UsuarioResponse crear(UsuarioRequest req) {
        if (req.contrasena() == null || req.contrasena().isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria");   // 400
        if (repository.existsByCorreo(req.correo()))
            throw new ConflictoException("El correo ya está registrado");          // 409
        if (repository.existsByRut(req.rut()))
            throw new ConflictoException("El RUT ya está registrado");             // 409
        UsuarioModel u = new UsuarioModel();
        copiarDatos(u, req);
        u.setClave(encoder.encode(req.contrasena()));   // se guarda el HASH
        return aRespuesta(repository.save(u));
    }

    public UsuarioResponse actualizar(Long id, UsuarioRequest req) {
        UsuarioModel u = buscar(id);                     // 404 si no existe
        copiarDatos(u, req);
        if (req.contrasena() != null && !req.contrasena().isBlank())
            u.setClave(encoder.encode(req.contrasena()));
        return aRespuesta(repository.save(u));
    }

    public void eliminar(Long id) { repository.delete(buscar(id)); }

    public UsuarioResponse login(LoginRequest req) {
        UsuarioModel u = repository.findByCorreo(req.correo())
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos"));
        if (!encoder.matches(req.contrasena(), u.getClave()))
            throw new CredencialesInvalidasException("Correo o contraseña incorrectos");
        return aRespuesta(u);
    }

    private UsuarioModel buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario " + id + " no encontrado"));
    }

    private void copiarDatos(UsuarioModel u, UsuarioRequest req) {
        u.setNombre(req.nombre());
        u.setRut(req.rut());
        u.setCorreo(req.correo());
        u.setDespacho(req.despacho() == null ? "" : req.despacho());
        u.setRol(req.rol() == null ? Rol.CLIENTE : req.rol());
        u.setAvatar(req.avatar() == null ? 0 : req.avatar());
    }

    private UsuarioResponse aRespuesta(UsuarioModel u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getRut(), u.getCorreo(),
                u.getDespacho(), u.getRol(), u.getAvatar(), u.getFechaRegistro());
    }
}