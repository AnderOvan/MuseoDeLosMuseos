public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {
    Optional<UsuarioModel> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    boolean existsByRut(String rut);
}