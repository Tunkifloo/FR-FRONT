package com.example.fr_front.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.fr_front.network.RetrofitClient
import com.example.fr_front.network.IdentificationMatch
import com.example.fr_front.utils.createMultipartFromUri
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentificationScreen(navController: NavHostController) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var identificationResults by remember { mutableStateOf<List<IdentificationMatch>>(emptyList()) }
    var bestMatch by remember { mutableStateOf<IdentificationMatch?>(null) }
    var showResults by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var totalComparisons by remember { mutableStateOf(0) }
    var facesDetected by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Identificar Persona") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!showResults) {
            // Pantalla de captura de imagen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Explicación
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
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Identificación Global",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Identifica una persona comparándola contra toda la base de datos. Útil cuando no sabes quién es la persona.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Sección de imagen
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Foto de la Persona a Identificar *",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Foto a identificar",
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedImageUri = null }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Quitar")
                                }

                                OutlinedButton(
                                    onClick = { imagePickerLauncher.launch("image/*") }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cambiar")
                                }
                            }
                        } else {
                            Icon(
                                Icons.Default.PersonSearch,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Selecciona una foto para identificar",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") }
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Seleccionar Foto")
                            }
                        }
                    }
                }

                // Botón de identificación
                Button(
                    onClick = {
                        if (selectedImageUri == null) {
                            errorMessage = "Por favor selecciona una foto"
                            return@Button
                        }

                        scope.launch {
                            isLoading = true
                            try {
                                val imagePart = createMultipartFromUri(context, selectedImageUri!!, "test_image")
                                val response = RetrofitClient.apiService.identifyPerson(imagePart)

                                if (response.isSuccessful) {
                                    val result = response.body()
                                    identificationResults = result?.all_matches ?: emptyList()
                                    bestMatch = result?.identification_result?.best_match
                                    totalComparisons = result?.identification_result?.total_comparisons ?: 0
                                    facesDetected = result?.identification_result?.faces_detected ?: 0
                                    showResults = true
                                } else {
                                    errorMessage = "Error: ${response.message()}"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error de conexión: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && selectedImageUri != null
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Identificando...")
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Identificar Persona")
                    }
                }

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
        } else {
            // Pantalla de resultados
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Botón para volver
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                showResults = false
                                identificationResults = emptyList()
                                bestMatch = null
                                errorMessage = ""
                            }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                        Text(
                            "Resultados de Identificación",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    // Resumen de resultados
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (bestMatch?.is_match == true)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (bestMatch?.is_match == true) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (bestMatch?.is_match == true)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (bestMatch?.is_match == true) "Persona Identificada" else "Persona No Identificada",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("📊 Estadísticas del Análisis:")
                            Text("• Rostros detectados: $facesDetected")
                            Text("• Comparaciones realizadas: $totalComparisons")
                            Text("• Mejor similitud: ${String.format("%.2f%%", (bestMatch?.similarity ?: 0.0) * 100)}")
                        }
                    }
                }

                if (bestMatch?.is_match == true && bestMatch != null) {
                    item {
                        Text(
                            "🎯 Mejor Coincidencia",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        PersonMatchCard(bestMatch!!, isMainResult = true)
                    }
                }

                if (identificationResults.isNotEmpty()) {
                    item {
                        Text(
                            "📋 Todas las Comparaciones (Top 5)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(identificationResults.take(5)) { match ->
                        PersonMatchCard(match, isMainResult = false)
                    }
                }

                item {
                    Button(
                        onClick = {
                            showResults = false
                            identificationResults = emptyList()
                            bestMatch = null
                            selectedImageUri = null
                            errorMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nueva Identificación")
                    }
                }
            }
        }
    }
}

@Composable
fun PersonMatchCard(match: IdentificationMatch, isMainResult: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isMainResult) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isMainResult && match.is_match)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Información de la persona
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (match.is_match) Icons.Default.CheckCircle else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (match.is_match) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${match.person.nombre} ${match.person.apellidos}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        match.person.correo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!match.person.id_estudiante.isNullOrBlank()) {
                        Text(
                            "ID: ${match.person.id_estudiante}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Indicador de coincidencia
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (match.is_match)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline
                ) {
                    Text(
                        String.format("%.1f%%", match.similarity * 100),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (match.is_match)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isMainResult || match.detailed_metrics != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Métricas básicas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem("Similitud", String.format("%.3f", match.similarity))
                    MetricItem("Umbral", String.format("%.3f", match.threshold))
                    MetricItem("Confianza", String.format("%.3f", match.confidence ?: 0.0))
                }

                // Métricas detalladas si están disponibles
                match.detailed_metrics?.let { metrics ->
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Métricas Detalladas:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem("Coseno", String.format("%.3f", metrics.cosine_similarity), small = true)
                        MetricItem("Correlación", String.format("%.3f", metrics.correlation), small = true)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem("Euclidiana", String.format("%.3f", metrics.euclidean_similarity), small = true)
                        MetricItem("Manhattan", String.format("%.3f", metrics.manhattan_similarity), small = true)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem("Consistencia", String.format("%.3f", metrics.consistency), small = true)
                        MetricItem("Umbral Ajust.", String.format("%.3f", metrics.adjusted_threshold), small = true)
                    }
                }
            }

            // Información del procesamiento
            if (isMainResult) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Método: ${match.person.metodo_caracteristicas ?: "N/A"} v${match.person.version_algoritmo ?: "1.0"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, small: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = if (small) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = if (small) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}