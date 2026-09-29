package mx.tec.codea.domain

sealed interface SalaError {
    data object NombreVacio : SalaError
    data object CupoInvalido : SalaError
}