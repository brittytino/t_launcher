package app.olauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.domain.model.AppCategory
import app.olauncher.domain.model.CategoryType
import app.olauncher.data.AppModel
import app.olauncher.ui.theme.TLauncherTypography
import kotlinx.coroutines.launch

@Composable
fun AppCategoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val appList by viewModel.appList.observeAsState(initial = emptyList())
    // Map of PackageName -> CategoryType
    var categoryMap by remember { mutableStateOf<Map<String, CategoryType>>(emptyMap()) }
    
    LaunchedEffect(Unit) {
        if (appList.isNullOrEmpty()) {
            viewModel.getAppList()
        }
        val map = mutableMapOf<String, CategoryType>()
        // We need to fetch categories. Since we don't have a direct method to get all,
        // we'll fetch individually or use a bulk loader if available.
        // For now, let's assume MainViewModel can expose this or we iterate.
        // NOTE: This iteration is slow for many apps if done sequentially.
        // Ideally we need getAllCategories() in Repository.
        // Repository has getAllCategories() returning Flow.
        // Let's use getAllCategoriesSync() if available or collect the flow.
        try {
            val allCats = viewModel.categoryRepository.getAllCategoriesSync()
            allCats.forEach { 
                map[it.packageName] = it.type 
            }
            categoryMap = map
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            SettingsTopBar(onBack)
        }
    ) { padding ->
        if (appList.isNullOrEmpty()) {
             Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                 item {
                     Text(
                        "Categorize your apps to control blocking strictness.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
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
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
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
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = app.appPackage, 
                style = TLauncherTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Box {
             Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentType.name, 
                    style = TLauncherTypography.labelMedium,
                    color = getCategoryColor(currentType)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                CategoryType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name, color = getCategoryColor(type)) },
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
        CategoryType.PHONE,
        CategoryType.MAPS,
        CategoryType.UTILITY,
        CategoryType.SYSTEM,
        CategoryType.MESSAGING,
        CategoryType.PRODUCTIVITY -> Color(0xFF4CAF50) // Green (Essential/Productive)
        
        CategoryType.SOCIAL,
        CategoryType.GAME,
        CategoryType.NEWS -> Color(0xFFF44336) // Red (Distracting)
        
        CategoryType.MUSIC,
        CategoryType.OTHER -> Color.Gray // Neutral
    }
}
