package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ExpenseRepository
import com.example.ui.compose.ExpenseAnalyticsScreen
import com.example.ui.compose.ExpenseDashboardScreen
import com.example.ui.legacy.AddExpenseActivity
import com.example.ui.legacy.ExpenseHistoryActivity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseFlowTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.ExpenseViewModel

/**
 * Main Entry Activity hosting the Jetpack Compose Screens
 * and bridging to classic Java XML activities (AddExpenseActivity, ExpenseHistoryActivity).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = ExpenseRepository.getInstance(applicationContext)

        setContent {
            ExpenseFlowTheme {
                val expenseViewModel: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModel.Factory(repository)
                )
                val analyticsViewModel: AnalyticsViewModel = viewModel(
                    factory = AnalyticsViewModel.Factory(repository)
                )

                MainAppScreen(
                    expenseViewModel = expenseViewModel,
                    analyticsViewModel = analyticsViewModel,
                    onOpenJavaAdd = {
                        startActivity(Intent(this, AddExpenseActivity::class.java))
                    },
                    onOpenJavaHistory = {
                        startActivity(Intent(this, ExpenseHistoryActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    expenseViewModel: ExpenseViewModel,
    analyticsViewModel: AnalyticsViewModel,
    onOpenJavaAdd: () -> Unit,
    onOpenJavaHistory: () -> Unit
) {
    var currentTab by remember { mutableStateOf("dashboard") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        bottomBar = {
            NavigationBar(
                containerColor = NavyPrimary,
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = currentTab == "dashboard",
                    onClick = { currentTab = "dashboard" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_dashboard"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "analytics",
                    onClick = { currentTab = "analytics" },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                    label = { Text("Analytics", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_analytics"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onOpenJavaAdd,
                    icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = "Java Add") },
                    label = { Text("Java Add", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_java_add"),
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = EmeraldPrimary,
                        unselectedTextColor = EmeraldPrimary
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onOpenJavaHistory,
                    icon = { Icon(Icons.Default.History, contentDescription = "Java History") },
                    label = { Text("Java History", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_java_history"),
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            "dashboard" -> {
                ExpenseDashboardScreen(
                    viewModel = expenseViewModel,
                    onNavigateToAnalytics = { currentTab = "analytics" },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            "analytics" -> {
                ExpenseAnalyticsScreen(
                    viewModel = analyticsViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
