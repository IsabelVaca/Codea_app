package mx.tec.codea.ui.screens.parent.documents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.codea.ui.theme.CodeaTheme

// the parent's documents screen: official documents associated with the child.
// uploading and document management will be implemented later.
@Composable
fun ParentDocumentsScreen(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun ParentDocumentsScreenPreview() {
    CodeaTheme {
        ParentDocumentsScreen()
    }
}
