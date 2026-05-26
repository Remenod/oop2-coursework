package com.remenod.oop2_coursework.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.remenod.oop2_coursework.AppContainer
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

    NavHost(navController = navController, startDestination = "disciplineList") {
        composable(
            route = "disciplineList",
            arguments = emptyList(),
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
    }
}
