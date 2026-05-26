package com.remenod.oop2_coursework.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.remenod.oop2_coursework.AppContainer
import com.remenod.oop2_coursework.presentation.dashboard.DashboardScreen
import com.remenod.oop2_coursework.presentation.dashboard.DashboardViewModel
import com.remenod.oop2_coursework.presentation.discipline.DisciplineListScreen
import com.remenod.oop2_coursework.presentation.discipline.DisciplineListViewModel
import com.remenod.oop2_coursework.presentation.workdetail.WorkDetailScreen
import com.remenod.oop2_coursework.presentation.workdetail.WorkDetailViewModel
import com.remenod.oop2_coursework.presentation.worklist.WorkListScreen
import com.remenod.oop2_coursework.presentation.worklist.WorkListViewModel
import com.remenod.oop2_coursework.presentation.common.ViewModelFactory

@Composable
fun AppNavHost(appContainer: AppContainer) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable(route = "dashboard") {
            val viewModel: DashboardViewModel = viewModel(
                factory = ViewModelFactory { 
                    DashboardViewModel(appContainer.repository, appContainer.analyticsService) 
                }
            )
            DashboardScreen(
                viewModel = viewModel,
                onDisciplineClick = { id -> navController.navigate("workList/$id") },
                onTaskClick = { id -> navController.navigate("workDetail/$id") },
                onViewDisciplines = { navController.navigate("disciplineList") }
            )
        }
        composable(
            route = "disciplineList",
            content = { _ ->
                val viewModel: DisciplineListViewModel = viewModel(
                    factory = ViewModelFactory { DisciplineListViewModel(appContainer.repository) }
                )
                DisciplineListScreen(
                    viewModel = viewModel,
                    onDisciplineClick = { id -> navController.navigate("workList/$id") }
                )
            }
        )
        composable(
            route = "workList/{disciplineId}",
            arguments = listOf(navArgument("disciplineId") { type = NavType.LongType }),
            content = { backStackEntry ->
                val disciplineId = backStackEntry.arguments?.getLong("disciplineId") ?: 0L
                val viewModel: WorkListViewModel = viewModel(
                    factory = ViewModelFactory { WorkListViewModel(appContainer.repository, disciplineId) }
                )
                WorkListScreen(
                    viewModel = viewModel,
                    onWorkItemClick = { id -> navController.navigate("workDetail/$id") },
                    onBack = { navController.popBackStack() }
                )
            }
        )
        composable(
            route = "workDetail/{workItemId}",
            arguments = listOf(navArgument("workItemId") { type = NavType.LongType }),
            content = { backStackEntry ->
                val workItemId = backStackEntry.arguments?.getLong("workItemId") ?: 0L
                val viewModel: WorkDetailViewModel = viewModel(
                    factory = ViewModelFactory { WorkDetailViewModel(appContainer.repository, workItemId) }
                )
                WorkDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSubTaskClick = { id -> navController.navigate("workDetail/$id") }
                )
            }
        )
    }
}
