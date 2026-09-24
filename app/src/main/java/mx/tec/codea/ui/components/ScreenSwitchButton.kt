package mx.tec.codea.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.codea.R
import mx.tec.codea.ui.theme.CodeaTheme

// a small round button that jumps between two versions of the same tab
// (for example "mi sala" and "aún el día no comienza").
// we use it while the app has fixed data, so we can show every screen in a demo.
@Composable
fun ScreenSwitchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // "tonal" means a soft background color, so the button does not fight with the content.
    FilledTonalIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.Refresh,
            // screen readers read this text to people who cannot see the screen.
            contentDescription = stringResource(R.string.screen_switch_description),
        )
    }
}

@Preview
@Composable
private fun ScreenSwitchButtonPreview() {
    CodeaTheme {
        ScreenSwitchButton(onClick = {})
    }
}
