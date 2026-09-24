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

// every tab of the prototype starts the same way: a small grey line on top
// (the place or the person) and a big title below it. we write it once here.
@Composable
fun ScreenHeader(
    overline: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            // the prototype writes this line in capital letters.
            text = overline.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.3.sp,
            color = MaterialTheme.colorScheme.outline,
        )
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
        ScreenHeader(overline = "Estancia La Oruga", title = "Mi sala · Preescolar 2")
    }
}
