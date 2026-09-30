package mx.tec.codea.ui.screens.parent.photos

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.codea.ui.screens.parents.FotosScreen
import mx.tec.codea.ui.theme.CodeaTheme

// the parent's photos screen: photos uploaded by the child's teachers.
@Composable
fun ParentPhotosScreen() {
    FotosScreen()
}

@Preview(showBackground = true)
@Composable
private fun ParentPhotosScreenPreview() {
    CodeaTheme {
        ParentPhotosScreen()
    }
}
