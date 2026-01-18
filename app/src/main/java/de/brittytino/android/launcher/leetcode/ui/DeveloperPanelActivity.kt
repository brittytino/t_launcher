package de.brittytino.android.launcher.leetcode.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.brittytino.android.launcher.Application
import de.brittytino.android.launcher.leetcode.data.LeetCodeUserEntity
import de.brittytino.android.launcher.preferences.LauncherPreferences
import java.time.LocalDate
import java.time.ZoneId
import de.brittytino.android.launcher.leetcode.data.DailyProblemEntity
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import androidx.compose.ui.graphics.Brush
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.ui.window.Popup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.horizontalScroll
import java.time.Instant
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.preference.PreferenceManager
import androidx.compose.runtime.saveable.rememberSaveable
import org.json.JSONObject

import de.brittytino.android.launcher.ui.settings.SettingsTheme

class DeveloperPanelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val factory = DeveloperPanelViewModelFactory(application as Application)
        val viewModel = androidx.lifecycle.ViewModelProvider(this, factory).get(DeveloperPanelViewModel::class.java)

        // Check standard preferences for username from Settings to ensure consistency
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val prefUsername = prefs.getString("leetcode_username", null)
        val currentProfile = viewModel.myProfile.value

        if (!prefUsername.isNullOrBlank()) {
             // Sync if no profile loaded yet OR if settings username differs from current profile
             if (currentProfile == null || currentProfile.username != prefUsername) {
                  viewModel.sync(prefUsername, true)
             }
        }

        // We only check if the feature is enabled.
        if (!LauncherPreferences.leetcode().enabled()) {
            finish()
            return
        }

        setContent {
            SettingsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val app = LocalContext.current.applicationContext as Application
                    val viewModel: DeveloperPanelViewModel = viewModel(
                        factory = DeveloperPanelViewModelFactory(app)
                    )
                    DeveloperPanelScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun DeveloperPanelScreen(viewModel: DeveloperPanelViewModel) {
    val myProfile by viewModel.myProfile.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val dailyProblem by viewModel.dailyProblem.collectAsState()
    val loading by viewModel.loadingState.collectAsState()
    val error by viewModel.errorState.collectAsState()
    
    // Auto-save username to preferences when profile is loaded
    val context = LocalContext.current
    LaunchedEffect(myProfile) {
        myProfile?.let { profile ->
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (prefs.getString("leetcode_username", null) != profile.username) {
                prefs.edit().putString("leetcode_username", profile.username).apply()
            }
        }
    }
    
    var showFriendDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dev Panel", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            if (myProfile != null) {
               IconButton(onClick = { viewModel.sync(myProfile!!.username, true) }) {
                   Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
               }
            }
        }
        
        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        AnimatedVisibility(visible = error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                    Button(onClick = { viewModel.clearError() }) { Text("Dismiss") }
                }
            }
        }

        if (myProfile == null) {
            SetupScreen(onSync = { username -> viewModel.sync(username, true) }, loading = loading)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    ProfileCard(user = myProfile!!, isMe = true)
                }
                
                item {
                    HeatmapCard(json = myProfile!!.submissionCalendarJson)
                }
                
                if (dailyProblem != null) {
                    item {
                        DailyProblemCard(problem = dailyProblem!!)
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        Text("Your Friends", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { showFriendDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Friend", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
                
                items(friends) { friend ->
                     ProfileCard(
                         user = friend, 
                         isMe = false, 
                         onRefresh = { viewModel.sync(friend.username, false) }, 
                         onDelete = { viewModel.removeFriend(friend.username) }
                     )
                }
                
                item { Spacer(modifier = Modifier.height(50.dp)) }
            }
        }
    }
    
    if (showFriendDialog) {
        AddUserDialog(
            onDismiss = { showFriendDialog = false },
            onConfirm = { username -> 
                viewModel.sync(username, false)
                showFriendDialog = false 
            }
        )
    }
}

@Composable
fun SetupScreen(onSync: (String) -> Unit, loading: Boolean) {
    var username by rememberSaveable { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp).fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add, // Placeholder, maybe use code icon
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Welcome to Developer Panel", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Text("Track your LeetCode consistency", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("LeetCode Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSync(username) }, 
                enabled = !loading && username.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Connect Profile", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddUserDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Friend") },
        text = {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(username) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileCard(user: LeetCodeUserEntity, isMe: Boolean, onRefresh: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(2.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = user.username, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    user.realName?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if(user.ranking > 0) "#${user.ranking}" else "N/A",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (onDelete != null) {
                   IconButton(onClick = onDelete) {
                       Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                   }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats Circular Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Progress
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                    CircularProgressChart(
                        easy = user.easySolved,
                        medium = user.mediumSolved,
                        hard = user.hardSolved,
                        total = user.totalSolved
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${user.totalSolved}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Solved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StatItem("Easy", user.easySolved.toString(), Color(0xFF00B8A3))
                    StatItem("Medium", user.mediumSolved.toString(), Color(0xFFFFC01E))
                    StatItem("Hard", user.hardSolved.toString(), Color(0xFFFF375F))
                }
            }
        }
    }
}

