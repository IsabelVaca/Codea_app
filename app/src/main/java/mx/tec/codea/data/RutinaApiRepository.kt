package mx.tec.codea.data

import kotlinx.serialization.builtins.ListSerializer
import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.offline.cachedResponse
import mx.tec.codea.data.offline.fetchAndCache
import mx.tec.codea.data.api.TemplateAssignBody
import mx.tec.codea.data.api.TemplateBody
import mx.tec.codea.data.api.TemplateDto
import mx.tec.codea.data.api.TemplateMomentBody
import mx.tec.codea.domain.MomentoRutina
import mx.tec.codea.domain.RutinaItem
import mx.tec.codea.domain.registroDeCategoria

class RutinaApiRepository {

    private val api get() = ApiClient.api

    // las rutinas y, por separado, los momentos de cada una (por id de rutina).
    suspend fun getAll(): Pair<List<RutinaItem>, Map<String, List<MomentoRutina>>> =
        mapear(fetchAndCache(CACHE, SERIALIZER) { api.routineTemplates() })

    // lo último que se guardó, o null (ver ApiCache).
    suspend fun cached(): Pair<List<RutinaItem>, Map<String, List<MomentoRutina>>>? =
        cachedResponse(CACHE, SERIALIZER)?.let { mapear(it) }

    private fun mapear(plantillas: List<TemplateDto>) =
        plantillas.map { it.toItem() } to plantillas.associate { it.id.toString() to it.toMomentos() }

    private companion object {
        const val CACHE = "routine-templates"
        val SERIALIZER = ListSerializer(TemplateDto.serializer())
    }

    suspend fun crear(nombre: String, momentos: List<MomentoRutina>) {
        api.createRoutineTemplate(body(nombre.trim(), momentos, esPredeterminada = false))
    }

    // manda la plantilla entera: la API reemplaza sus momentos por esta lista, en este orden.
    suspend fun guardar(rutina: RutinaItem, momentos: List<MomentoRutina>) {
        api.updateRoutineTemplate(rutina.id.toInt(), body(rutina.nombre, momentos, rutina.esPredeterminada))
    }

    // asignar = false deja a la sala con la plantilla predeterminada.
    suspend fun asignarASala(rutinaId: String, salaId: String, asignar: Boolean) {
        api.assignRoutineTemplate(
            salaId.toInt(),
            TemplateAssignBody(templateId = if (asignar) rutinaId.toInt() else null)
        )
    }

    private fun body(nombre: String, momentos: List<MomentoRutina>, esPredeterminada: Boolean) = TemplateBody(
        name = nombre,
        moments = momentos.map { TemplateMomentBody(name = it.nombre, category = it.categoria) },
        isDefault = esPredeterminada,
    )
}

private fun TemplateDto.toItem() = RutinaItem(
    id = id.toString(),
    nombre = name,
    cantidadMomentos = moments.size,
    esPredeterminada = isDefault,
    salasIds = classrooms.map { it.id.toString() },
)

// el id de cada momento solo tiene que ser único dentro de la rutina (la lista arrastrable lo usa de llave).
private fun TemplateDto.toMomentos() = moments.sortedBy { it.number }.map {
    MomentoRutina(
        id = "$id-${it.number}",
        nombre = it.name,
        registro = registroDeCategoria(it.category),
        categoria = it.category,
    )
}
