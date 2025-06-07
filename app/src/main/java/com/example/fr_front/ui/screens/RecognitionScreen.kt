package com.example.fr_front.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.fr_front.network.RetrofitClient
import com.example.fr_front.utils.createMultipartFromUri
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognitionScreen(navController: NavHostController) {
    var searchType by remember { mutableStateOf("email") } // "email" o "student_id"
    var searchValue by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var recognitionResult by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

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
                title = { Text("Reconocimiento Facial") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
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
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Reconocimiento vs Persona Específica",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Compara una foto con una persona específica ya registrada en el sistema.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Tipo de búsqueda
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Buscar persona por:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { searchType = "email" },
                            label = { Text("Email") },
                            selected = searchType == "email",
                            leadingIcon = if (searchType == "email") {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else {
                                { Icon(Icons.Default.Email, contentDescription = null) }
                            }
                        )

                        FilterChip(
                            onClick = { searchType = "student_id" },
                            label = { Text("ID Estudiante") },
                            selected = searchType == "student_id",
                            leadingIcon = if (searchType == "student_id") {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else {
                                { Icon(Icons.Default.Badge, contentDescription = null) }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchValue,
                        onValueChange = { searchValue = it },
                        label = {
                            Text(if (searchType == "email") "Correo Electrónico" else "ID Estudiante")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                if (searchType == "email") Icons.Default.Email else Icons.Default.Badge,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (searchType == "email") KeyboardType.Email else KeyboardType.Number
                        ),
                        singleLine = true
                    )
                }
            }

            // Sección de imagen para comparar
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Foto para Comparar *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Foto para comparar",
                            modifier = Modifier
                                .size(200.dp)
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
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Selecciona la foto a comparar",
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

            // Botón de reconocimiento
            Button(
                onClick = {
                    if (searchValue.isBlank() || selectedImageUri == null) {
                        recognitionResult = "❌ Por favor completa todos los campos"
                        showResult = true
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        try {
                            val imagePart = createMultipartFromUri(context, selectedImageUri!!, "test_image")

                            val response = if (searchType == "email") {
                                val emailBody = searchValue.toRequestBody("text/plain".toMediaTypeOrNull())
                                RetrofitClient.apiService.recognizeByEmail(emailBody, imagePart)
                            } else {
                                val studentIdBody = searchValue.toRequestBody("text/plain".toMediaTypeOrNull())
                                RetrofitClient.apiService.recognizeByStudentId(studentIdBody, imagePart)
                            }

                            if (response.isSuccessful) {
                                val result = response.body()
                                val person = result?.person
                                val recognition = result?.recognition_result

                                recognitionResult = """
                                    ${if (recognition?.is_match == true) "✅ COINCIDENCIA ENCONTRADA" else "❌ NO HAY COINCIDENCIA"}
                                    
                                    👤 Persona: ${person?.nombre} ${person?.apellidos}
                                    📧 Email: ${person?.correo}
                                    🆔 ID Estudiante: ${person?.id_estudiante ?: "N/A"}
                                    
                                    📊 Resultado del Análisis:
                                    • Similitud: ${String.format("%.2f%%", (recognition?.similarity ?: 0.0) * 100)}
                                    • Umbral: ${String.format("%.2f%%", (recognition?.threshold ?: 0.0) * 100)}
                                    • Confianza: ${String.format("%.2f%%", (recognition?.confidence ?: 0.0) * 100)}
                                    • Rostros detectados: ${recognition?.faces_detected}
                                    • Características: ${recognition?.features_compared}
                                    • Método: ${recognition?.processing_method ?: "N/A"}
                                    
                                    ${if (recognition?.cosine_similarity != null) """
                                    📈 Métricas Detalladas:
                                    • Similitud Coseno: ${String.format("%.3f", recognition.cosine_similarity)}
                                    • Correlación: ${String.format("%.3f", recognition.correlation ?: 0.0)}
                                    • Similitud Euclidiana: ${String.format("%.3f", recognition.euclidean_similarity ?: 0.0)}
                                    • Similitud Manhattan: ${String.format("%.3f", recognition.manhattan_similarity ?: 0.0)}
                                    """ else ""}
                                """.trimIndent()
                            } else {
                                recognitionResult = "❌ Error: ${response.message()}"
                            }
                        } catch (e: Exception) {
                            recognitionResult = "❌ Error de conexión: ${e.message}"
                        } finally {
                            isLoading = false
                            showResult = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analizando...")
                } else {
                    Icon(Icons.Default.Face, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Realizar Reconocimiento")
                }
            }
        }
    }

    // Mostrar resultado
    if (showResult) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            title = { Text("Resultado del Reconocimiento") },
            text = {
                Text(
                    recognitionResult,
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