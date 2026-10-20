package mx.tec.codea.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

// a flat colored box with a title and a short text, like "se puede cambiar después"
// or "llegaste después de tu hora". the color tells what kind of message it is.
@Composable
fun InfoBox(
    title: String,
    body: String,
    containerColor: Color,
    titleColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = titleColor)
            Text(body, fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium, color = CodeaColores.GrisTexto)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoBoxPreview() {
    CodeaTheme {
        InfoBox(
            title = "Se puede cambiar después",
            body = "Aunque empieces con foto, puedes dictar el texto en el paso siguiente.",
            containerColor = CodeaColores.Lila,
            titleColor = CodeaColores.Morado,
        )
    }
}
