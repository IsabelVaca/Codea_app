package mx.tec.codea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.tec.codea.ui.theme.CodeaTheme

// the activity is the door into the app. we keep it very small:
// it only sets the theme and shows CodeaApp, where the real ui lives.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // this lets the app draw behind the system bars (status bar and navigation bar).
        enableEdgeToEdge()
        setContent {
            CodeaTheme {
                CodeaApp()
            }
        }
    }
}
