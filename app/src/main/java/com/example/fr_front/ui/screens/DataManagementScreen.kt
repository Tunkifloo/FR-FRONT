package com.example.fr_front.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fr_front.network.RetrofitClient
import com.example.fr_front.utils.createMultipartFromUri
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(navController: NavHostController) {
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var exportUrl by remember { mutableStateOf("") }
    var syncStatus by remember { mutableStateOf<Map<String, Any>?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Launcher para seleccionar archivo JSON para importar
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                isLoading = true
                try {
                    val filePart = createMultipartFromUri(context, it, "file")
                    val response = RetrofitClient.apiService.importData(filePart)

                    if (response.isSuccessful) {
                        val result = response.body()
                        resultMessage = """
                            ✅ Importación completada
                            
                            📊 Resultados:
                            • Total procesados: ${result?.total ?: 0}
                            • Importados exitosamente: ${result?.successful ?: 0}
                            • Errores: ${result?.failed ?: 0}
                            
                            ${if ((result?.errors?.size ?: 0) > 0) "\n❌ Errores encontrados:\n${result?.errors?.take(3)?.joinToString("\n") { "• $it" }}" else ""}
                        """.trimIndent()
                    } else {
                        resultMessage = "❌ Error en importación: ${response.message()}"
                    }
                } catch (e: Exception) {
                    resultMessage = "❌ Error de conexión: ${e.message}"
                } finally {
                    isLoading = false
                    showResult = true
                }
            }
        }
    }

    // Cargar estado de sincronización al inicio
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.checkSynchronization()
                if (response.isSuccessful) {
                    syncStatus = response.body()?.synchronization
                }
            } catch (e: Exception) {
                // Ignorar error si el endpoint no está disponible
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Datos") },
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
                // Información sobre gestión de datos
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
                                Icons.Default.ManageAccounts,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Gestión de Datos del Sistema",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Exporta, importa y gestiona los datos del sistema de reconocimiento facial.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Estado de sincronización
            syncStatus?.let { sync ->
                item {
                    SyncStatusCard(sync)
                }
            }

            item {
                Text(
                    "Exportación de Datos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Exportar todos los datos
            item {
                DataActionCard(
                    title = "Exportar Todos los Datos",
                    description = "Descarga un archivo JSON con todas las personas y sus características faciales",
                    icon = Icons.Default.FileDownload,
                    buttonText = "Exportar Todo",
                    isLoading = isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val response = RetrofitClient.apiService.exportAllData()
                                if (response.isSuccessful) {
                                    val result = response.body()
                                    exportUrl = result?.download_url ?: ""
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
                                isLoading = false
                                showResult = true
                            }
                        }
                    }
                )
            }

            // Crear backup
            item {
                DataActionCard(
                    title = "Crear Backup del Sistema",
                    description = "Genera un respaldo completo incluyendo configuraciones y metadatos",
                    icon = Icons.Default.Backup,
                    buttonText = "Crear Backup",
                    isLoading = isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val response = RetrofitClient.apiService.createBackup()
                                if (response.isSuccessful) {
                                    val result = response.body()
                                    resultMessage = """
                                        ✅ ${result?.message}
                                        
                                        El backup se ha creado exitosamente.
                                    """.trimIndent()
                                } else {
                                    resultMessage = "❌ Error creando backup: ${response.message()}"
                                }
                            } catch (e: Exception) {
                                resultMessage = "❌ Error de conexión: ${e.message}"
                            } finally {
                                isLoading = false
                                showResult = true
                            }
                        }
                    }
                )
            }

            item {
                Text(
                    "Importación de Datos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Importar datos
            item {
                DataActionCard(
                    title = "Importar Datos desde JSON",
                    description = "Sube un archivo JSON con datos de personas para importar al sistema",
                    icon = Icons.Default.FileUpload,
                    buttonText = "Seleccionar Archivo",
                    isLoading = isLoading,
                    onClick = {
                        importLauncher.launch("application/json")
                    }
                )
            }

            item {
                Text(
                    "Herramientas de Mantenimiento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Verificar sincronización
            item {
                DataActionCard(
                    title = "Verificar Sincronización",
                    description = "Comprueba la consistencia entre base de datos, archivos JSON y modelos",
                    icon = Icons.Default.Sync,
                    buttonText = "Verificar",
                    isLoading = isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val response = RetrofitClient.apiService.checkSynchronization()
                                if (response.isSuccessful) {
                                    val result = response.body()
                                    syncStatus = result?.synchronization

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
                                isLoading = false
                                showResult = true
                            }
                        }
                    }
                )
            }

            // Limpiar sistema
            item {
                DataActionCard(
                    title = "Limpiar Archivos Temporales",
                    description = "Elimina archivos temporales y optimiza el espacio de almacenamiento",
                    icon = Icons.Default.CleaningServices,
                    buttonText = "Limpiar",
                    isLoading = isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val response = RetrofitClient.apiService.cleanupSystem()
                                if (response.isSuccessful) {
                                    val result = response.body()
                                    resultMessage = """
                                        ✅ ${result?.message}
                                        
                                        🧹 Limpieza completada
                                        ⏱️ Archivos de más de ${result?.max_age_hours ?: 24} horas eliminados
                                    """.trimIndent()
                                } else {
                                    resultMessage = "❌ Error en limpieza: ${response.message()}"
                                }
                            } catch (e: Exception) {
                                resultMessage = "❌ Error de conexión: ${e.message}"
                            } finally {
                                isLoading = false
                                showResult = true
                            }
                        }
                    }
                )
            }
        }
    }

    // Mostrar resultado
    if (showResult) {
        AlertDialog(
            onDismissRequest = {
                showResult = false
                exportUrl = ""
            },
            title = { Text("Resultado de la Operación") },
            text = {
                Text(
                    resultMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showResult = false
                    exportUrl = ""
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                if (exportUrl.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            // Intentar abrir el URL de descarga
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(exportUrl))
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Descargar")
                    }
                }
            }
        )
    }
}

