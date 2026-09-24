package mx.tec.codea.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// we give each prototype color a material "role". screens ask for the role
// (for example "primaryContainer"), not the color, so dark mode still works.
private val LightColorScheme = lightColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = Lavender,
    onPrimaryContainer = VioletDeep,
    // amber is the color of the main action inside a card ("realizar reporte").
    secondary = Amber,
    onSecondary = Ink,
    // teal is the color of finished things.
    tertiary = Teal,
    onTertiary = Color.White,
    tertiaryContainer = TealWash,
    onTertiaryContainer = TealDeep,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Haze,
    onSurfaceVariant = Dusk,
    // white cards (children, menu items) sit on top of the cream background.
    surfaceContainerLowest = Color.White,
    surfaceContainer = Frost,
    // coral is used for "missing" things, like the check-in that is not done yet.
    error = Coral,
    onError = Color.White,
    errorContainer = CoralWash,
    onErrorContainer = CoralDeep,
    outline = Mist,
    outlineVariant = Lilac,
    // the "inverse" colors paint the dark card of the active subprocess.
    inverseSurface = Ink,
    inverseOnSurface = Cream,
)

private val DarkColorScheme = darkColorScheme(
    primary = VioletLight,
    onPrimary = Ink,
    primaryContainer = VioletNight,
    onPrimaryContainer = Lavender,
    secondary = Amber,
    onSecondary = Ink,
    tertiary = Teal,
    onTertiary = Color.White,
    tertiaryContainer = TealNight,
    onTertiaryContainer = TealLight,
    background = Night,
    onBackground = Cream,
    surface = Night,
    onSurface = Cream,
    surfaceVariant = NightRaised,
    onSurfaceVariant = Mist,
    surfaceContainerLowest = NightCard,
    surfaceContainer = NightRaised,
    error = Coral,
    onError = Color.White,
    errorContainer = CoralNight,
    onErrorContainer = CoralLight,
    outline = Dusk,
    outlineVariant = NightRaised,
    inverseSurface = Lavender,
    inverseOnSurface = Ink,
)

@Composable
fun CodeaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // dynamic color takes the colors from the phone wallpaper (android 12+).
    // it is off by default, because we want the app to look like the prototype.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
