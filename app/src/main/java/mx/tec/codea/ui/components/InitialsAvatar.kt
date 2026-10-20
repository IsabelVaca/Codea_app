package mx.tec.codea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Violet

// every avatar looks like the admin ones: lavender background and violet initials.
// avatarColorAt stays so the callers do not change, but it always returns the violet.
fun avatarColorAt(@Suppress("UNUSED_PARAMETER") index: Int): Color = CodeaColores.Morado

// a rounded square with the initials of a person, like "KR" or "SM".
// color = the color of the initials; background = lavender, or white over a lavender card.
@Composable
fun InitialsAvatar(
    initials: String,
    color: Color = CodeaColores.Morado,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    background: Color = CodeaColores.Lila,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(background, RoundedCornerShape(size * 0.38f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            fontSize = (size.value * 0.36f).sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Preview
@Composable
private fun InitialsAvatarPreview() {
    CodeaTheme {
        InitialsAvatar(initials = "KR", color = Violet)
    }
}
