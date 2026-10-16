package mx.tec.codea.domain

data class MomentoCatalogo(
    val nombre: String,
    val registro: String,
    val categoria: String = categoriaDeNombre(nombre),
)
