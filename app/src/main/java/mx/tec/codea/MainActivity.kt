package mx.tec.codea

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import mx.tec.codea.domain.Invite
import mx.tec.codea.domain.InviteLink
import mx.tec.codea.data.LeidosStore
import mx.tec.codea.ui.navigation.CodeaApp
import mx.tec.codea.ui.theme.CodeaTheme

// the activity is the door into the app. we keep it very small:
// it only sets the theme and shows CodeaApp, where the real ui lives.
class MainActivity : ComponentActivity() {

    // el enlace de invitación con el que se abrió la app (o null). CodeaApp lo usa y avisa cuando ya lo gastó.
    private var invite by mutableStateOf<InviteLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // this lets the app draw behind the system bars (status bar and navigation bar).
        enableEdgeToEdge()
        // qué avisos ya se vieron se guarda en el teléfono.
        LeidosStore.init(applicationContext)
        // al girar la pantalla la actividad se recrea con el mismo intent: solo se lee la primera vez.
        if (savedInstanceState == null) readInvite(intent)
        setContent {
            CodeaTheme {
                CodeaApp(inviteLink = invite, onInviteUsed = { invite = null })
            }
        }
    }

    // con la app ya abierta, tocar otro enlace de invitación llega aquí.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readInvite(intent)
    }

    private fun readInvite(intent: Intent?) {
        Invite.parseLink(intent?.data?.toString())?.let { invite = it }
    }
}
