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
import com.example.fr_front.network.RetrofitClient
import kotlinx.coroutines.launch

data class Tool(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val category: String,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(navController: NavHostController) {
    var isExecuting by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Definir todas las herramientas disponibles
    val tools = remember {
        listOf(
            // Herramientas de Diagnóstico
            Tool(
                title = "Health Check Completo",
                description = "Verificar estado de todos los componentes del sistema",
                icon = Icons.Default.HealthAndSafety,
                category = "Diagnóstico",
                action = { navController.navigate("health_check") }
            ),
            Tool(
                title = "Verificar Integridad",
                description = "Comprobar integridad de datos y consistencia del sistema",
                icon = Icons.Default.Security,
                category = "Diagnóstico",
                action = {
                    scope.launch {
                        isExecuting = true
                        try {
                            val response = RetrofitClient.apiService.checkSystemIntegrity()
                            if (response.isSuccessful) {
                                val result = response.body()
                                resultMessage = """
                                    ✅ Verificación de Integridad Completada
                                    
                                    Estado General: ${result?.integrity_check?.overall_status?.uppercase()}
                                    
                                    Base de Datos: ${result?.integrity_check?.database?.status}
                                    Conexión: ${if (result?.integrity_check?.database?.connection == true) "OK" else "Error"}
                                    
                                    ${if (result?.recommendations?.isNotEmpty() == true)
                                    "💡 Recomendaciones:\n${result.recommendations.take(3).joinToString("\n") { "• $it" }}"
                                else "✅ No se encontraron problemas"}
                                """.trimIndent()
                            } else {
                                resultMessage = "❌ Error verificando integridad: ${response.message()}"
                            }
                        } catch (e: Exception) {
                            resultMessage = "❌ Error de conexión: ${e.message}"
                        } finally {
                            isExecuting = false
                            showResult = true
                        }
                    }
                }
            ),
            Tool(
                title = "Estadísticas del Sistema",
                description = "Ver métricas detalladas y estadísticas de rendimiento",
                icon = Icons.Default.Analytics,
                category = "Diagnóstico",
                action = { navController.navigate("stats") }
            ),

            // Herramientas de Gestión de Datos
            Tool(
                title = "Exportar Todos los Datos",
                description = "Crear backup completo de personas y características",
                icon = Icons.Default.FileDownload,
                category = "Gestión de Datos",
                action = {
                    scope.launch {
                        isExecuting = true
                        try {
                            val response = RetrofitClient.apiService.exportAllData()
                            if (response.isSuccessful) {
                                val result = response.body()
                                resultMessage = """
                                    ✅ ${result?.message}
                                    
                                    📁 Archivo: ${result?.filename}
                                    📊 Registros exportados: ${result?.total_records}
                                    
                                    El archivo está listo para descargar.
                                """.trimIndent()
                            } else {
                                resultMessage = "❌ Error en exportación: ${response.message()}"
                            }
                        } catch (e: Exception) {
                            resultMessage = "❌ Error de conexión: ${e.message}"
                        } finally {
                            isExecuting = false
                            showResult = true
                        }
                    }
                }
            ),
            Tool(
                title = "Crear Backup",
                description = "Generar respaldo completo del sistema",
                icon = Icons.Default.Backup,
                category = "Gestión de Datos",
                action = {
                    scope.launch {
                        isExecuting = true
                        try {
                            val response = RetrofitClient.apiService.createBackup()
                            if (response.isSuccessful) {
                                val result = response.body()
                                resultMessage = """
                                    ✅ ${result?.message}
                                    
                                    Backup creado exitosamente en el servidor.
                                """.trimIndent()
                            } else {
                                resultMessage = "❌ Error creando backup: ${response.message()}"
                            }
                        } catch (e: Exception) {
                            resultMessage = "❌ Error de conexión: ${e.message}"
                        } finally {
                            isExecuting = false
                            showResult = true
                        }
                    }
                }
            ),
            Tool(
                title = "Verificar Sincronización",
                description = "Comprobar consistencia entre BD, JSON y modelos",
                icon = Icons.Default.Sync,
                category = "Gestión de Datos",
                action = {
                    scope.launch {
                        isExecuting = true
                        try {
                            val response = RetrofitClient.apiService.checkSynchronization()
                            if (response.isSuccessful) {
                                val result = response.body()
                                val sync = result?.synchronization
                                resultMessage = """
                                    📊 Estado de Sincronización: ${sync?.get("status") ?: "N/A"}
                                    
                                    📈 Estadísticas:
                                    • Registros en BD: ${sync?.get("database_records") ?: 0}
                                    • Backups JSON: ${sync?.get("json_backups") ?: 0}
                                    • Modelos Pickle: ${sync?.get("pickle_models") ?: 0}
                                    
                                    💡 ${sync?.get("recommendation") ?: "Estado verificado"}
                                """.trimIndent()
                            } else {
                                resultMessage = "❌ Error verificando sincronización: ${response.message()}"
                            }
                        } catch (e: Exception) {
                            resultMessage = "❌ Error de conexión: ${e.message}"
                        } finally {
                            isExecuting = false
                            showResult = true
                        }
                    }
                }
            ),
            Tool(
                title = "Gestión de Datos Completa",
                description = "Acceder al panel completo de gestión de datos",
                icon = Icons.Default.ManageAccounts,
                category = "Gestión de Datos",
                action = { navController.navigate("data_management") }
            ),

            // Herramientas de Mantenimiento
            Tool(
                title = "Limpiar Sistema",
                description = "Eliminar archivos temporales y optimizar espacio",
                icon = Icons.Default.CleaningServices,
                category = "Mantenimiento",
                action = {
                    scope.launch {
                        isExecuting = true
                        try {
                            val response = RetrofitClient.apiService.cleanupSystem(24)
                            if (response.isSuccessful) {
                                val result = response.body()
                                resultMessage = """
                                    ✅ ${result?.message}
                                    
                                    🧹 Limpieza completada
                                    ⏱️ Archivos de más de 24 horas eliminados
                                """.trimIndent()
                            } else {
                                resultMessage = "❌ Error en limpieza: ${response.message()}"
                            }
                        } catch (e: Exception) {
                            resultMessage = "❌ Error de conexión: ${e.message}"
                        } finally {
                            isExecuting = false
                            showResult = true
                        }
                    }
                }
            ),
            Tool(
                title = "Métricas de Rendimiento",
                description = "Obtener métricas detalladas de rendimiento del sistema",
                icon = Icons.Default.Speed,
                category = "Mantenimiento",
                action = {
                    scope.launch {
                        isExecuting = true
                        try {
                            val response = RetrofitClient.apiService.getPerformanceMetrics()
                            if (response.isSuccessful) {
                                val result = response.body()
                                resultMessage = """
                                    📊 Métricas de Rendimiento
                                    
                                    📈 Base de Datos:
                                    • Total Personas: ${result?.database_performance?.total_persons ?: 0}
                                    • Total Características: ${result?.database_performance?.total_features ?: 0}
                                    
                                    💻 Recursos del Sistema:
                                    • Dependencias: ${if (result?.system_resources?.dependencies_ok == true) "✅ OK" else "❌ Error"}
                                    
                                    ⚙️ Configuración:
                                    • Procesamiento Mejorado: ${if (result?.configuration_status?.enhanced_processing == true) "✅" else "❌"}
                                    • Umbrales Adaptativos: ${if (result?.configuration_status?.adaptive_thresholds == true) "✅" else "❌"}
                                    • Detectores Múltiples: ${if (result?.configuration_status?.multiple_detectors == true) "✅" else "❌"}
                                """.trimIndent()
                            } else {
                                resultMessage = "❌ Error obteniendo métricas: ${response.message()}"
                            }
                        } catch (e: Exception) {
                            resultMessage = "❌ Error de conexión: ${e.message}"
                        } finally {
                            isExecuting = false
                            showResult = true
                        }
                    }
                }
            ),

            // Herramientas de Configuración
            Tool(
                title = "Ver Configuración",
                description = "Mostrar configuración actual del sistema",
                icon = Icons.Default.Settings,
                category = "Configuración",
                action = { navController.navigate("advanced_config") }
            ),
            Tool(
                title = "Información del Sistema",
                description = "Detalles técnicos completos del sistema",
                icon = Icons.Default.Computer,
                category = "Configuración",
                action = { navController.navigate("system_info") }
            ),
            Tool(
                title = "Panel de Administración",
                description = "Acceso completo a herramientas administrativas",
                icon = Icons.Default.AdminPanelSettings,
                category = "Configuración",
                action = { navController.navigate("admin") }
            ),

            // Herramientas de Reconocimiento
            Tool(
                title = "Estadísticas de Reconocimiento",
                description = "Métricas específicas del sistema de reconocimiento facial",
                icon = Icons.Default.Face,
                category = "Reconocimiento",
                action = {
                    scope.launch {
                        isExecuting = true
                        try {
                            val response = RetrofitClient.apiService.getRecognitionStats()
                            if (response.isSuccessful) {
                                val result = response.body()
                                resultMessage = """
                                    🧠 Estadísticas de Reconocimiento Facial
                                    
                                    📊 Sistema:
                                    • Total Personas: ${result?.system_stats?.total_personas ?: 0}
                                    
                                    ⚙️ Configuración Actual:
                                    • Procesamiento Mejorado: ${if (result?.configuration?.enhanced_processing == true) "✅" else "❌"}
                                    • Método: ${result?.configuration?.feature_method ?: "N/A"}
                                    • Umbral por Defecto: ${((result?.configuration?.default_threshold ?: 0.0) * 100).toInt()}%
                                    • Umbrales Adaptativos: ${if (result?.configuration?.adaptive_threshold == true) "✅" else "❌"}
                                    • Detectores Múltiples: ${if (result?.configuration?.use_multiple_detectors == true) "✅" else "❌"}
                                    • dlib: ${if (result?.configuration?.use_dlib == true) "✅" else "❌"}
                                    
                                    🔧 Características:
                                    • Método: ${result?.feature_config?.method ?: "N/A"}
                                    • Normalización: ${if (result?.feature_config?.normalize == true) "✅" else "❌"}
                                    • Tamaño Objetivo: ${result?.feature_config?.target_size ?: 0}
                                """.trimIndent()
                            } else {
                                resultMessage = "❌ Error obteniendo estadísticas: ${response.message()}"
                            }
                        } catch (e: Exception) {
                            resultMessage = "❌ Error de conexión: ${e.message}"
                        } finally {
                            isExecuting = false
                            showResult = true
                        }
                    }
                }
            ),
            Tool(
                title = "Estadísticas de Procesamiento",
                description = "Métricas específicas del procesamiento de personas",
                icon = Icons.Default.Psychology,
                category = "Reconocimiento",
                action = {
                    scope.launch {
                        isExecuting = true
                        try {
                            val response = RetrofitClient.apiService.getProcessingStats()
                            if (response.isSuccessful) {
                                val result = response.body()
                                resultMessage = """
                                    🔬 Estadísticas de Procesamiento
                                    
                                    📊 Base de Datos:
                                    • Total Personas: ${result?.database_stats?.total_personas ?: 0}
                                    
                                    ⚙️ Configuración Actual:
                                    • Procesamiento Mejorado: ${if (result?.current_config?.enhanced_processing == true) "✅" else "❌"}
                                    • Método de Características: ${result?.current_config?.feature_method ?: "N/A"}
                                    • Umbral por Defecto: ${((result?.current_config?.default_threshold ?: 0.0) * 100).toInt()}%
                                    • Umbrales Adaptativos: ${if (result?.current_config?.adaptive_threshold == true) "✅" else "❌"}
                                    • Detectores Múltiples: ${if (result?.current_config?.multiple_detectors == true) "✅" else "❌"}
                                    
                                    💻 Capacidades del Sistema:
                                    • dlib Disponible: ${if (result?.system_capabilities?.dlib_available == true) "✅" else "❌"}
                                    • scikit-learn Disponible: ${if (result?.system_capabilities?.sklearn_available == true) "✅" else "❌"}
                                    • Migración Habilitada: ${if (result?.system_capabilities?.migration_enabled == true) "✅" else "❌"}
                                """.trimIndent()
                            } else {
                                resultMessage = "❌ Error obteniendo estadísticas: ${response.message()}"
                            }
                        } catch (e: Exception) {
                            resultMessage = "❌ Error de conexión: ${e.message}"
                        } finally {
                            isExecuting = false
                            showResult = true
                        }
                    }
                }
            )
        )
    }

    // Agrupar herramientas por categoría
    val toolsByCategory = tools.groupBy { it.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Herramientas del Sistema") },
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
                // Header de herramientas
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
                                Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Herramientas del Sistema",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Conjunto completo de utilidades administrativas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Mostrar herramientas por categoría
            toolsByCategory.forEach { (category, categoryTools) ->
                item {
                    Text(
                        category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(categoryTools) { tool ->
                    ToolCard(
                        tool = tool,
                        isExecuting = isExecuting,
                        onExecute = tool.action
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Mostrar resultado
    if (showResult) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            title = { Text("Resultado de la Herramienta") },
            text = {
                Text(
                    resultMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showResult = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ToolCard(
    tool: Tool,
    isExecuting: Boolean,
    onExecute: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (!isExecuting) onExecute else { {} },
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
                    if (isExecuting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            tool.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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

            if (!isExecuting) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Ejecutar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}