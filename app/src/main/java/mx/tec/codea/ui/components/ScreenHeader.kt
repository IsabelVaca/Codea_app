package mx.tec.codea.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.theme.CodeaTheme

// every screen starts the same way: a big title. the small grey line on top
// (overline) was dropped from almost every screen; only a few keep it because
// it carries data ("Resumen 2 de 5", "Subproceso 3 · Comida"). null = no line.
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
) {
    Column(
        modifier = modifier.padding(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (overline != null) {
            Text(
                // the prototype writes this line in capital letters.
                text = overline.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.3.sp,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderPreview() {
    CodeaTheme {
        ScreenHeader(title = "Mi sala · Preescolar 2")
    }
}