@Composable
fun CircularProgressChart(easy: Int, medium: Int, hard: Int, total: Int) {
    val totalQuestions = 3500f // Approximate total questions on LeetCode
    // Usually the API returns totalQuestions. For now we use solved as the circle parts relative to each other?
    // The image shows a full ring. Let's assume the ring represents the proportion of solved relative to total.
    // Or just the distribution.
    
    // To match the image style: A background ring, and then colored arcs.
    val strokeWidth = 8.dp
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val diameter = size.minDimension
        val radius = diameter / 2
        val stroke = strokeWidth.toPx()
        
        // Background track
        drawCircle(
            color = trackColor, 
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        
        // Angles
        // We draw them sequentially.
        // Let's assume we want to show the distribution relative to total solved?
        // Actually, the image typically shows progress against *total available* questions, hence the ring isn't full usually.
        // But for visual flair, let's make it reflect the distribution of the solved ones (full circle = total solved).
        
        if (total > 0) {
            val easySweep = (easy.toFloat() / total) * 360f
            val mediumSweep = (medium.toFloat() / total) * 360f
            val hardSweep = (hard.toFloat() / total) * 360f
            
            var startAngle = -90f
            
            // Easy
            drawArc(
                color = Color(0xFF00B8A3),
                startAngle = startAngle,
                sweepAngle = easySweep,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            startAngle += easySweep
            
            // Medium
            drawArc(
                color = Color(0xFFFFC01E),
                startAngle = startAngle,
                sweepAngle = mediumSweep,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            startAngle += mediumSweep
            
            // Hard
            drawArc(
                color = Color(0xFFFF375F),
                startAngle = startAngle,
                sweepAngle = hardSweep,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@Composable
fun HeatmapCard(json: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Submission Heatmap", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            Heatmap(json)
        }
    }
}

@Composable
fun Heatmap(json: String) {
    val submissions = remember(json) { parseSubmissionCalendar(json) }
    val now = LocalDate.now()
    
    // Last year (365 days) aligned to Sunday week start
    val totalDays = 365L
    val startDate = now.minusDays(totalDays).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    
    val days = remember(startDate) {
        val list = mutableListOf<LocalDate>()
        var current = startDate
        // Generate enough days to cover until today
        while (!current.isAfter(now)) {
            list.add(current)
            current = current.plusDays(1)
        }
        list
    }
    
    // Normalize timestamps to LocalDate for matching (optimized map)
    val submissionDates = remember(submissions) {
        submissions.entries.associate { (ts, count) ->
            Instant.ofEpochSecond(ts).atZone(ZoneId.systemDefault()).toLocalDate() to count
        }
    }
    
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedCount by remember { mutableStateOf(0) }

    Box {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Render columns (Weeks)
            for (weekChunk in days.chunked(7)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    weekChunk.forEach { date ->
                         // Match count
                         val count = submissionDates[date] ?: 0
                         
                         val color = calculateHeatmapColor(count, MaterialTheme.colorScheme.surfaceVariant)
                         
                         Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, RoundedCornerShape(2.dp))
                                .clickable { 
                                    selectedDate = date
                                    selectedCount = count
                                }
                        )
                    }
                }
            }
        }
        
        // Tooltip Popup
        if (selectedDate != null) {
            Popup(
                alignment = Alignment.TopCenter,
                onDismissRequest = { selectedDate = null }
            ) {
                Surface(
                   color = MaterialTheme.colorScheme.inverseSurface,
                   contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                   shape = RoundedCornerShape(4.dp),
                   shadowElevation = 4.dp,
                   modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${selectedCount} submissions", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Text(selectedDate.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Legend
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
        Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(end = 4.dp))
        listOf(0, 2, 5, 8, 12).forEach { 
             Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(10.dp)
                    .background(calculateHeatmapColor(it, MaterialTheme.colorScheme.surfaceVariant), RoundedCornerShape(2.dp))
            )
        }
        Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(start = 4.dp))
    }
}

fun calculateHeatmapColor(count: Int, emptyColor: Color): Color {
    return when {
        count == 0 -> emptyColor
        count in 1..2 -> Color(0xFF382800) // Deep Brown
        count in 3..5 -> Color(0xFFD99000) // Medium Orange
        count in 6..9 -> Color(0xFFFFC01E) // Gold
        else -> Color(0xFFFFD700)          // Bright Gold
    }
}

@Composable
fun DailyProblemCard(problem: DailyProblemEntity) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val date = try {
        LocalDate.parse(problem.date)
    } catch (e: Exception) {
        LocalDate.now()
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
             // Background glow effect
             Canvas(modifier = Modifier.matchParentSize()) {
                 drawRect(
                     brush = Brush.horizontalGradient(
                         colors = listOf(
                             Color(0xFF2C2C2C), 
                             surfaceColor
                         )
                     ),
                     alpha = 0.5f
                 )
             }
             
             Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Problem of the Day", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = LocalDate.now().toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "${problem.frontendId}. ${problem.title}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(problem.difficulty) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = when(problem.difficulty) {
                                "Easy" -> Color(0xFF00B8A3).copy(alpha = 0.2f)
                                "Medium" -> Color(0xFFFFC01E).copy(alpha = 0.2f)
                                "Hard" -> Color(0xFFFF375F).copy(alpha = 0.2f)
                                else -> Color.Gray.copy(alpha = 0.2f)
                            },
                            labelColor = when(problem.difficulty) {
                                "Easy" -> Color(0xFF00B8A3)
                                "Medium" -> Color(0xFFFFC01E)
                                "Hard" -> Color(0xFFFF375F)
                                else -> Color.Gray
                            }
                        ),
                        border = null
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { /* Could open DeepLink to LeetCode app or Browser */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Solve Now", color = Color.Black, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

fun parseSubmissionCalendar(json: String): Map<Long, Int> {
    return try {
        val jsonObject = JSONObject(json)
        val map = mutableMapOf<Long, Int>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            // Key is unix timestamp string
            map[key.toLong()] = jsonObject.getInt(key)
        }
        map
    } catch (e: Exception) {
        emptyMap()
    }
}
