package com.example.fr_front.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fr_front.network.*
import com.example.fr_front.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCheckScreen(navController: NavHostController) {
    var isLoading by remember { mutableStateOf(false) }
    var generalHealth by remember { mutableStateOf<GeneralHealthResponse?>(null) }
    var adminHealth by remember { mutableStateOf<HealthCheckResponse?>(null) }
    var systemInfo by remember { mutableStateOf<SystemInfoResponse?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var lastCheckTime by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    @RequiresApi(Build.VERSION_CODES.O)
    fun performHealthCheck() {
        scope.launch {
            isLoading = true
            errorMessage = ""

            try {
                // Health check general
                val generalResponse = RetrofitClient.apiService.getGeneralHealth()
                if (generalResponse.isSuccessful) {
                    generalHealth = generalResponse.body()
                } else {
                    errorMessage = "Error en health check general: ${generalResponse.message()}"
                }

                // Health check administrativo
                val adminResponse = RetrofitClient.apiService.getHealthCheck()
                if (adminResponse.isSuccessful) {
                    adminHealth = adminResponse.body()
                }

                // Información del sistema
                val infoResponse = RetrofitClient.apiService.getSystemInfo()
                if (infoResponse.isSuccessful) {
                    systemInfo = infoResponse.body()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    lastCheckTime = java.time.LocalDateTime.now().toString()
                }

            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Ejecutar health check al cargar
    LaunchedEffect(Unit) {
        performHealthCheck()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Check Completo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { performHealthCheck() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Verificar")
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
                // Header
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
                                Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Verificación de Salud del Sistema",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Estado completo de todos los componentes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (lastCheckTime.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Última verificación: ${lastCheckTime.substring(0, 19).replace("T", " ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
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
                                Text("Verificando salud del sistema...")
                            }
                        }
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                item {
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

            // Estado general del sistema
            generalHealth?.let { health ->
                item {
                    GeneralHealthCard(health)
                }
            }

            // Estado administrativo detallado
            adminHealth?.let { health ->
                item {
                    AdminHealthCard(health)
                }
            }

            // Información del sistema
            systemInfo?.let { info ->
                item {
                    SystemInfoCard(info)
                }
            }

            // Botón para verificación completa
            item {
                Button(
                    onClick = { performHealthCheck() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificando...")
                    } else {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificar Estado Completo")
                    }
                }
            }
        }
    }
}

@Composable
fun GeneralHealthCard(health: GeneralHealthResponse) {
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
                    "Estado General: ${health.status.uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Componentes principales
            val components = listOf(
                "Base de Datos" to health.database,
                "Procesamiento Facial" to health.facial_processing
            )

            components.forEach { (name, status) ->
                ComponentStatusRow(name, status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dependencias
            if (health.dependencies.isNotEmpty()) {
                Text(
                    "Dependencias:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                health.dependencies.forEach { (dep, status) ->
                    ComponentStatusRow(dep, status)
                }
            }
        }
    }
}

@Composable
fun AdminHealthCard(health: HealthCheckResponse) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Verificación Administrativa Detallada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Estado de componentes
            Text(
                "Componentes del Sistema:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComponentStatusRow("Base de Datos", health.components.database.status)
            ComponentStatusRow("Reconocimiento Facial", health.components.facial_recognition)
            ComponentStatusRow("Sistema de Archivos", health.components.file_system)

            Spacer(modifier = Modifier.height(12.dp))

            // Configuración del sistema
            Text(
                "Configuración Actual:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ConfigurationRow("Procesamiento Mejorado", if (health.system_config.enhanced_processing) "Habilitado" else "Deshabilitado")
            ConfigurationRow("Método de Características", health.system_config.feature_method)
            ConfigurationRow("Umbrales Adaptativos", if (health.system_config.adaptive_threshold) "Habilitado" else "Deshabilitado")

            Spacer(modifier = Modifier.height(12.dp))

            // Dependencias detalladas
            if (health.components.dependencies.isNotEmpty()) {
                Text(
                    "Estado de Dependencias:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                health.components.dependencies.forEach { (dep, available) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            dep,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (available) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (available) "Disponible" else "No disponible",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemInfoCard(info: SystemInfoResponse) {
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

            // Información básica del sistema
            ConfigurationRow("Nombre", info.system.name)
            ConfigurationRow("Versión", info.system.version)
            ConfigurationRow("Modo", info.system.mode)
            ConfigurationRow("Entorno", info.system.environment)

            Spacer(modifier = Modifier.height(12.dp))

            // Dependencias con versiones
            if (info.dependencies.isNotEmpty()) {
                Text(
                    "Versiones de Dependencias:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                info.dependencies.forEach { (dep, version) ->
                    ConfigurationRow(dep, version)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Capacidades del sistema
            if (info.capabilities.isNotEmpty()) {
                Text(
                    "Capacidades:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                info.capabilities.forEach { (capability, value) ->
                    when (value) {
                        is Boolean -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    capability,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    if (value) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        else -> {
                            ConfigurationRow(capability, value.toString())
                        }
                    }
                }
            }
        }
    }
}