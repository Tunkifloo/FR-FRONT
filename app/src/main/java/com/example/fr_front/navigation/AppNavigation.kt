package com.example.fr_front.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fr_front.ui.screens.*

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Pantalla principal
        composable("home") {
            HomeScreen(navController = navController)
        }

        // ===== GESTIÓN DE PERSONAS =====
        composable("register") {
            RegisterPersonScreen(navController = navController)
        }

        composable("search") {
            SearchPersonScreen(navController = navController)
        }

        composable("persons_list") {
            PersonsListScreen(navController = navController)
        }

        // ===== RECONOCIMIENTO FACIAL =====
        composable("recognition") {
            RecognitionScreen(navController = navController)
        }

        composable("identification") {
            IdentificationScreen(navController = navController)
        }

        // ===== ESTADÍSTICAS Y ADMINISTRACIÓN =====
        composable("stats") {
            StatsScreen(navController = navController)
        }

        composable("admin") {
            AdminScreen(navController = navController)
        }

        composable("health_check") {
            HealthCheckScreen(navController = navController)
        }

        // ===== GESTIÓN DE DATOS =====
        composable("data_management") {
            DataManagementScreen(navController = navController)
        }

        // ===== CONFIGURACIÓN AVANZADA =====
        composable("advanced_config") {
            AdvancedConfigScreen(navController = navController)
        }

        // ===== INFORMACIÓN DEL SISTEMA =====
        composable("system_info") {
            SystemInfoDetailScreen(navController = navController)
        }

        // ===== HERRAMIENTAS =====
        composable("tools") {
            ToolsScreen(navController = navController)
        }
    }
}