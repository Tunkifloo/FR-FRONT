package com.example.fr_front.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fr_front.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    var systemStatus by remember { mutableStateOf("Verificando...") }
    val scope = rememberCoroutineScope()

    // Verificar estado del sistema al cargar
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getGeneralHealth()
                if (response.isSuccessful) {
                    val health = response.body()
                    systemStatus = when (health?.status) {
                        "healthy" -> "✅ Sistema Operativo"
                        "degraded" -> "⚠️ Sistema con Problemas"
                        else -> "❌ Sistema No Disponible"
                    }
                } else {
                    systemStatus = "❌ Error de Conexión"
                }
            } catch (e: Exception) {
                systemStatus = "❌ Sin Conexión"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reconocimiento Facial",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Estado del Sistema
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Estado del Sistema",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            systemStatus,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                Text(
                    "Funciones Principales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                MenuCard(
                    title = "Registrar Persona",
                    description = "Agregar nueva persona al sistema",
                    icon = Icons.Default.PersonAdd,
                    onClick = { navController.navigate("register") }
                )
            }

            item {
                MenuCard(
                    title = "Reconocimiento Facial",
                    description = "Comparar foto con persona específica",
                    icon = Icons.Default.Face,
                    onClick = { navController.navigate("recognition") }
                )
            }

            item {
                MenuCard(
                    title = "Identificar Persona",
                    description = "Identificar persona contra toda la BD",
                    icon = Icons.Default.Search,
                    onClick = { navController.navigate("identification") }
                )
            }

            item {
                Text(
                    "Gestión de Datos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                MenuCard(
                    title = "Buscar Persona",
                    description = "Buscar por email o ID estudiante",
                    icon = Icons.Default.PersonSearch,
                    onClick = { navController.navigate("search") }
                )
            }

            item {
                MenuCard(
                    title = "Lista de Personas",
                    description = "Ver todas las personas registradas",
                    icon = Icons.Default.People,
                    onClick = { navController.navigate("persons_list") }
                )
            }

            item {
                MenuCard(
                    title = "Estadísticas",
                    description = "Ver estadísticas del sistema",
                    icon = Icons.Default.Analytics,
                    onClick = { navController.navigate("stats") }
                )
            }

            item {
                Text(
                    "Herramientas y Administración",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                MenuCard(
                    title = "Herramientas del Sistema",
                    description = "Conjunto completo de utilidades administrativas",
                    icon = Icons.Default.Build,
                    onClick = { navController.navigate("tools") }
                )
            }

            item {
                MenuCard(
                    title = "Panel de Administración",
                    description = "Gestión avanzada y configuración del sistema",
                    icon = Icons.Default.AdminPanelSettings,
                    onClick = { navController.navigate("admin") }
                )
            }

            item {
                MenuCard(
                    title = "Gestión de Datos",
                    description = "Exportar, importar y gestionar datos del sistema",
                    icon = Icons.Default.ManageAccounts,
                    onClick = { navController.navigate("data_management") }
                )
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}