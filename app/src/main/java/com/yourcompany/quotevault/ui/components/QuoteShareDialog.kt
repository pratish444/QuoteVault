package com.yourcompany.quotevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourcompany.quotevault.utils.CardStyle

sealed class ShareDialogStep {
    data object ChooseOption : ShareDialogStep()
    data class ChooseStyle(val action: ShareAction) : ShareDialogStep()
}

enum class ShareAction {
    SHARE_AS_IMAGE,
    SAVE_AS_IMAGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteShareDialog(
    onDismiss: () -> Unit,
    onShareAsText: () -> Unit,
    onShareAsImage: (CardStyle) -> Unit,
    onSaveAsImage: (CardStyle) -> Unit
) {
    var currentStep by remember { mutableStateOf<ShareDialogStep>(ShareDialogStep.ChooseOption) }

    when (currentStep) {
        is ShareDialogStep.ChooseOption -> ChooseOptionDialog(
            onDismiss = onDismiss,
            onShareAsText = {
                onShareAsText()
                onDismiss()
            },
            onShareAsImage = {
                currentStep = ShareDialogStep.ChooseStyle(ShareAction.SHARE_AS_IMAGE)
            },
            onSaveAsImage = {
                currentStep = ShareDialogStep.ChooseStyle(ShareAction.SAVE_AS_IMAGE)
            }
        )
        is ShareDialogStep.ChooseStyle -> ChooseStyleDialog(
            action = (currentStep as ShareDialogStep.ChooseStyle).action,
            onStyleSelected = { style ->
                when ((currentStep as ShareDialogStep.ChooseStyle).action) {
                    ShareAction.SHARE_AS_IMAGE -> onShareAsImage(style)
                    ShareAction.SAVE_AS_IMAGE -> onSaveAsImage(style)
                }
                onDismiss()
            },
            onBack = { currentStep = ShareDialogStep.ChooseOption },
            onCancel = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChooseOptionDialog(
    onDismiss: () -> Unit,
    onShareAsText: () -> Unit,
    onShareAsImage: () -> Unit,
    onSaveAsImage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.FormatQuote, contentDescription = null)
                Text(
                    text = "Share Quote",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Choose how you'd like to share this quote:")

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ListItem(
                        leadingContent = { Icon(Icons.Default.TextSnippet, null) },
                        headlineContent = { Text("Share as Text") },
                        trailingContent = { Icon(Icons.Default.ArrowBack, contentDescription = null) },
                        modifier = Modifier.clickable(onClick = onShareAsText)
                    )
                    HorizontalDivider()
                    ListItem(
                        leadingContent = { Icon(Icons.Default.Image, null) },
                        headlineContent = { Text("Share as Image") },
                        supportingContent = { Text("Choose from multiple card styles") },
                        trailingContent = { Icon(Icons.Default.ArrowBack, contentDescription = null) },
                        modifier = Modifier.clickable(onClick = onShareAsImage)
                    )
                    HorizontalDivider()
                    ListItem(
                        leadingContent = { Icon(Icons.Default.Save, null) },
                        headlineContent = { Text("Save as Image") },
                        supportingContent = { Text("Download to your device") },
                        trailingContent = { Icon(Icons.Default.ArrowBack, contentDescription = null) },
                        modifier = Modifier.clickable(onClick = onSaveAsImage)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChooseStyleDialog(
    action: ShareAction,
    onStyleSelected: (CardStyle) -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    var selectedStyle by remember { mutableStateOf<CardStyle?>(null) }
    val actionTitle = when (action) {
        ShareAction.SHARE_AS_IMAGE -> "Share as Image"
        ShareAction.SAVE_AS_IMAGE -> "Save as Image"
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Column {
                Text(
                    text = actionTitle,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choose a card style",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(CardStyle.entries) { style ->
                    EnhancedStyleItem(
                        style = style,
                        isSelected = selectedStyle == style,
                        onClick = { selectedStyle = style }
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back")
                }
                Button(
                    onClick = { selectedStyle?.let { onStyleSelected(it) } },
                    enabled = selectedStyle != null
                ) {
                    Text(if (action == ShareAction.SHARE_AS_IMAGE) "Share" else "Save")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedStyleItem(
    style: CardStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = when (style) {
        CardStyle.MODERN -> Color(0xFFF5F5F5)
        CardStyle.CLASSIC -> Color(0xFFFFF8E1)
        CardStyle.MINIMAL -> Color.White
        CardStyle.DARK -> Color(0xFF424242)
        CardStyle.PASTEL -> Color(0xFFE1F5FE)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                color
            }
        ),
        border = if (!isSelected && style == CardStyle.MODERN) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = Color(0xFF6366F1)
            )
        } else if (!isSelected && style == CardStyle.CLASSIC) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = Color(0xFF8B4513)
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .let { modifier ->
                            if (style == CardStyle.MODERN || style == CardStyle.CLASSIC) {
                                modifier.background(color, shape = RoundedCornerShape(8.dp))
                                    .border(
                                        width = 2.dp,
                                        color = if (style == CardStyle.MODERN) Color(0xFF6366F1) else Color(0xFF8B4513),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            } else {
                                modifier.background(color, shape = RoundedCornerShape(8.dp))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Q",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (style == CardStyle.DARK) Color.White else Color(0xFF212121)
                    )
                }
                Column {
                    Text(
                        text = style.displayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = getStyleDescription(style),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.DarkGray
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun getStyleDescription(style: CardStyle): String {
    return when (style) {
        CardStyle.MODERN -> "Clean with purple border"
        CardStyle.CLASSIC -> "Elegant serif font"
        CardStyle.MINIMAL -> "Simple and clean"
        CardStyle.DARK -> "Bold dark theme"
        CardStyle.PASTEL -> "Soft blue tones"
    }
}