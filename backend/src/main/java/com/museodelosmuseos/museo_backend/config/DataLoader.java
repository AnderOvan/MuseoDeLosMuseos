package com.museodelosmuseos.museo_backend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.museodelosmuseos.museo_backend.model.MuseoModel;
import com.museodelosmuseos.museo_backend.model.ProductoModel;
import com.museodelosmuseos.museo_backend.model.Rol;
import com.museodelosmuseos.museo_backend.model.UsuarioModel;
import com.museodelosmuseos.museo_backend.repository.MuseoRepository;
import com.museodelosmuseos.museo_backend.repository.ProductoRepository;
import com.museodelosmuseos.museo_backend.repository.UsuarioRepository;

/** Carga datos iniciales al arrancar. Solo agrega lo que falta, nunca duplica. */
@Component
public class DataLoader implements CommandLineRunner {

    private final MuseoRepository museos;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public DataLoader(MuseoRepository museos, ProductoRepository productos,
                      UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.museos = museos;
        this.productos = productos;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        cargarMuseos();
        cargarProductos();
        cargarAdministrador();
    }

    private void cargarMuseos() {
        List<MuseoModel> lista = List.of(
                new MuseoModel("MU01", "Museo Nacional de Bellas Artes", "Santiago", "Chile", -33.4344, -70.6438, "Principal museo de artes visuales de Chile, en el Parque Forestal.", "Arte", "Arte Latinoamericano", "https://www.mnba.gob.cl", 4.7, 310),
                new MuseoModel("MU02", "Museo Chileno de Arte Precolombino", "Santiago", "Chile", -33.4389, -70.6519, "Arte de los pueblos originarios de América, a pasos de la Plaza de Armas.", "Historia", "Patrimonio Precolombino", "https://www.precolombino.cl", 4.8, 540),
                new MuseoModel("MU03", "Museo Nacional de Historia Natural", "Santiago", "Chile", -33.4413, -70.6823, "Ciencias naturales y antropología, dentro del Parque Quinta Normal.", "Ciencia", "Ciencia y Naturaleza", "https://www.mnhn.gob.cl", 4.6, 380),
                new MuseoModel("MU04", "Museo Larco", "Lima", "Perú", -12.0727, -77.071, "Arte precolombino peruano con más de 5.000 años de historia.", "Historia", "Patrimonio Precolombino", "https://www.museolarco.org", 4.8, 650),
                new MuseoModel("MU05", "MALBA", "Buenos Aires", "Argentina", -34.577, -58.4033, "Museo de Arte Latinoamericano de Buenos Aires.", "Arte", "Arte Latinoamericano", "https://www.malba.org.ar", 4.6, 480),
                new MuseoModel("MU06", "Museo del Oro", "Bogotá", "Colombia", 4.6019, -74.072, "La mayor colección de orfebrería prehispánica del mundo.", "Historia", "Patrimonio Precolombino", "https://www.banrepcultural.org/bogota/museo-del-oro", 4.8, 900),
                new MuseoModel("MU07", "Museo Nacional de Arte Colonial", "Bogotá", "Colombia", 4.5968, -74.0745, "Pintura, escultura y platería del período colonial en Colombia.", "Historia", "Arte Colonial", "https://www.museocolonial.gov.co", 4.8, 120),
                new MuseoModel("MU08", "MASP", "São Paulo", "Brasil", -23.5614, -46.6558, "Museo de Arte de São Paulo, ícono de la arquitectura moderna.", "Arte", "Arte Latinoamericano", "https://masp.org.br", 4.7, 750),
                new MuseoModel("MU09", "Museo Frida Kahlo", "Ciudad de México", "México", 19.3551, -99.1625, "La Casa Azul, hogar y obra de Frida Kahlo.", "Arte", "Arte Latinoamericano", "https://www.museofridakahlo.org.mx", 4.7, 860),
                new MuseoModel("MU10", "Museo Nacional de Antropología", "Ciudad de México", "México", 19.426, -99.1863, "La colección más grande de arte precolombino mesoamericano.", "Historia", "Patrimonio Precolombino", "https://www.mna.inah.gob.mx", 4.9, 1500),
                new MuseoModel("MU11", "Museo del Prado", "Madrid", "España", 40.4138, -3.6921, "Una de las pinacotecas más importantes del mundo.", "Arte", "Arte Colonial", "https://www.museodelprado.es", 4.9, 1200),
                new MuseoModel("MU12", "Museo de Ciencias Naturales de La Plata", "La Plata", "Argentina", -34.9093, -57.9322, "Paleontología, zoología y antropología en el Paseo del Bosque.", "Ciencia", "Ciencia y Naturaleza", "https://www.museo.fcnym.unlp.edu.ar", 4.7, 410));

        for (MuseoModel m : lista) {
            if (!museos.existsById(m.getId())) {
                museos.save(m);
            }
        }
    }

    private void cargarProductos() {
        List<ProductoModel> lista = List.of(
                new ProductoModel("C001", "Curso de Curaduría Virtual", "Aprende a diseñar y montar exposiciones en línea, paso a paso.", 49.0, null, "Curso", 999),
                new ProductoModel("C002", "Curso Museos y Comunidad", "Estrategias para acercar los museos a su comunidad.", 39.0, null, "Curso", 999),
                new ProductoModel("C003", "Curso de Restauración Virtual", "Técnicas digitales para documentar y restaurar piezas patrimoniales.", 55.0, null, "Curso", 999),
                new ProductoModel("C004", "Taller de Arte Digital", "Taller práctico de ilustración y modelado de obras en 3D.", 29.0, null, "Curso", 999),
                new ProductoModel("L001", "Historia del Arte Iberoamericano", "Recorrido ilustrado por el arte de la región, desde lo precolombino a lo contemporáneo.", 35.0, null, "Libro", 12),
                new ProductoModel("L002", "Libro de Conservación Preventiva", "Manual para cuidar colecciones: luz, humedad, temperatura y almacenaje.", 42.0, null, "Libro", 8),
                new ProductoModel("L003", "Guía de Museos de Chile", "Guía con los principales museos de Chile, sus colecciones y horarios.", 22.5, null, "Libro", 20),
                new ProductoModel("M001", "Kit de Conservación de Documentos", "Guantes, cajas libres de ácido y papel de archivo para proteger documentos.", 120.0, null, "Material", 5),
                new ProductoModel("M002", "Réplica de Cerámica Precolombina", "Réplica artesanal de una vasija precolombina, hecha a mano.", 68.0, null, "Material", 3),
                new ProductoModel("M003", "Taza Conmemorativa", "Taza de cerámica con ilustraciones de piezas patrimoniales.", 12.0, null, "Material", 0));

        for (ProductoModel p : lista) {
            if (!productos.existsById(p.getId())) {
                productos.save(p);
            }
        }
    }

    private void cargarAdministrador() {
        crearSiNoExiste("Administrador", "99999999-9", "admin@museo.cl", "Admin123", Rol.ADMINISTRADOR);
        crearSiNoExiste("Investigadora Museo", "88888888-8", "investigador@museo.cl", "Invest123", Rol.INVESTIGADOR);
    }

    private void crearSiNoExiste(String nombre, String rut, String correo, String clave, Rol rol) {
        if (usuarios.existsByCorreo(correo)) {
            return;
        }
        UsuarioModel u = new UsuarioModel();
        u.setNombre(nombre);
        u.setRut(rut);
        u.setCorreo(correo);
        u.setClave(encoder.encode(clave));
        u.setDespacho("Santiago, Chile");
        u.setRol(rol);
        usuarios.save(u);
    }
}