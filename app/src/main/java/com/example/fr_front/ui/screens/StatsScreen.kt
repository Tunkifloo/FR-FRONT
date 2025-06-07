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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fr_front.network.RetrofitClient
import com.example.fr_front.network.SystemStatsResponse
import com.example.fr_front.network.HealthCheckResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavHostController) {
    var systemStats by remember { mutableStateOf<SystemStatsResponse?>(null) }
    var healthCheck by remember { mutableStateOf<HealthCheckResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isLoading = true
            errorMessage = ""

            try {
                // Cargar estadísticas del sistema
                val statsResponse = RetrofitClient.apiService.getSystemStats()
                if (statsResponse.isSuccessful) {
                    systemStats = statsResponse.body()
                } else {
                    errorMessage = "Error cargando estadísticas: ${statsResponse.message()}"
                }

                // Cargar health check
                val healthResponse = RetrofitClient.apiService.getHealthCheck()
                if (healthResponse.isSuccessful) {
                    healthCheck = healthResponse.body()
                }

            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar datos al inicio
    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas del Sistema") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { loadData() }) {
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
                // Estado de carga
                if (isLoading) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Cargando estadísticas...")
                            }
                        }
                    }
                }

                // Error
                if (errorMessage.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Estado del Sistema
            healthCheck?.let { health ->
                item {
                    SystemHealthCard(health)
                }
            }

            // Información del Sistema
            systemStats?.system_info?.let { info ->
                item {
                    SystemInfoCard(info)
                }
            }

            // Estadísticas de Base de Datos
            systemStats?.database_statistics?.let { dbStats ->
                item {
                    DatabaseStatsCard(dbStats)
                }
            }

            // Configuración Actual
            systemStats?.configuration?.let { config ->
                item {
                    ConfigurationCard(config)
                }
            }

            // Sistema de Archivos
            systemStats?.file_system?.let { fileSystem ->
                item {
                    FileSystemCard(fileSystem)
                }
            }
        }
    }
}

@Composable
fun SystemHealthCard(health: HealthCheckResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (health.status) {
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
                    when (health.status) {
                        "healthy" -> Icons.Default.CheckCircle
                        "degraded" -> Icons.Default.Warning
                        else -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = when (health.status) {
                        "healthy" -> MaterialTheme.colorScheme.primary
                        "degraded" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Estado del Sistema: ${health.status.uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Componentes del sistema
            Text(
                "Componentes:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ComponentStatus(
                    name = "Base de Datos",
                    status = health.components.database.status,
                    icon = Icons.Default.Storage
                )

                ComponentStatus(
                    name = "Reconocimiento",
                    status = if (health.components.facial_recognition == "ready") "ready" else "error",
                    icon = Icons.Default.Face
                )

                ComponentStatus(
                    name = "Archivos",
                    status = health.components.file_system,
                    icon = Icons.Default.Folder
                )
            }
        }
    }
}

@Composable
fun ComponentStatus(name: String, status: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = when (status) {
                "ready", "connected" -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.error
            }
        )
        Text(
            name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            status,
            style = MaterialTheme.typography.labelSmall,
            color = when (status) {
                "ready", "connected" -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.error
            }
        )
    }
}

@Composable
fun SystemInfoCard(info: com.example.fr_front.network.SystemInfoDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Información del Sistema",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Versión", value = info.version)
            InfoRow(label = "Estado", value = info.status)
            InfoRow(label = "Base de Datos", value = info.database)
            InfoRow(label = "Procesamiento Mejorado", value = if (info.enhanced_processing) "Habilitado" else "Deshabilitado")
            InfoRow(label = "Método de Características", value = info.feature_method)
            InfoRow(label = "Umbral por Defecto", value = "${(info.default_threshold * 100).toInt()}%")
        }
    }
}

@Composable
fun DatabaseStatsCard(dbStats: com.example.fr_front.network.DatabaseStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Estadísticas de Base de Datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Personas",
                    value = dbStats.total_persons.toString(),
                    icon = Icons.Default.People
                )

                StatCard(
                    title = "Modelos",
                    value = dbStats.total_models.toString(),
                    icon = Icons.Default.Psychology
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(label = "MySQL", value = dbStats.mysql_version)
            dbStats.first_register?.let {
                InfoRow(label = "Primer Registro", value = it.substring(0, 10))
            }
            dbStats.last_register?.let {
                InfoRow(label = "Último Registro", value = it.substring(0, 10))
            }

            // Métodos de procesamiento
            if (dbStats.methods.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Por Método:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                dbStats.methods.forEach { (method, stats) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            method,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${stats.cantidad} personas",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigurationCard(config: com.example.fr_front.network.Configuration) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Configuración Actual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ConfigRow(
                label = "Procesamiento Mejorado",
                value = config.enhanced_processing,
                icon = Icons.Default.Enhancement
            )
            ConfigRow(
                label = "Método de Características",
                value = config.feature_method,
                icon = Icons.Default.Psychology
            )
            ConfigRow(
                label = "Umbral Adaptativo",
                value = config.adaptive_threshold,
                icon = Icons.Default.Tune
            )
            ConfigRow(
                label = "Detectores Múltiples",
                value = config.use_multiple_detectors,
                icon = Icons.Default.CameraAlt
            )
            ConfigRow(
                label = "dlib Habilitado",
                value = config.use_dlib,
                icon = Icons.Default.Settings
            )

            InfoRow(
                label = "Umbral por Defecto",
                value = "${(config.default_threshold * 100).toInt()}%"
            )
        }
    }
}

@Composable
fun FileSystemCard(fileSystem: com.example.fr_front.network.FileSystemStats) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Sistema de Archivos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(
                label = "Total de Archivos",
                value = fileSystem.total_files.toString()
            )
            InfoRow(
                label = "Tamaño Total",
                value = fileSystem.total_size_formatted
            )

            // Uso de disco si está disponible
            fileSystem.disk_usage?.let { diskUsage ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Uso de Disco:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total", style = MaterialTheme.typography.labelMedium)
                        Text(diskUsage.total, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column {
                        Text("Usado", style = MaterialTheme.typography.labelMedium)
                        Text(diskUsage.used, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column {
                        Text("Libre", style = MaterialTheme.typography.labelMedium)
                        Text(diskUsage.free, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column {
                        Text("Uso", style = MaterialTheme.typography.labelMedium)
                        Text("${diskUsage.usage_percent.toInt()}%", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Directorios
            if (fileSystem.directories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Directorios:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                fileSystem.directories.forEach { (directory, info) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            directory,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${info.files} archivos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            info.size_formatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
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
fun InfoRow(label: String, value: String) {
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
fun ConfigRow(
    label: String,
    value: Any,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        when (value) {
            is Boolean -> {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (value) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (value) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (value) "Sí" else "No",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (value) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            is String -> {
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            else -> {
                Text(
                    value.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}