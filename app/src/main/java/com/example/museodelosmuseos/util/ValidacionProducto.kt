package com.example.museodelosmuseos.util

// Campos del formulario de producto (para mostrar el error debajo de cada uno)
enum class CampoProducto { ID, TITULO, DESCRIPCION, PRECIO, STOCK }

// Reglas del formulario del Administrador. Es lógica pura (sin Android),
// por eso se puede probar con pruebas unitarias.
object ValidacionProducto {

    val CATEGORIAS = listOf("Curso", "Libro", "Material")

    // Acepta "35", "35.5" o "35,5"
    fun leerPrecio(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

    fun leerStock(texto: String): Int? = texto.trim().toIntOrNull()

    // Devuelve un mapa campo -> mensaje. Vacío = formulario válido.
    fun validar(
        id: String,
        titulo: String,
        descripcion: String,
        precioTexto: String,
        stockTexto: String,
        esNuevo: Boolean,
        idsExistentes: Set<String>
    ): Map<CampoProducto, String> {
        val errores = mutableMapOf<CampoProducto, String>()
        val codigo = id.trim().uppercase()

        when {
            codigo.isEmpty() -> errores[CampoProducto.ID] = "Ingresa un código (ej: L004)"
            !codigo.matches(Regex("[A-Z][0-9]{3}")) ->
                errores[CampoProducto.ID] = "Usa una letra y 3 números (ej: L004)"
            esNuevo && codigo in idsExistentes -> errores[CampoProducto.ID] = "Ya existe un producto con ese código"
        }

        if (titulo.trim().length < 3) errores[CampoProducto.TITULO] = "El título debe tener al menos 3 caracteres"
        if (descripcion.isBlank()) errores[CampoProducto.DESCRIPCION] = "Ingresa una descripción"

        val precio = leerPrecio(precioTexto)
        if (precio == null || precio <= 0.0) errores[CampoProducto.PRECIO] = "Ingresa un precio mayor a 0"

        val stock = leerStock(stockTexto)
        if (stock == null || stock < 0) errores[CampoProducto.STOCK] = "Ingresa un stock entero (0 o más)"

        return errores
    }
}