package mx.tec.codea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.tec.codea.ui.theme.Amber
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Coral
import mx.tec.codea.ui.theme.Teal
import mx.tec.codea.ui.theme.Violet

// the colors of the avatars. the color is a ui decision, so it is not in the domain:
// the screen picks it with the position of the child in the list.
private val avatarColors = listOf(Amber, Coral, Violet, Teal)

fun avatarColorAt(index: Int): Color = avatarColors[index % avatarColors.size]

// a rounded square with the initials of a person, like "KR" or "SM".
@Composable
fun InitialsAvatar(
    initials: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
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
