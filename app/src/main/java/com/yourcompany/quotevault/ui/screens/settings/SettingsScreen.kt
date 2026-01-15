package com.yourcompany.quotevault.ui.screens.settings


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourcompany.quotevault.domain.model.AppTheme
import com.yourcompany.quotevault.domain.model.FontSize
import com.yourcompany.quotevault.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle(
        initialValue = com.yourcompany.quotevault.domain.model.UserPreferences()
    )
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showNotificationTimeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Appearance Section
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column {
                    ListItem(
                        headlineContent = { Text("Theme") },
                        supportingContent = { Text(userPreferences.theme.name) },
                        leadingContent = { Icon(Icons.Default.Palette, null) },
                        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
                        modifier = Modifier.clickable { showThemeDialog = true }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Accent Color") },
                        supportingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Color(android.graphics.Color.parseColor(userPreferences.accentColor))
                                        )
                                )
                                Text(Constants.ACCENT_COLORS.entries.find { it.value == userPreferences.accentColor }?.key ?: "Custom")
                            }
                        },
                        leadingContent = { Icon(Icons.Default.ColorLens, null) },
                        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
                        modifier = Modifier.clickable { showColorDialog = true }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Font Size") },
                        supportingContent = { Text(userPreferences.fontSize.name) },
                        leadingContent = { Icon(Icons.Default.FormatSize, null) },
                        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
                        modifier = Modifier.clickable { showFontSizeDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notifications Section
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column {
                    ListItem(
                        headlineContent = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Daily Quote Notification")
                                Switch(
                                    checked = userPreferences.notificationsEnabled,
                                    onCheckedChange = { enabled ->
                                        viewModel.updateNotifications(
                                            enabled,
                                            userPreferences.notificationHour,
                                            userPreferences.notificationMinute
                                        )
                                    }
                                )
                            }
                        },
                        leadingContent = { Icon(Icons.Default.Notifications, null) }
                    )
                    if (userPreferences.notificationsEnabled) {
                        Divider()
                        ListItem(
                            headlineContent = { Text("Notification Time") },
                            supportingContent = {
                                val hour = userPreferences.notificationHour
                                val minute = userPreferences.notificationMinute
                                val amPm = if (hour >= 12) "PM" else "AM"
                                val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                                Text(String.format("%02d:%02d %s", displayHour, minute, amPm))
                            },
                            leadingContent = { Icon(Icons.Default.AccessTime, null) },
                            trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
                            modifier = Modifier.clickable { showNotificationTimeDialog = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column {
                    ListItem(
                        headlineContent = { Text("Version") },
                        supportingContent = { Text("1.0.0") },
                        leadingContent = { Icon(Icons.Default.Info, null) }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Privacy Policy") },
                        leadingContent = { Icon(Icons.Default.PrivacyTip, null) },
                        modifier = Modifier.clickable { }
                    )
                }
            }
        }
    }

    // Theme Dialog
    if (showThemeDialog) {
        ThemeDialog(
            currentTheme = userPreferences.theme,
            onThemeSelected = { theme ->
                viewModel.updateTheme(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Color Dialog
    if (showColorDialog) {
        ColorDialog(
            currentColor = userPreferences.accentColor,
            onColorSelected = { color ->
                viewModel.updateAccentColor(color)
                showColorDialog = false
            },
            onDismiss = { showColorDialog = false }
        )
    }

    // Font Size Dialog
    if (showFontSizeDialog) {
        FontSizeDialog(
            currentFontSize = userPreferences.fontSize,
            onFontSizeSelected = { fontSize ->
                viewModel.updateFontSize(fontSize)
                showFontSizeDialog = false
            },
            onDismiss = { showFontSizeDialog = false }
        )
    }

    // Notification Time Dialog
    if (showNotificationTimeDialog) {
        NotificationTimeDialog(
            currentHour = userPreferences.notificationHour,
            currentMinute = userPreferences.notificationMinute,
            onTimeSelected = { hour, minute ->
                viewModel.updateNotifications(
                    userPreferences.notificationsEnabled,
                    hour,
                    minute
                )
                showNotificationTimeDialog = false
            },
            onDismiss = { showNotificationTimeDialog = false }
        )
    }
}

@Composable
fun ThemeDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column {
                AppTheme.values().forEach { theme ->
                    ListItem(
                        headlineContent = { Text(theme.name) },
                        leadingContent = {
                            RadioButton(
                                selected = currentTheme == theme,
                                onClick = { onThemeSelected(theme) }
                            )
                        },
                        modifier = Modifier.clickable { onThemeSelected(theme) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ColorDialog(
    currentColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Accent Color") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Constants.ACCENT_COLORS.forEach { (name, color) ->
                    ListItem(
                        headlineContent = { Text(name) },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(color)))
                            )
                        },
                        trailingContent = {
                            if (currentColor == color) {
                                Icon(Icons.Default.Check, contentDescription = "Selected")
                            }
                        },
                        modifier = Modifier.clickable { onColorSelected(color) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun FontSizeDialog(
    currentFontSize: FontSize,
    onFontSizeSelected: (FontSize) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Font Size") },
        text = {
            Column {
                FontSize.values().forEach { fontSize ->
                    ListItem(
                        headlineContent = {
                            Text(
                                fontSize.name,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize * fontSize.scale
                            )
                        },
                        leadingContent = {
                            RadioButton(
                                selected = currentFontSize == fontSize,
                                onClick = { onFontSizeSelected(fontSize) }
                            )
                        },
                        modifier = Modifier.clickable { onFontSizeSelected(fontSize) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun NotificationTimeDialog(
    currentHour: Int,
    currentMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    TimePickerDialog(
        initialHour = currentHour,
        initialMinute = currentMinute,
        onConfirm = onTimeSelected,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        val selectedTime = "${timePickerState.hour}:${String.format("%02d", timePickerState.minute)}"
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Time") },
            text = { Text("Set daily quote notification at $selectedTime?") },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                    showConfirmDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Edit")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Set Notification Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = { showConfirmDialog = true }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}