package com.example.museodelosmuseos.repository

import com.example.museodelosmuseos.model.Museo
import kotlinx.coroutines.delay

// Contrato de la "API REST" de museos: la app solo conoce esta interfaz
interface MuseoRepository {
    suspend fun obtenerMuseos(): Result<List<Museo>>
}

// API REST ficticia (Control 2): responde con datos locales después de un retraso,
// como lo haría un servidor. En el Control 3 se reemplaza por una versión con Retrofit.
class FakeMuseoRepository : MuseoRepository {

    override suspend fun obtenerMuseos(): Result<List<Museo>> {
        delay(700) // simula el tiempo de respuesta de la red
        return Result.success(MUSEOS)
    }

    companion object {
        fun buscarPorId(id: String): Museo? = MUSEOS.find { it.id == id }

        // El id coincide con los QR: "MUSEO_MU01" abre el Museo Nacional de Bellas Artes
        val MUSEOS = listOf(
            Museo("MU01", "Museo Nacional de Bellas Artes", "Santiago", "Chile",
                -33.4344, -70.6438, "Principal museo de artes visuales de Chile, en el Parque Forestal.",
                "Arte", "Arte Latinoamericano", "https://www.mnba.gob.cl", 4.7, 310),
            Museo("MU02", "Museo Chileno de Arte Precolombino", "Santiago", "Chile",
                -33.4389, -70.6519, "Arte de los pueblos originarios de América, a pasos de la Plaza de Armas.",
                "Historia", "Patrimonio Precolombino", "https://www.precolombino.cl", 4.8, 540),
            Museo("MU03", "Museo Nacional de Historia Natural", "Santiago", "Chile",
                -33.4413, -70.6823, "Ciencias naturales y antropología, dentro del Parque Quinta Normal.",
                "Ciencia", "Ciencia y Naturaleza", "https://www.mnhn.gob.cl", 4.6, 380),
            Museo("MU04", "Museo Larco", "Lima", "Perú",
                -12.0727, -77.0710, "Arte precolombino peruano con más de 5.000 años de historia.",
                "Historia", "Patrimonio Precolombino", "https://www.museolarco.org", 4.8, 650),
            Museo("MU05", "MALBA", "Buenos Aires", "Argentina",
                -34.5770, -58.4033, "Museo de Arte Latinoamericano de Buenos Aires.",
                "Arte", "Arte Latinoamericano", "https://www.malba.org.ar", 4.6, 480),
            Museo("MU06", "Museo del Oro", "Bogotá", "Colombia",
                4.6019, -74.0720, "La mayor colección de orfebrería prehispánica del mundo.",
                "Historia", "Patrimonio Precolombino", "https://www.banrepcultural.org/bogota/museo-del-oro", 4.8, 900),
            Museo("MU07", "Museo Nacional de Arte Colonial", "Bogotá", "Colombia",
                4.5968, -74.0745, "Pintura, escultura y platería del período colonial en Colombia.",
                "Historia", "Arte Colonial", "https://www.museocolonial.gov.co", 4.8, 120),
            Museo("MU08", "MASP", "São Paulo", "Brasil",
                -23.5614, -46.6558, "Museo de Arte de São Paulo, ícono de la arquitectura moderna.",
                "Arte", "Arte Latinoamericano", "https://masp.org.br", 4.7, 750),
            Museo("MU09", "Museo Frida Kahlo", "Ciudad de México", "México",
                19.3551, -99.1625, "La Casa Azul, hogar y obra de Frida Kahlo.",
                "Arte", "Arte Latinoamericano", "https://www.museofridakahlo.org.mx", 4.7, 860),
            Museo("MU10", "Museo Nacional de Antropología", "Ciudad de México", "México",
                19.4260, -99.1863, "La colección más grande de arte precolombino mesoamericano.",
                "Historia", "Patrimonio Precolombino", "https://www.mna.inah.gob.mx", 4.9, 1500),
            Museo("MU11", "Museo del Prado", "Madrid", "España",
                40.4138, -3.6921, "Una de las pinacotecas más importantes del mundo.",
                "Arte", "Arte Colonial", "https://www.museodelprado.es", 4.9, 1200),
            Museo("MU12", "Museo de Ciencias Naturales de La Plata", "La Plata", "Argentina",
                -34.9093, -57.9322, "Paleontología, zoología y antropología en el Paseo del Bosque.",
                "Ciencia", "Ciencia y Naturaleza", "https://www.museo.fcnym.unlp.edu.ar", 4.7, 410)
        )
    }
}