package mx.tec.codea.ui.screens.myday

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.ui.components.ScreenSwitchButton
import mx.tec.codea.ui.theme.CodeaTheme

// the screen "mi día" shows while the teacher has not checked in yet.
// the routine can not start without the check-in, so we send the teacher there first.
@Composable
fun DayNotStartedScreen(
    modifier: Modifier = Modifier,
    // fixed name for now. later it will come from the logged-in teacher.
    teacherName: String = "miss Karla",
    onCheckerClick: () -> Unit = {},
    // goes back to the normal "mi día" screen.
    onSwitchClick: () -> Unit = {},
) {
    // a box lets us put the switch button in the corner, on top of the message.
    Box(modifier = modifier.fillMaxSize()) {
        DayNotStartedMessage(
            teacherName = teacherName,
            onCheckerClick = onCheckerClick,
        )
        ScreenSwitchButton(
            onClick = onSwitchClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        )
    }
}

// the greeting, the explanation and the button to go to the check-in.
@Composable
private fun DayNotStartedMessage(teacherName: String, onCheckerClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        // the content sits in the middle of the screen, like a friendly message.
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CheckerRingIcon()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.day_not_started_greeting, teacherName),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.day_not_started_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            // coral says "something is missing", the same color as the check-in card.
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.day_not_started_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onCheckerClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            Text(
                text = stringResource(R.string.day_not_started_checker_button),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

// the same ring we draw on the check-in card of the menu, but bigger,
// so the teacher links this message with that card.
@Composable
private fun CheckerRingIcon() {
    Box(
        modifier = Modifier
            .size(84.dp)
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(30.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .border(5.dp, MaterialTheme.colorScheme.error, CircleShape),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DayNotStartedScreenPreview() {
    CodeaTheme {
        DayNotStartedScreen()
    }
}
