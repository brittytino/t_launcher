package app.olauncher.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.data.AppModel
import app.olauncher.data.Constants
import app.olauncher.ui.theme.*

@Composable
fun AppListScreen(
    viewModel: MainViewModel,
    flag: Int,
    onAppClick: (AppModel) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val appList by viewModel.appList.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.getAppList(includeHiddenApps = flag == Constants.FLAG_HIDDEN_APPS)
        // focusRequester.requestFocus() // Optional: maybe we don't want keyboard immediately popping up in this new calm design
    }

    val filteredApps = remember(appList, searchQuery) {
        if (searchQuery.isBlank()) {
             appList?.sortedBy { it.appLabel.lowercase() } ?: emptyList()
        } else {
             appList?.filter { 
                 it.appLabel.contains(searchQuery, ignoreCase = true) 
             }?.sortedBy { 
                 if (it.appLabel.startsWith(searchQuery, ignoreCase = true)) 0 else 1
             } ?: emptyList()
        }
    }

    TScaffold {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // App List (Takes available space)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = TLauncherTheme.spacing.medium, vertical = TLauncherTheme.spacing.medium),
                reverseLayout = false // Requirement checks? "Reverse list toggle" is in settings, assume standard for now.
            ) {
                items(filteredApps) { app ->
                    AppItem(
                        app = app,
                        onClick = { onAppClick(app) },
                        onLongClick = { /* Options */ }
                    )
                }
            }
            
            // Bottom Dock Area (Shadow/Gradient?)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface) // Bottom panel bg
                    .padding(TLauncherTheme.spacing.medium)
            ) {
                
                // Shortcuts Panel (Settings, Wallpaper, Focus)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = TLauncherTheme.spacing.medium),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                     DrawerShortcut(
                         icon = Icons.Default.Settings, 
                         label = "Settings", 
                         onClick = onNavigateToSettings
                     )
                     DrawerShortcut(
                         icon = Icons.Default.Wallpaper, 
                         label = "Wallpaper", 
                         onClick = { 
                             val count = app.olauncher.helper.WallpaperManager.getWallpaperCount()
                             val randomIdx = (0 until count).random()
                             app.olauncher.helper.WallpaperManager.applyWallpaper(context, randomIdx)
                         }
                     )
                     DrawerShortcut(
                         icon = Icons.Default.Adjust, 
                         label = "Focus", 
                         onClick = onNavigateToFocus
                     )
                }

                // Search Bar
                TCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(horizontal = TLauncherTheme.spacing.medium, vertical = 12.dp)) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = TLauncherTypography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { 
                                 if (filteredApps.isNotEmpty()) {
                                     onAppClick(filteredApps[0])
                                 }
                            }),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Search, 
                                            contentDescription = null, 
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Search apps...",
                                            style = TLauncherTypography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem(
    app: AppModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) 
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appLabel,
                style = TLauncherTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            // Usage stats placeholder if not available in AppModel
            // "Usage time shown subtly under app name"
            Text(
                text = "0m today", // Placeholder until verified
                style = TLauncherTypography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DrawerShortcut(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = TLauncherTypography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
