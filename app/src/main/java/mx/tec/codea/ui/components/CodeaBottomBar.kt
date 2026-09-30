package mx.tec.codea.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.ui.navigation.TopLevelDestination
import mx.tec.codea.ui.theme.CodeaTheme

// this bar does not know about navigation. it only receives data and reports clicks.
// we call this a "stateless" component: it is easier to test, reuse and preview.
@Composable
fun CodeaBottomBar(
    // the tab that is selected now, so we can highlight it.
    currentDestination: TopLevelDestination?,
    // we tell the parent which tab was tapped, and the parent decides what to do.
    onDestinationClick: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    // the list of tabs visible for the active role.
    destinations: List<TopLevelDestination> = TopLevelDestination.entries,
) {
    // the prototype draws the bar with the same color as the screen,
    // and only a thin line on top separates them.
    Column(modifier = modifier) {
        HorizontalDivider(thickness = 1.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            // the same colors for every tab, so we create them once.
            val itemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.outline,
                unselectedTextColor = MaterialTheme.colorScheme.outline,
            )

            // we create one item for each destination in the provided list.
            destinations.forEach { destination ->
                val selected = destination == currentDestination
                NavigationBarItem(
                    selected = selected,
                    onClick = { onDestinationClick(destination) },
                    // a disabled item looks grey and ignores taps, perfect for locked tabs.
                    enabled = !destination.isLocked,
                    icon = { DestinationIcon(destination) },
                    label = {
                        Text(
                            text = stringResource(destination.labelRes),
                            // the selected label is bolder, like in the prototype.
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                        )
                    },
                    colors = itemColors,
                )
            }
        }
    }
}

// we move the icon to its own function to keep CodeaBottomBar short and easy to read.
@Composable
private fun DestinationIcon(destination: TopLevelDestination) {
    // open tabs only show their icon.
    if (!destination.isLocked) {
        Icon(imageVector = destination.icon, contentDescription = null)
        return
    }

    // locked tabs show their icon with a small lock on the corner (a "badge").
    BadgedBox(
        badge = {
            Badge {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    // screen readers read this text to people who cannot see the screen.
                    contentDescription = stringResource(R.string.nav_locked_description),
                    modifier = Modifier.size(10.dp),
                )
            }
        },
    ) {
        // the label already describes the tab, so the main icon does not need a description.
        Icon(imageVector = destination.icon, contentDescription = null)
    }
}

// a preview lets us see the component in android studio without running the app.
@Preview(showBackground = true)
@Composable
private fun CodeaBottomBarPreview() {
    CodeaTheme {
        CodeaBottomBar(
            currentDestination = TopLevelDestination.MY_DAY,
            onDestinationClick = {},
        )
    }
}
