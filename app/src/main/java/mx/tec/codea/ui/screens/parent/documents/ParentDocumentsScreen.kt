package mx.tec.codea.ui.screens.parent.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.theme.CodeaTheme

@Composable
fun ParentDocumentsScreen(
    modifier: Modifier = Modifier,
    childName: String = ParentDocumentsSampleData.childName,
    documents: List<RequestedDocument> = ParentDocumentsSampleData.documents,
    authorizedPeople: List<AuthorizedPerson> = ParentDocumentsSampleData.authorizedPeople,
    onDocumentAction: (RequestedDocument) -> Unit = {},
    onAddAuthorized: () -> Unit = {},
    onRemoveAuthorized: (AuthorizedPerson) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 22.dp,
            vertical = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                overline = stringResource(R.string.parent_documents_overline),
                title = stringResource(
                    R.string.parent_documents_title,
                    childName,
                ),
            )
        }

        items(
            items = documents,
            key = { it.id },
        ) { document ->
            DocumentCard(
                document = document,
                onActionClick = {
                    onDocumentAction(document)
                },
            )
        }

        item {
            PickupSectionHeader()
        }

        item {
            DocumentCard(
                document = ParentDocumentsSampleData.pickupPermission,
                onActionClick = onAddAuthorized,
            )
        }

        items(
            items = authorizedPeople,
            key = { it.id },
        ) { person ->
            AuthorizedPersonRow(
                person = person,
                onRemove = {
                    onRemoveAuthorized(person)
                },
            )
        }
    }
}

@Composable
private fun DocumentCard(
    document: RequestedDocument,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    document.detail?.let { detail ->
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                DocumentStatusPill(
                    status = document.status,
                )
            }

            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            ) {
                Text(
                    text = document.actionLabel,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun DocumentStatusPill(
    status: DocumentStatus,
) {
    val containerColor: Color
    val contentColor: Color
    val text: String

    when (status) {
        DocumentStatus.CURRENT -> {
            containerColor = MaterialTheme.colorScheme.tertiary
            contentColor = MaterialTheme.colorScheme.onTertiary
            text = stringResource(R.string.parent_documents_current)
        }

        DocumentStatus.MISSING -> {
            containerColor = MaterialTheme.colorScheme.error
            contentColor = MaterialTheme.colorScheme.onError
            text = stringResource(R.string.parent_documents_missing)
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = contentColor,
        modifier = Modifier
            .background(
                color = containerColor,
                shape = CircleShape,
            )
            .padding(
                horizontal = 11.dp,
                vertical = 6.dp,
            ),
    )
}

@Composable
private fun PickupSectionHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        Text(
            text = stringResource(
                R.string.parent_documents_authorized_section,
            ),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AuthorizedPersonRow(
    person: AuthorizedPerson,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = person.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = stringResource(
                R.string.parent_documents_remove_authorized,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable(onClick = onRemove),
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ParentDocumentsScreenPreview() {
    CodeaTheme {
        ParentDocumentsScreen()
    }
}
