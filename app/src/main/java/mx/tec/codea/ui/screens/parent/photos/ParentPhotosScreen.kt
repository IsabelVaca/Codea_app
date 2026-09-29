package mx.tec.codea.ui.screens.parent.photos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.codea.ui.theme.CodeaTheme

// the parent's photos screen: photos uploaded by the child's teachers.
// content will be added later.
@Composable
fun ParentPhotosScreen(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun ParentPhotosScreenPreview() {
    CodeaTheme {
        ParentPhotosScreen()
    }
}
