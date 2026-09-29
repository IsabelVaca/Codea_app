package mx.tec.codea.ui.screens.parent.today

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.codea.ui.theme.CodeaTheme

// the parent's "Hoy" screen: an overview of the child's information
// for the selected day. content will be added later.
@Composable
fun ParentTodayScreen(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun ParentTodayScreenPreview() {
    CodeaTheme {
        ParentTodayScreen()
    }
}
