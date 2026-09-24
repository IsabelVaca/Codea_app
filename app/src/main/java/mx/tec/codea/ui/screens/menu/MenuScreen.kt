package mx.tec.codea.ui.screens.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.codea.ui.theme.CodeaTheme

// the "menú" screen: the teacher profile, the check-in and other options.
// for now the screen is empty on purpose: the tab already works,
// and we will build the content here later, following the html prototype.
@Composable
fun MenuScreen(modifier: Modifier = Modifier) {
    // an empty box that fills the space, so the tab shows a blank screen.
    Box(modifier = modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    CodeaTheme {
        MenuScreen()
    }
}
