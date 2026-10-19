package mx.tec.codea.domain

// lo que la familia cuenta de la salud de su hijo en "Mi hijo". la maestra lo lee en la ficha del niño.
// los campos van como texto: "" es que no hay nada que decir.
data class ChildHealth(
    val allergies: String = "",
    val medicalConditions: String = "",
    val intolerances: String = "",
    val medications: String = "",
    val otherNotes: String = "",
) {
    val isEmpty: Boolean
        get() = listOf(allergies, medicalConditions, intolerances, medications, otherNotes).all { it.isBlank() }
}

// lo que la pantalla "Mi hijo" muestra del niño, además de sus documentos.
data class MyChild(
    val fullName: String,
    // "20 de enero de 2022"; null si no se conoce.
    val birthDateText: String?,
    // "2 años 4 meses"; null si no se conoce la fecha de nacimiento.
    val ageText: String?,
    val classroomName: String?,
    val guardians: List<Contact>,
    val health: ChildHealth,
)

object ChildHealthRules {

    // la misma cifra que la API (422 TEXT_TOO_LONG): cada dato puede tener hasta 1000 caracteres.
    const val MAX_LENGTH = 1000

    fun isTooLong(text: String): Boolean = text.trim().length > MAX_LENGTH

    fun isValid(health: ChildHealth): Boolean =
        listOf(health.allergies, health.medicalConditions, health.intolerances, health.medications, health.otherNotes)
            .none(::isTooLong)

    // lo que se manda a guardar: sin espacios en las orillas.
    fun clean(health: ChildHealth) = ChildHealth(
        allergies = health.allergies.trim(),
        medicalConditions = health.medicalConditions.trim(),
        intolerances = health.intolerances.trim(),
        medications = health.medications.trim(),
        otherNotes = health.otherNotes.trim(),
    )

    // "Guardar" solo se enciende cuando lo escrito es distinto de lo guardado y cabe en la API.
    fun canSave(saved: ChildHealth, edited: ChildHealth): Boolean =
        isValid(edited) && clean(saved) != clean(edited)
}
