package com.example.museodelosmuseos.model

// Estados por los que pasa una solicitud de compra (los actualiza el Administrador)
enum class EstadoPedido(val titulo: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    ENVIADO("Enviado"),
    ENTREGADO("Entregado")
}