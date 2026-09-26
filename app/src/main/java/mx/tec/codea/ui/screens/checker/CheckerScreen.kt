package mx.tec.codea.ui.screens.checker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.codea.ui.theme.CodeaTheme

// the "checador" screen: the map and the button to register the start of the shift.
// for now the screen is empty on purpose: the navigation already arrives here,
// and we will build the content later, following the html prototype.
@Composable
fun CheckerScreen(modifier: Modifier = Modifier) {
    // an empty box that fills the space, so the screen shows blank.
    Box(modifier = modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun CheckerScreenPreview() {
    CodeaTheme {
        CheckerScreen()
    }
}
