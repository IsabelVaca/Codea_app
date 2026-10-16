package mx.tec.codea.domain

// la API guarda cada momento con una categoría fija (ARRIVAL, MEAL, ACTIVITY, NAP, HYGIENE, PICKUP).
// la app de admin trabaja con nombres libres ("Merienda", "Patio"), así que se deduce
// la categoría del nombre. lo que no se reconoce cuenta como ACTIVITY.
fun categoriaDeNombre(nombre: String): String {
    val n = nombre.lowercase()
    return when {
        "llegada" in n || "bienvenida" in n -> "ARRIVAL"
        "desayuno" in n || "almuerzo" in n || "comida" in n || "merienda" in n -> "MEAL"
        "siesta" in n -> "NAP"
        "higiene" in n || "pañal" in n || "panal" in n -> "HYGIENE"
        "salida" in n || "recoger" in n -> "PICKUP"
        else -> "ACTIVITY"
    }
}

// lo que se le pide a la asistente registrar en cada tipo de momento (se muestra en el detalle).
fun registroDeCategoria(categoria: String): String = when (categoria) {
    "ARRIVAL" -> "Hora de llegada"
    "MEAL" -> "Qué comió"
    "NAP" -> "Hora inicio y fin"
    "HYGIENE" -> "Hora"
    "PICKUP" -> "Quién lo recogió"
    else -> "Foto + nota"
}
