package mx.tec.codea.ui.screens.myroom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.codea.ui.theme.CodeaTheme

// the "mi sala" screen: the list of children of the room, with a chat button for each family.
// for now the screen is empty on purpose: the tab already works,
// and we will build the content here later, following the html prototype.
@Composable
fun MyRoomScreen(modifier: Modifier = Modifier) {
    // an empty box that fills the space, so the tab shows a blank screen.
    Box(modifier = modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun MyRoomScreenPreview() {
    CodeaTheme {
        MyRoomScreen()
    }
}
