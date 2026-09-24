package mx.tec.codea.ui.screens.chats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.codea.ui.theme.CodeaTheme

// the "chats" screen: the conversations with the families of the room.
// for now the screen is empty on purpose: the tab already works,
// and we will build the content here later, following the html prototype.
@Composable
fun ChatsScreen(modifier: Modifier = Modifier) {
    // an empty box that fills the space, so the tab shows a blank screen.
    Box(modifier = modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun ChatsScreenPreview() {
    CodeaTheme {
        ChatsScreen()
    }
}