@Composable
fun DataActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (status) {
                        "perfect" -> Icons.Default.CheckCircle
                        "inconsistent" -> Icons.Default.Warning
                        else -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = when (status) {
                        "perfect" -> MaterialTheme.colorScheme.primary
                        "inconsistent" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Estado de Sincronización: ${status.uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SyncMetric(
                    label = "BD",
                    value = dbRecords.toInt().toString(),
                    icon = Icons.Default.Storage
                )
                SyncMetric(
                    label = "JSON",
                    value = jsonBackups.toInt().toString(),
                    icon = Icons.Default.Description
                )
                SyncMetric(
                    label = "Modelos",
                    value = pickleModels.toInt().toString(),
                    icon = Icons.Default.Psychology
                )
            }

            if (recommendation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "💡 $recommendation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SyncMetric(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
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
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}(
verticalAlignment = Alignment.CenterVertically
) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    Spacer(modifier = Modifier.width(12.dp))

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
}

Spacer(modifier = Modifier.height(12.dp))

Button(
onClick = onClick,
modifier = Modifier.fillMaxWidth(),
enabled = !isLoading
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Procesando...")
    } else {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(buttonText)
    }
}
}
}
}

@Composable
fun SyncStatusCard(syncStatus: Map<String, Any>) {
    val status = syncStatus["status"] as? String ?: "unknown"
    val dbRecords = syncStatus["database_records"] as? Double ?: 0.0
    val jsonBackups = syncStatus["json_backups"] as? Double ?: 0.0
    val pickleModels = syncStatus["pickle_models"] as? Double ?: 0.0
    val recommendation = syncStatus["recommendation"] as? String ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                "perfect" -> MaterialTheme.colorScheme.primaryContainer
                "inconsistent" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row