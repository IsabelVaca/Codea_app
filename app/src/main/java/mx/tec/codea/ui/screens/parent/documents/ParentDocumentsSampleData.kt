package mx.tec.codea.ui.screens.parent.documents

enum class DocumentStatus {
    CURRENT,
    MISSING,
}

data class RequestedDocument(
    val id: String,
    val title: String,
    val detail: String? = null,
    val status: DocumentStatus,
    val actionLabel: String,
)

data class AuthorizedPerson(
    val id: String,
    val name: String,
)

object ParentDocumentsSampleData {
    const val childName = "Mateo"

    val documents = listOf(
        RequestedDocument(
            id = "allergy-certificate",
            title = "Certificado de alergias",
            detail = "Subido el 20 de septiembre",
            status = DocumentStatus.CURRENT,
            actionLabel = "Reemplazar documento",
        ),
        RequestedDocument(
            id = "disability-report",
            title = "Informe de discapacidad",
            detail = "Se solicita para el día 9 de octubre",
            status = DocumentStatus.MISSING,
            actionLabel = "Subir documento",
        ),
    )

    val pickupPermission = RequestedDocument(
        id = "pickup-permission",
        title = "Permisos para recoger",
        status = DocumentStatus.CURRENT,
        actionLabel = "Agregar autorizado",
    )

    val authorizedPeople = listOf(
        AuthorizedPerson(
            id = "andrea-torres",
            name = "Andrea Torres",
        ),
        AuthorizedPerson(
            id = "luis-torres",
            name = "Luis Torres",
        ),
    )
}
