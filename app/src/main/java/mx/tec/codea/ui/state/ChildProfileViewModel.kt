package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.TeacherApiRepository
import mx.tec.codea.domain.ChildProfile

// los datos que la ficha del niño pide al abrirse: alergias, edad, tutores y personas autorizadas.
// el nombre, la asistencia, los avisos y el resumen ya los tiene Mi sala (CodeaViewModel), así que la ficha
// se puede abrir sin conexión y solo estas secciones avisan que no pudieron cargarse.
class ChildProfileViewModel : ViewModel() {

    private val repository = TeacherApiRepository()
    private var childId: Int? = null

    var loading by mutableStateOf(true)
        private set
    var failed by mutableStateOf(false)
        private set
    var profile by mutableStateOf<ChildProfile?>(null)
        private set

    fun load(id: Int) {
        if (childId == id && profile != null) return
        childId = id
        loading = true
        failed = false
        viewModelScope.launch {
            try {
                profile = repository.childProfile(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failed = true
            } finally {
                loading = false
            }
        }
    }

    fun retry() {
        profile = null
        childId?.let(::load)
    }
}
