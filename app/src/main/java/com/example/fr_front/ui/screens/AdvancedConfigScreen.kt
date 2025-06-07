package com.example.fr_front.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.fr_front.network.SystemConfigResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedConfigScreen(navController: NavHostController) {
    var isLoading by remember { mutableStateOf(false) }
    var systemConfig by remember { mutableStateOf<SystemConfigResponse?>(null) }
    var showWarning by remember { mutableStateOf(false) }
    var warningMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // Cargar configuración actual
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.apiService.getSystemConfig()
                if (response.isSuccessful) {
                    systemConfig = response.body()
                }
            } catch (e: Exception) {
                // Manejar error
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración Avanzada") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
                // Advertencia de configuración avanzada
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Configuración Avanzada",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ Esta sección es solo para lectura desde la aplicación móvil. Los cambios de configuración deben realizarse directamente en el servidor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

            systemConfig?.let { config ->
                // Configuración de Reconocimiento Facial
                item {
                    ConfigSectionCard(
                        title = "Reconocimiento Facial",
                        icon = Icons.Default.Face
                    ) {
                        ConfigDisplayItem(
                            "Procesamiento Mejorado",
                            if (config.facial_recognition.enhanced_processing) "Habilitado" else "Deshabilitado",
                            config.facial_recognition.enhanced_processing
                        )
                        ConfigDisplayItem(
                            "Método de Características",
                            config.facial_recognition.feature_method,
                            true
                        )
                        ConfigDisplayItem(
                            "Umbral por Defecto",
                            "${(config.facial_recognition.default_threshold * 100).toInt()}%",
                            true
                        )
                        ConfigDisplayItem(
                            "Umbrales Adaptativos",
                            if (config.facial_recognition.adaptive_threshold) "Habilitado" else "Deshabilitado",
                            config.facial_recognition.adaptive_threshold
                        )
                        ConfigDisplayItem(
                            "Detectores Múltiples",
                            if (config.facial_recognition.multiple_detectors) "Habilitado" else "Deshabilitado",
                            config.facial_recognition.multiple_detectors
                        )
                        ConfigDisplayItem(
                            "dlib",
                            if (config.facial_recognition.use_dlib) "Habilitado" else "Deshabilitado",
                            config.facial_recognition.use_dlib
                        )
                    }
                }

                // Configuración de Umbrales
                item {
                    ConfigSectionCard(
                        title = "Umbrales de Similitud",
                        icon = Icons.Default.Tune
                    ) {
                        ConfigDisplayItem(
                            "Umbral por Defecto",
                            "${(config.thresholds.default * 100).toInt()}%",
                            true
                        )
                        ConfigDisplayItem(
                            "Umbral Mínimo",
                            "${(config.thresholds.min * 100).toInt()}%",
                            true
                        )
                        ConfigDisplayItem(
                            "Umbral Máximo",
                            "${(config.thresholds.max * 100).toInt()}%",
                            true
                        )
                    }
                }

                // Pesos de Comparación (si están disponibles)
                config.comparison_weights?.let { weights ->
                    item {
                        ConfigSectionCard(
                            title = "Pesos de Comparación Multi-Métrica",
                            icon = Icons.Default.Analytics
                        ) {
                            weights.forEach { (metric, weight) ->
                                ConfigDisplayItem(
                                    metric.replaceFirstChar { it.uppercase() },
                                    "${(weight * 100).toInt()}%",
                                    true
                                )
                            }
                        }
                    }
                }

                // Configuración del Sistema
                item {
                    ConfigSectionCard(
                        title = "Sistema General",
                        icon = Icons.Default.Settings
                    ) {
                        ConfigDisplayItem(
                            "Modo Debug",
                            if (config.system.debug) "Habilitado" else "Deshabilitado",
                            config.system.debug
                        )
                        ConfigDisplayItem(
                            "Nivel de Log",
                            config.system.log_level,
                            true
                        )
                    }
                }

                // Directorios
                item {
                    ConfigSectionCard(
                        title = "Directorios del Sistema",
                        icon = Icons.Default.Folder
                    ) {
                        ConfigDisplayItem(
                            "Uploads",
                            config.directories.upload,
                            true
                        )
                        ConfigDisplayItem(
                            "Modelos",
                            config.directories.models,
                            true
                        )
                        ConfigDisplayItem(
                            "Backups",
                            config.directories.backup,
                            true
                        )
                        ConfigDisplayItem(
                            "JSON Backup",
                            config.directories.json_backup,
                            true
                        )
                    }
                }

                // Información adicional
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
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
                                    "Información Importante",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "• Los cambios de configuración deben realizarse en el archivo .env del servidor",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "• Después de cambiar la configuración, reinicia el servidor",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "• El procesamiento mejorado requiere scikit-learn instalado",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "• Los umbrales adaptativos mejoran la precisión automáticamente",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Botones de acción
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.navigate("health_check") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Health Check")
                        }

                        Button(
                            onClick = { navController.navigate("stats") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver Estadísticas")
                        }
                    }
                }
            }
        }
    }

    // Mostrar advertencia si es necesario
    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            title = { Text("Advertencia") },
            text = { Text(warningMessage) },
            confirmButton = {
                TextButton(onClick = { showWarning = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
fun ConfigSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
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
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
fun ConfigDisplayItem(
    label: String,
    value: String,
    isGood: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isGood) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ) {
            Text(
                value,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = if (isGood) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )
        }
    }
}