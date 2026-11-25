public record UsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El RUT es obligatorio") @Size(max = 20) String rut,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo inválido") String correo,
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String contrasena,
        String despacho, Rol rol, Integer avatar) {}