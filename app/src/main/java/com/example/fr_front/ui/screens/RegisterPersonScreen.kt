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
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPersonScreen(navController: NavHostController) {
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var idEstudiante by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Función para crear URI temporal para la cámara
    fun createImageUri(format: String = "jpg"): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_${timeStamp}_"
        val storageDir = File(context.cacheDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        // Validar formato y asignar extensión apropiada
        val extension = when (format.lowercase()) {
            "png" -> ".png"
            "jpeg", "jpg" -> ".jpg"
            else -> ".jpg" // Por defecto JPG
        }

        val imageFile = File.createTempFile(
            imageFileName,
            extension,
            storageDir
        )

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    // Launcher para seleccionar imagen de galería (múltiples formatos)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    // URI temporal para la cámara
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para tomar foto con cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            selectedImageUri = tempImageUri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Persona") },
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
            // Campos de texto
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                singleLine = true
            )

            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                singleLine = true
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo Electrónico *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            OutlinedTextField(
                value = idEstudiante,
                onValueChange = { idEstudiante = it },
                label = { Text("ID Estudiante (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

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
                        "Foto de la Persona *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Foto seleccionada",
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
                                onClick = {
                                    selectedImageUri = null
                                    tempImageUri = null
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Quitar")
                            }

                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") }
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Galería")
                            }

                            OutlinedButton(
                                onClick = {
                                    tempImageUri = createImageUri("jpg") // JPG por defecto para cámara
                                    cameraLauncher.launch(tempImageUri!!)
                                }
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cámara")
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
                            "Selecciona una foto",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") }
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Galería")
                            }

                            Button(
                                onClick = {
                                    tempImageUri = createImageUri("jpg") // JPG por defecto para cámara
                                    cameraLauncher.launch(tempImageUri!!)
                                }
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cámara")
                            }
                        }
                    }
                }
            }

            // Botón de registro
            Button(
                onClick = {
                    if (nombre.isBlank() || apellidos.isBlank() || correo.isBlank() || selectedImageUri == null) {
                        resultMessage = "Por favor completa todos los campos obligatorios"
                        showResult = true
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        try {
                            val nombreBody = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
                            val apellidosBody = apellidos.toRequestBody("text/plain".toMediaTypeOrNull())
                            val correoBody = correo.toRequestBody("text/plain".toMediaTypeOrNull())
                            val idEstudianteBody = if (idEstudiante.isNotBlank()) {
                                idEstudiante.toRequestBody("text/plain".toMediaTypeOrNull())
                            } else null

                            val imagePart = createMultipartFromUri(context, selectedImageUri!!, "foto")

                            val response = RetrofitClient.apiService.registerPerson(
                                nombre = nombreBody,
                                apellidos = apellidosBody,
                                correo = correoBody,
                                idEstudiante = idEstudianteBody,
                                foto = imagePart
                            )

                            if (response.isSuccessful) {
                                val result = response.body()
                                resultMessage = """
                                    ✅ Persona registrada exitosamente!
                                    
                                    ID: ${result?.person_id}
                                    Características extraídas: ${result?.features_count}
                                    Rostros detectados: ${result?.faces_detected}
                                    Método: ${result?.processing_method}
                                    Tiempo: ${result?.processing_time}s
                                """.trimIndent()

                                // Limpiar formulario
                                nombre = ""
                                apellidos = ""
                                correo = ""
                                idEstudiante = ""
                                selectedImageUri = null
                                tempImageUri = null
                            } else {
                                resultMessage = "❌ Error: ${response.message()}"
                            }
                        } catch (e: Exception) {
                            resultMessage = "❌ Error de conexión: ${e.message}"
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
                    Text("Registrando...")
                } else {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar Persona")
                }
            }
        }
    }

    // Mostrar resultado
    if (showResult) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            title = { Text("Resultado del Registro") },
            text = { Text(resultMessage) },
            confirmButton = {
                TextButton(onClick = { showResult = false }) {
                    Text("OK")
                }
            }
        )
    }
}