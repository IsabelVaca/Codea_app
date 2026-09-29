package mx.tec.codea.ui.screens.parent.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.codea.ui.theme.CodeaTheme

// the parent's calendar screen. later, selecting a date here will open
// the "hoy" tab showing the overview for that selected date.
@Composable
fun ParentCalendarScreen(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun ParentCalendarScreenPreview() {
    CodeaTheme {
        ParentCalendarScreen()
    }
}
