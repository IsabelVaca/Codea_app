package mx.tec.codea

import android.app.Application
import mx.tec.codea.di.AppContainer

// la aplicación crea el contenedor de dependencias una vez, antes que cualquier pantalla.
class CodeaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
