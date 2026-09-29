package mx.tec.codea.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import mx.tec.codea.R
import androidx.compose.ui.unit.sp

// poppins is the font of the prototype. the files live in res/font,
// so the app does not need internet to show it.
// we only add the weights the prototype uses (from 400 to 800).
// if a screen asks for another weight, android picks the closest one.


val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold),
    Font(R.font.poppins_black, FontWeight.Black)
)

// we start from the default material sizes and only change the font,
// so every text style of the app (titles, body, labels) uses poppins.
private val baseline = Typography()

val Typography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = Poppins),
    displayMedium = baseline.displayMedium.copy(fontFamily = Poppins),
    displaySmall = baseline.displaySmall.copy(fontFamily = Poppins),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = Poppins),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = Poppins),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = Poppins),
    titleLarge = baseline.titleLarge.copy(fontFamily = Poppins),
    titleMedium = baseline.titleMedium.copy(fontFamily = Poppins),
    titleSmall = baseline.titleSmall.copy(fontFamily = Poppins),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = Poppins),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = Poppins),
    bodySmall = baseline.bodySmall.copy(fontFamily = Poppins),
    labelLarge = baseline.labelLarge.copy(fontFamily = Poppins),
    labelMedium = baseline.labelMedium.copy(fontFamily = Poppins),
    labelSmall = baseline.labelSmall.copy(fontFamily = Poppins),

    /* Other default text styles to override
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
