package mx.tec.codea.ui.screens.menu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mx.tec.codea.R
import mx.tec.codea.ui.theme.CodeaTheme

// the "menú" screen. for now it only has the check-in card, copied from
// screen 1 of "registrar entrada" in the html prototype.
// when openChecker is true, the card taps itself, so the teacher sees
// where the check-in lives before the check-in screen opens.
@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    openChecker: Boolean = false,
    onCheckerClick: () -> Unit = {},
) {
    // the interaction source is where the card hears about touches.
    // we keep it here, so we can also send a "fake" touch to it.
    val checkerInteraction = remember { MutableInteractionSource() }
    // we need the card size to start the ripple from its center.
    var checkerSize by remember { mutableStateOf(IntSize.Zero) }
    // rememberSaveable keeps this value when the user comes back from the check-in,
    // so the card does not tap itself a second time.
    var autoTapDone by rememberSaveable { mutableStateOf(false) }

    if (openChecker && !autoTapDone) {
        // a launched effect runs this code once, outside the drawing of the screen.
        LaunchedEffect(Unit) {
            // we wait until the slide between tabs is over.
            delay(450)
            val center = Offset(checkerSize.width / 2f, checkerSize.height / 2f)
            val press = PressInteraction.Press(center)
            // the same events a real finger sends: press, wait a little, release.
            checkerInteraction.emit(press)
            delay(300)
            checkerInteraction.emit(PressInteraction.Release(press))
            // a short pause, so the eye can follow the ripple before the screen changes.
            delay(150)
            autoTapDone = true
            onCheckerClick()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 16.dp),
    ) {
        CheckerCard(
            interactionSource = checkerInteraction,
            onClick = onCheckerClick,
            modifier = Modifier.onSizeChanged { checkerSize = it },
        )
    }
}

// the white card with a coral border. coral means "you still have to do this":
// the teacher has not registered the start of the shift yet.
@Composable
private fun CheckerCard(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // the card becomes a bit smaller while it is pressed, like a real button.
    // this works for a real finger and also for the automatic tap.
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "checkerScale")
    val coral = MaterialTheme.colorScheme.error

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(2.5.dp, coral),
        shadowElevation = 6.dp,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            // the icon is a coral ring inside a light coral square, like in the prototype.
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(3.dp, coral, CircleShape),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.menu_checker_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = stringResource(R.string.menu_checker_subtitle),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = coral,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    CodeaTheme {
        MenuScreen()
    }
}
