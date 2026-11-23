package com.example.museodelosmuseos.repository

import com.example.museodelosmuseos.repository.local.ProductoCacheEntity
import kotlinx.coroutines.delay

// Catálogo de la tienda (Control 2: datos simulados, sin backend).
// Se precarga en la tabla "productos_cache" de Room y la pantalla lee desde ahí.
// En el Control 3, "sincronizar" traerá los datos con Retrofit en vez de esta lista.
object FakeProductoRepository {

    // categoria: "Curso", "Libro" o "Material" | stock 999 = curso digital (sin límite)
    val PRODUCTOS = listOf(
        ProductoCacheEntity("C001", "Curso de Curaduría Virtual",
            "Aprende a diseñar y montar exposiciones en línea, paso a paso.", 49.0, null, "Curso", 999),
        ProductoCacheEntity("C002", "Curso Museos y Comunidad",
            "Estrategias para acercar los museos a su comunidad.", 39.0, null, "Curso", 999),
        ProductoCacheEntity("C003", "Curso de Restauración Virtual",
            "Técnicas digitales para documentar y restaurar piezas patrimoniales.", 55.0, null, "Curso", 999),
        ProductoCacheEntity("C004", "Taller de Arte Digital",
            "Taller práctico de ilustración y modelado de obras en 3D.", 29.0, null, "Curso", 999),
        ProductoCacheEntity("L001", "Historia del Arte Iberoamericano",
            "Recorrido ilustrado por el arte de la región, desde lo precolombino a lo contemporáneo.", 35.0, null, "Libro", 12),
        ProductoCacheEntity("L002", "Libro de Conservación Preventiva",
            "Manual para cuidar colecciones: luz, humedad, temperatura y almacenaje.", 42.0, null, "Libro", 8),
        ProductoCacheEntity("L003", "Guía de Museos de Chile",
            "Guía con los principales museos de Chile, sus colecciones y horarios.", 22.5, null, "Libro", 20),
        ProductoCacheEntity("M001", "Kit de Conservación de Documentos",
            "Guantes, cajas libres de ácido y papel de archivo para proteger documentos.", 120.0, null, "Material", 5),
        ProductoCacheEntity("M002", "Réplica de Cerámica Precolombina",
            "Réplica artesanal de una vasija precolombina, hecha a mano.", 68.0, null, "Material", 3),
        ProductoCacheEntity("M003", "Taza Conmemorativa",
            "Taza de cerámica con ilustraciones de piezas patrimoniales.", 12.0, null, "Material", 0)
    )

    fun buscarPorId(id: String): ProductoCacheEntity? = PRODUCTOS.find { it.id == id }

    // Simula la sincronización con el servidor y guarda todo en la caché SQLite (Room)
    // Devuelve cuántas unidades hay disponibles (999 = curso digital).
    suspend fun consultarStock(id: String): Int {
        delay(500) // simula el tiempo de respuesta del servidor
        return buscarPorId(id)?.stock ?: 0
    }
    suspend fun sincronizar(local: LocalRepository) {
        delay(600) // simula el tiempo de respuesta de la red
        local.guardarCatalogo(PRODUCTOS)
    }
}