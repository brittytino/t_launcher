package app.olauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.data.AppModel
import app.olauncher.domain.model.CategoryType
import app.olauncher.ui.theme.*

@Composable
fun AppCategoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val appList by viewModel.appList.observeAsState(initial = emptyList())
    var categoryMap by remember { mutableStateOf<Map<String, CategoryType>>(emptyMap()) }
    
    LaunchedEffect(Unit) {
        if (appList.isNullOrEmpty()) {
            viewModel.getAppList()
        }
        try {
            val allCats = viewModel.categoryRepository.getAllCategoriesSync()
            val map = mutableMapOf<String, CategoryType>()
            allCats.forEach { map[it.packageName] = it.type }
            categoryMap = map
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    TScaffold {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = TLauncherTheme.spacing.medium)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = TLauncherTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                 IconButton(onClick = onBack) {
                     Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                 }
                 Text("APP CATEGORIES", style = TLauncherTypography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
            
            Text(
                "Categorize to enforce strict blocking rules.",
                style = TLauncherTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (appList.isNullOrEmpty()) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                TCard(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn {
                        val sortedApps = appList!!.sortedBy { it.appLabel }
                        items(sortedApps) { app ->
                            val currentType = categoryMap[app.appPackage] ?: CategoryType.OTHER
                            
                            CategoryAppItem(
                                app = app,
                                currentType = currentType,
                                onTypeSelected = { newType ->
                                    viewModel.updateAppCategory(app.appPackage, newType)
                                    categoryMap = categoryMap.toMutableMap().apply { put(app.appPackage, newType) }
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryAppItem(app: AppModel, currentType: CategoryType, onTypeSelected: (CategoryType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appLabel, 
                style = TLauncherTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = app.appPackage, 
                style = TLauncherTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Box {
             Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentType.name, 
                    style = TLauncherTypography.labelSmall,
                    color = getCategoryColor(currentType)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            DropdownMenu(
                expanded = expanded, 
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CategoryType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name, color = getCategoryColor(type), style = TLauncherTypography.labelMedium) },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

fun getCategoryColor(type: CategoryType): Color {
    return when(type) {
        CategoryType.PRODUCTIVE, CategoryType.UTILITY, CategoryType.SYSTEM -> Color(0xFF81C784) // Soft Green
        CategoryType.SOCIAL, CategoryType.GAME, CategoryType.NEWS -> Color(0xFFE57373) // Soft Red
        else -> Color.Gray
    }
}
