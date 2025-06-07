package com.example.fr_front.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.fr_front.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavHostController) {
    var isLoading by remember { mutableStateOf(false) }
    var systemConfig by remember { mutableStateOf<SystemConfigResponse?>(null) }
    var integrityCheck by remember { mutableStateOf<IntegrityCheckResponse?>(null) }
    var performanceMetrics by remember { mutableStateOf<PerformanceMetricsResponse?>(null) }
    var showCleanupDialog by remember { mutableStateOf(false) }
    var cleanupResult by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun loadAdminData() {
        scope.launch {
            isLoading = true
            try {
                // Cargar configuración del sistema
                val configResponse = RetrofitClient.apiService.getSystemConfig()
                if (configResponse.isSuccessful) {
                    systemConfig = configResponse.body()
                }

                // Cargar verificación de integridad
                val integrityResponse = RetrofitClient.apiService.checkSystemIntegrity()
                if (integrityResponse.isSuccessful) {
                    integrityCheck = integrityResponse.body()
                }

                // Cargar métricas de rendimiento
                val performanceResponse = RetrofitClient.apiService.getPerformanceMetrics()
                if (performanceResponse.isSuccessful) {
                    performanceMetrics = performanceResponse.body()
                }

            } catch (e: Exception) {
                // Manejar error silenciosamente o mostrar un mensaje
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar datos al inicio
    LaunchedEffect(Unit) {
        loadAdminData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { loadAdminData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
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
                // Header del panel de admin
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Panel de Administración",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Gestión avanzada del sistema",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Estado de integridad del sistema
            integrityCheck?.let { integrity ->
                item {
                    SystemIntegrityCard(integrity)
                }
            }

            // Métricas de rendimiento
            performanceMetrics?.let { performance ->
                item {
                    PerformanceCard(performance)
                }
            }

            // Configuración del sistema
            systemConfig?.let { config ->
                item {
                    SystemConfigCard(config)
                }
            }

            item {
                Text(
                    "Herramientas de Administración",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Herramientas administrativas
            val adminTools = listOf(
                AdminTool(
                    title = "Estadísticas del Sistema",
                    description = "Ver métricas detalladas y estadísticas",
                    icon = Icons.Default.Analytics,
                    action = { navController.navigate("stats") }
                ),
                AdminTool(
                    title = "Gestión de Datos",
                    description = "Exportar, importar y gestionar datos",
                    icon = Icons.Default.ManageAccounts,
                    action = { navController.navigate("data_management") }
                ),
                AdminTool(
                    title = "Health Check Completo",
                    description = "Verificar estado de todos los componentes",
                    icon = Icons.Default.HealthAndSafety,
                    action = { navController.navigate("health_check") }
                ),
                AdminTool(
                    title = "Limpiar Sistema",
                    description = "Eliminar archivos temporales y limpiar cache",
                    icon = Icons.Default.CleaningServices,
                    action = { showCleanupDialog = true }
                ),
                AdminTool(
                    title = "Configuración Avanzada",
                    description = "Ajustar parámetros del sistema",
                    icon = Icons.Default.Settings,
                    action = { navController.navigate("advanced_config") }
                )
            )

            items(adminTools) { tool ->
                AdminToolCard(
                    tool = tool,
                    onAction = tool.action
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Diálogo de limpieza
    if (showCleanupDialog) {
        CleanupDialog(
            onDismiss = { showCleanupDialog = false },
            onConfirm = { maxAgeHours ->
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.cleanupSystem(maxAgeHours)
                        if (response.isSuccessful) {
                            val result = response.body()
                            cleanupResult = "✅ ${result?.message}\nLimpieza completada para archivos de más de $maxAgeHours horas"
                        } else {
                            cleanupResult = "❌ Error en limpieza: ${response.message()}"
                        }
                    } catch (e: Exception) {
                        cleanupResult = "❌ Error de conexión: ${e.message}"
                    }
                    showCleanupDialog = false
                }
            },
            result = cleanupResult,
            onResultDismiss = { cleanupResult = "" }
        )
    }
}

@Composable
fun SystemIntegrityCard(integrity: IntegrityCheckResponse) {
    val overallStatus = integrity.integrity_check.overall_status

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (overallStatus) {
                "healthy" -> MaterialTheme.colorScheme.primaryContainer
                "degraded" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (overallStatus) {
                        "healthy" -> Icons.Default.CheckCircle
                        "degraded" -> Icons.Default.Warning
                        else -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = when (overallStatus) {
                        "healthy" -> MaterialTheme.colorScheme.primary
                        "degraded" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Integridad del Sistema: ${overallStatus.uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Estado de la base de datos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Base de Datos:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (integrity.integrity_check.database.connection) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (integrity.integrity_check.database.connection)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        integrity.integrity_check.database.status,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Problemas de integridad de datos
            val dataIntegrity = integrity.integrity_check.data_integrity
            if (dataIntegrity.issues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "⚠️ Problemas encontrados:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                dataIntegrity.issues.take(3).forEach { issue ->
                    Text(
                        "• $issue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Recomendaciones
            if (integrity.recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "💡 Recomendaciones:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                integrity.recommendations.take(2).forEach { recommendation ->
                    Text(
                        "• $recommendation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceCard(performance: PerformanceMetricsResponse) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Métricas de Rendimiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricCard(
                    title = "Personas",
                    value = performance.database_performance.total_persons.toString(),
                    icon = Icons.Default.People
                )

                MetricCard(
                    title = "Características",
                    value = performance.database_performance.total_features.toString(),
                    icon = Icons.Default.Psychology
                )

                MetricCard(
                    title = "Dependencias",
                    value = if (performance.system_resources.dependencies_ok) "OK" else "Error",
                    icon = if (performance.system_resources.dependencies_ok) Icons.Default.CheckCircle else Icons.Default.Error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Configuración actual
            Text(
                "Estado de Configuración:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ConfigStatusRow(
                "Procesamiento Mejorado",
                performance.configuration_status.enhanced_processing
            )
            ConfigStatusRow(
                "Umbrales Adaptativos",
                performance.configuration_status.adaptive_thresholds
            )
            ConfigStatusRow(
                "Detectores Múltiples",
                performance.configuration_status.multiple_detectors
            )
        }
    }
}

@Composable
fun SystemConfigCard(config: SystemConfigResponse) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Configuración del Sistema",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Configuración de reconocimiento facial
            Text(
                "Reconocimiento Facial:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ConfigRow("Método", config.facial_recognition.feature_method)
            ConfigRow("Umbral por Defecto", "${(config.facial_recognition.default_threshold * 100).toInt()}%")

            ConfigStatusRow("Procesamiento Mejorado", config.facial_recognition.enhanced_processing)
            ConfigStatusRow("Umbrales Adaptativos", config.facial_recognition.adaptive_threshold)
            ConfigStatusRow("Detectores Múltiples", config.facial_recognition.multiple_detectors)
            ConfigStatusRow("dlib Habilitado", config.facial_recognition.use_dlib)

            Spacer(modifier = Modifier.height(12.dp))

            // Umbrales
            Text(
                "Configuración de Umbrales:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ConfigRow("Mínimo", "${(config.thresholds.min * 100).toInt()}%")
            ConfigRow("Máximo", "${(config.thresholds.max * 100).toInt()}%")

            Spacer(modifier = Modifier.height(12.dp))

            // Directorios
            Text(
                "Directorios:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ConfigRow("Uploads", config.directories.upload)
            ConfigRow("Modelos", config.directories.models)
            ConfigRow("Backups", config.directories.backup)
        }
    }
}

@Composable
fun AdminToolCard(
    tool: AdminTool,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onAction,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        tool.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    tool.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    tool.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    Surface(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ConfigStatusRow(label: String, enabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (enabled) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (enabled) "Sí" else "No",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun CleanupDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    result: String,
    onResultDismiss: () -> Unit
) {
    var selectedHours by remember { mutableStateOf(24) }

    if (result.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onResultDismiss,
            title = { Text("Resultado de Limpieza") },
            text = { Text(result) },
            confirmButton = {
                TextButton(onClick = onResultDismiss) {
                    Text("OK")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Limpiar Sistema") },
            text = {
                Column {
                    Text("Eliminar archivos temporales de más de:")
                    Spacer(modifier = Modifier.height(16.dp))

                    val options = listOf(1, 6, 12, 24, 48, 72)
                    options.forEach { hours ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedHours == hours,
                                onClick = { selectedHours = hours }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$hours ${if (hours == 1) "hora" else "horas"}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(selectedHours) }) {
                    Text("Limpiar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}

data class AdminTool(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val action: () -> Unit
)