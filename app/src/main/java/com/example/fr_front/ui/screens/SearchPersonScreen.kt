package com.example.fr_front.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fr_front.network.PersonResponse
import com.example.fr_front.network.RetrofitClient
import com.example.fr_front.utils.formatDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPersonScreen(navController: NavHostController) {
    var searchType by remember { mutableStateOf("email") }
    var searchValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var foundPerson by remember { mutableStateOf<PersonResponse?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Persona") },
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
                            Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Búsqueda de Personas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Busca información de una persona registrada usando su email o ID de estudiante.",
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
                        "Buscar por:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = {
                                searchType = "email"
                                searchValue = ""
                                errorMessage = ""
                                showResult = false
                            },
                            label = { Text("Email") },
                            selected = searchType == "email",
                            leadingIcon = if (searchType == "email") {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else {
                                { Icon(Icons.Default.Email, contentDescription = null) }
                            }
                        )

                        FilterChip(
                            onClick = {
                                searchType = "student_id"
                                searchValue = ""
                                errorMessage = ""
                                showResult = false
                            },
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
                        onValueChange = {
                            searchValue = it
                            errorMessage = ""
                            showResult = false
                        },
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
                        singleLine = true,
                        placeholder = {
                            Text(
                                if (searchType == "email")
                                    "ejemplo@universidad.edu"
                                else
                                    "123456789"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (searchValue.isBlank()) {
                                errorMessage = "Por favor ingresa un valor para buscar"
                                return@Button
                            }

                            scope.launch {
                                isLoading = true
                                errorMessage = ""
                                foundPerson = null

                                try {
                                    val response = if (searchType == "email") {
                                        RetrofitClient.apiService.getPersonByEmail(searchValue.trim())
                                    } else {
                                        RetrofitClient.apiService.getPersonByStudentId(searchValue.trim())
                                    }

                                    if (response.isSuccessful) {
                                        foundPerson = response.body()
                                        showResult = true
                                    } else {
                                        when (response.code()) {
                                            404 -> errorMessage = "❌ Persona no encontrada"
                                            400 -> errorMessage = "❌ Formato inválido"
                                            else -> errorMessage = "❌ Error: ${response.message()}"
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "❌ Error de conexión: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && searchValue.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscando...")
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscar")
                        }
                    }
                }
            }

            // Mostrar error
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

            // Mostrar resultado
            if (showResult && foundPerson != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Persona Encontrada",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Información básica usando Text simple (más seguro)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Nombre completo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "👤 Nombre:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "${foundPerson!!.nombre} ${foundPerson!!.apellidos}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Email
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "📧 Email:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    foundPerson!!.correo,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // ID Estudiante (solo si no es null)
                            foundPerson!!.id_estudiante?.let { studentId ->
                                if (studentId.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "🆔 ID Estudiante:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            studentId,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            // ID Interno
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "🔑 ID Interno:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "#${foundPerson!!.id}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Fecha de registro
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "📅 Registro:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    formatDate(foundPerson!!.fecha_registro),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Estado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "⚡ Estado:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    if (foundPerson!!.isActive) "✅ Activo" else "❌ Inactivo",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botones de acción
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    searchValue = ""
                                    foundPerson = null
                                    showResult = false
                                    errorMessage = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Nueva Búsqueda")
                            }

                            Button(
                                onClick = { navController.navigate("recognition") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Face, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reconocer")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String?
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
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}