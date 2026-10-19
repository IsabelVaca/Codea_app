package mx.tec.codea.domain

// los filtros de la lista de Mi sala. cada uno cuenta solo a los niños que están hoy.
enum class RoomFilter { ALL, WITH_NOTE, WITH_CHAT, NO_SUMMARY }

// un aviso de casa junto al niño que lo recibió, para listarlos sin recorrer a todos los niños.
data class RoomNotice(val child: Child, val note: FamilyNote)

// las reglas de Mi sala: qué se atiende primero, qué muestra cada filtro y cuántos hay.
// "seen" son los avisos que la docente ya marcó como vistos en este teléfono.
object RoomRules {

    // tope de avisos que se leen directo en Mi sala; el resto se abre en "Avisos de casa".
    const val NOTICES_ON_HOME = 3

    fun present(children: List<Child>): List<Child> =
        children.filter { it.attendance == AttendanceStatus.PRESENT }

    fun absent(children: List<Child>): List<Child> =
        children.filter { it.attendance == AttendanceStatus.ABSENT }.sortedBy { it.name }

    fun unseenNotes(child: Child, seen: Set<Int>): List<FamilyNote> =
        child.notes.filter { it.id !in seen }

    // todos los avisos de hoy, de cualquier niño (también de uno que aún no llega: "llega tarde"),
    // el más nuevo primero. los vistos se quedan fuera cuando onlyUnseen es true.
    fun notices(children: List<Child>, seen: Set<Int>, onlyUnseen: Boolean): List<RoomNotice> =
        children
            .flatMap { child -> child.notes.map { RoomNotice(child, it) } }
            .filter { !onlyUnseen || it.note.id !in seen }
            .sortedByDescending { it.note.id }

    // algo que atender: un aviso sin ver o un chat sin leer.
    fun needsAttention(child: Child, seen: Set<Int>): Boolean =
        unseenNotes(child, seen).isNotEmpty() || child.hasUnreadChat

    // los que están hoy: primero los que tienen algo pendiente, y por nombre dentro de cada grupo.
    fun presentSorted(children: List<Child>, seen: Set<Int>): List<Child> =
        present(children).sortedWith(
            compareByDescending<Child> { needsAttention(it, seen) }.thenBy { it.name },
        )

    fun matches(child: Child, filter: RoomFilter, seen: Set<Int>): Boolean = when (filter) {
        RoomFilter.ALL -> true
        RoomFilter.WITH_NOTE -> unseenNotes(child, seen).isNotEmpty()
        RoomFilter.WITH_CHAT -> child.hasUnreadChat
        RoomFilter.NO_SUMMARY -> !child.hasSummaryToday
    }

    fun filtered(children: List<Child>, filter: RoomFilter, seen: Set<Int>): List<Child> =
        presentSorted(children, seen).filter { matches(it, filter, seen) }

    fun count(children: List<Child>, filter: RoomFilter, seen: Set<Int>): Int =
        present(children).count { matches(it, filter, seen) }

    // la cola del resumen guiado: los niños que están hoy y todavía no tienen su resumen, por nombre.
    fun pendingSummaries(children: List<Child>): List<Child> =
        present(children).filter { !it.hasSummaryToday }.sortedBy { it.name }

    // "11 de 12": para la línea de texto del encabezado y para la tarjeta del pase de lista.
    fun presentOutOf(children: List<Child>): Pair<Int, Int> = present(children).size to children.size
}
