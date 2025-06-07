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
import com.example.fr_front.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemInfoDetailScreen(navController: NavHostController) {
    var isLoading by remember { mutableStateOf(false) }
    var systemInfo by remember { mutableStateOf<SystemInfoResponse?>(null) }
    var rootInfo by remember { mutableStateOf<RootInfoResponse?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var lastUpdateTime by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun loadSystemInfo() {
        scope.launch {
            isLoading = true
            errorMessage = ""

            try {
                // Cargar información del sistema
                val infoResponse = RetrofitClient.apiService.getSystemInfo()
                if (infoResponse.isSuccessful) {
                    systemInfo = infoResponse.body()
                } else {
                    errorMessage = "Error cargando información: ${infoResponse.message()}"
                }

                // Cargar información raíz
                val rootResponse = RetrofitClient.apiService.getRootInfo()
                if (rootResponse.isSuccessful) {
                    rootInfo = rootResponse.body()
                }

                lastUpdateTime = java.time.LocalDateTime.now().toString()

            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar información al inicio
    LaunchedEffect(Unit) {
        loadSystemInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Información del Sistema") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { loadSystemInfo() }) {
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
                // Header de información del sistema
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
                                Icons.Default.Computer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Información Detallada del Sistema",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Detalles técnicos y configuración",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (lastUpdateTime.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Última actualización: ${lastUpdateTime.substring(0, 19).replace("T", " ")}",
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
                                Text("Cargando información del sistema...")
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

            // Información raíz del sistema
            rootInfo?.let { root ->
                item {
                    SystemRootInfoCard(root)
                }
            }

            // Información detallada del sistema
            systemInfo?.let { info ->
                item {
                    SystemBasicInfoCard(info.system)
                }

                item {
                    SystemDependenciesCard(info.dependencies)
                }

                item {
                    SystemCapabilitiesCard(info.capabilities)
                }

                item {
                    SystemConfigurationCard(info.configuration)
                }

                item {
                    SystemDatabaseCard(info.database)
                }
            }

            // Acciones rápidas
            item {
                QuickActionsCard(navController)
            }
        }
    }
}

@Composable
fun SystemRootInfoCard(rootInfo: RootInfoResponse) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Api,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Información de la API",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("Mensaje", rootInfo.message)
            InfoRow("Versión", rootInfo.version)
            InfoRow("Estado", rootInfo.status)

            Spacer(modifier = Modifier.height(12.dp))

            // Características del sistema
            if (rootInfo.features.isNotEmpty()) {
                Text(
                    "Características:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                rootInfo.features.forEach { (feature, value) ->
                    when (value) {
                        is Boolean -> {
                            FeatureStatusRow(feature, value)
                        }
                        else -> {
                            InfoRow(feature, value.toString())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Endpoints disponibles
            if (rootInfo.endpoints.isNotEmpty()) {
                Text(
                    "Endpoints Principales:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                rootInfo.endpoints.forEach { (name, path) ->
                    InfoRow(name, path)
                }
            }
        }
    }
}

@Composable
fun SystemBasicInfoCard(systemInfo: SystemInfoSystem) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Información Básica",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("Nombre", systemInfo.name)
            InfoRow("Versión", systemInfo.version)
            InfoRow("Modo", systemInfo.mode)
            InfoRow("Entorno", systemInfo.environment)
        }
    }
}

@Composable
fun SystemDependenciesCard(dependencies: Map<String, String>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Extension,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Dependencias del Sistema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (dependencies.isNotEmpty()) {
                dependencies.forEach { (dependency, version) ->
                    DependencyRow(dependency, version)
                }
            } else {
                Text(
                    "No hay información de dependencias disponible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SystemCapabilitiesCard(capabilities: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Capacidades del Sistema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (capabilities.isNotEmpty()) {
                capabilities.forEach { (capability, value) ->
                    when (value) {
                        is Boolean -> {
                            FeatureStatusRow(capability, value)
                        }
                        else -> {
                            InfoRow(capability, value.toString())
                        }
                    }
                }
            } else {
                Text(
                    "No hay información de capacidades disponible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SystemConfigurationCard(configuration: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Configuración Actual",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (configuration.isNotEmpty()) {
                configuration.forEach { (config, value) ->
                    when (value) {
                        is Boolean -> {
                            FeatureStatusRow(config, value)
                        }
                        is Number -> {
                            InfoRow(config, value.toString())
                        }
                        else -> {
                            InfoRow(config, value.toString())
                        }
                    }
                }
            } else {
                Text(
                    "No hay información de configuración disponible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SystemDatabaseCard(database: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Información de Base de Datos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (database.isNotEmpty()) {
                database.forEach { (key, value) ->
                    InfoRow(key, value.toString())
                }
            } else {
                Text(
                    "No hay información de base de datos disponible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickActionsCard(navController: NavHostController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("health_check") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.HealthAndSafety, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Health Check")
                }

                OutlinedButton(
                    onClick = { navController.navigate("stats") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Estadísticas")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { navController.navigate("admin") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Admin Panel")
                }

                Button(
                    onClick = { navController.navigate("advanced_config") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Configuración")
                }
            }
        }
    }
}

@Composable
fun DependencyRow(dependency: String, version: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            dependency,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = when {
                version.contains("not_installed") || version.contains("missing") ->
                    MaterialTheme.colorScheme.errorContainer
                version.contains("available") ->
                    MaterialTheme.colorScheme.primaryContainer
                else ->
                    MaterialTheme.colorScheme.secondaryContainer
            }
        ) {
            Text(
                version,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    version.contains("not_installed") || version.contains("missing") ->
                        MaterialTheme.colorScheme.onErrorContainer
                    version.contains("available") ->
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else ->
                        MaterialTheme.colorScheme.onSecondaryContainer
                },
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FeatureStatusRow(feature: String, enabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            feature.replace("_", " ").replaceFirstChar { it.uppercase() },
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
                    if (enabled) "Habilitado" else "Deshabilitado",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline
                )
            }
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}