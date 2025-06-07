package com.example.fr_front.network

import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody

interface ApiService {

    // ===== PERSONAS =====
    @Multipart
    @POST("api/persons/register")
    suspend fun registerPerson(
        @Part("nombre") nombre: RequestBody,
        @Part("apellidos") apellidos: RequestBody,
        @Part("correo") correo: RequestBody,
        @Part("id_estudiante") idEstudiante: RequestBody?,
        @Part foto: MultipartBody.Part
    ): Response<PersonRegistrationResponse>

    @GET("api/persons/list")
    suspend fun getPersons(): Response<PersonListResponse>

    @GET("api/persons/search/email/{email}")
    suspend fun getPersonByEmail(@Path("email") email: String): Response<PersonResponse>

    @GET("api/persons/search/student/{student_id}")
    suspend fun getPersonByStudentId(@Path("student_id") studentId: String): Response<PersonResponse>

    @GET("api/persons/{person_id}")
    suspend fun getPersonById(@Path("person_id") personId: Int): Response<PersonResponse>

    @Multipart
    @PUT("api/persons/{person_id}/update-features")
    suspend fun updatePersonFeatures(
        @Path("person_id") personId: Int,
        @Part foto: MultipartBody.Part
    ): Response<PersonUpdateFeaturesResponse>

    @GET("api/persons/stats/processing")
    suspend fun getProcessingStats(): Response<ProcessingStatsResponse>

    // ===== RECONOCIMIENTO =====
    @Multipart
    @POST("api/recognition/compare/email")
    suspend fun recognizeByEmail(
        @Part("email") email: RequestBody,
        @Part testImage: MultipartBody.Part
    ): Response<RecognitionResponse>

    @Multipart
    @POST("api/recognition/compare/student")
    suspend fun recognizeByStudentId(
        @Part("student_id") studentId: RequestBody,
        @Part testImage: MultipartBody.Part
    ): Response<RecognitionResponse>

    @Multipart
    @POST("api/recognition/compare/id/{person_id}")
    suspend fun recognizeByPersonId(
        @Path("person_id") personId: Int,
        @Part testImage: MultipartBody.Part
    ): Response<RecognitionResponse>

    @Multipart
    @POST("api/recognition/identify")
    suspend fun identifyPerson(
        @Part testImage: MultipartBody.Part
    ): Response<IdentificationResponse>

    @GET("api/recognition/stats")
    suspend fun getRecognitionStats(): Response<RecognitionStatsResponse>

    // ===== ADMINISTRACIÓN =====
    @GET("api/admin/stats")
    suspend fun getSystemStats(): Response<SystemStatsResponse>

    @GET("api/admin/health")
    suspend fun getHealthCheck(): Response<HealthCheckResponse>

    @GET("api/admin/integrity")
    suspend fun checkSystemIntegrity(): Response<IntegrityCheckResponse>

    @POST("api/admin/cleanup")
    suspend fun cleanupSystem(@Query("max_age_hours") maxAgeHours: Int = 24): Response<CleanupResponse>

    @GET("api/admin/config")
    suspend fun getSystemConfig(): Response<SystemConfigResponse>

    @GET("api/admin/performance")
    suspend fun getPerformanceMetrics(): Response<PerformanceMetricsResponse>

    // ===== GESTIÓN DE DATOS =====
    @GET("api/data/export/all")
    suspend fun exportAllData(): Response<ExportResponse>

    @GET("api/data/export/person/email/{email}")
    suspend fun exportPersonByEmail(@Path("email") email: String): Response<ExportResponse>

    @Multipart
    @POST("api/data/import")
    suspend fun importData(@Part file: MultipartBody.Part): Response<ImportResponse>

    @GET("api/data/download/{filename}")
    suspend fun downloadFile(@Path("filename") filename: String): Response<ResponseBody>

    @GET("api/data/backup/create")
    suspend fun createBackup(): Response<BackupResponse>

    @GET("api/data/sync/check")
    suspend fun checkSynchronization(): Response<SyncCheckResponse>

    // ===== HEALTH CHECKS =====
    @GET("health")
    suspend fun getGeneralHealth(): Response<GeneralHealthResponse>

    @GET("/")
    suspend fun getRootInfo(): Response<RootInfoResponse>

    @GET("info")
    suspend fun getSystemInfo(): Response<SystemInfoResponse>
}

// ===== DATA CLASSES EXTENDIDAS =====

// Respuestas de administración
data class IntegrityCheckResponse(
    val integrity_check: IntegrityCheck,
    val recommendations: List<String>
)

data class IntegrityCheck(
    val overall_status: String,
    val database: DatabaseStatus,
    val directories: Map<String, DirectoryStatus>,
    val data_integrity: DataIntegrity
)

data class DatabaseStatus(
    val status: String,
    val connection: Boolean
)

data class DirectoryStatus(
    val status: String,
    val files: Int,
    val size_mb: Double?,
    val message: String?
)

data class DataIntegrity(
    val status: String,
    val issues: List<String>,
    val persons_without_features: Int,
    val orphan_features: Int,
    val invalid_thresholds: Int
)

data class CleanupResponse(
    val message: String,
    val max_age_hours: Int,
    val status: String
)

data class SystemConfigResponse(
    val facial_recognition: FacialRecognitionConfig,
    val thresholds: ThresholdConfig,
    val comparison_weights: Map<String, Double>?,
    val system: SystemGeneralConfig,
    val directories: DirectoriesConfig
)

data class FacialRecognitionConfig(
    val enhanced_processing: Boolean,
    val feature_method: String,
    val default_threshold: Double,
    val adaptive_threshold: Boolean,
    val multiple_detectors: Boolean,
    val use_dlib: Boolean
)

data class ThresholdConfig(
    val default: Double,
    val min: Double,
    val max: Double
)

data class SystemGeneralConfig(
    val debug: Boolean,
    val log_level: String
)

data class DirectoriesConfig(
    val upload: String,
    val models: String,
    val backup: String,
    val json_backup: String
)

data class PerformanceMetricsResponse(
    val database_performance: DatabasePerformance,
    val system_resources: SystemResources,
    val configuration_status: ConfigurationStatus
)

data class DatabasePerformance(
    val total_persons: Int,
    val total_features: Int
)

data class SystemResources(
    val dependencies_ok: Boolean,
    val disk_usage: DiskUsage?,
    val file_counts: Map<String, DirectoryInfo>
)

data class ConfigurationStatus(
    val enhanced_processing: Boolean,
    val adaptive_thresholds: Boolean,
    val multiple_detectors: Boolean
)

// Respuestas de gestión de datos
data class ImportResponse(
    val message: String,
    val imported: Int,
    val errors: Int,
    val error_details: List<String>
)

data class BackupResponse(
    val message: String,
    val backup_info: ExportResponse
)

data class SyncCheckResponse(
    val synchronization: Map<String, Any>
)

// Respuestas de personas extendidas
data class PersonUpdateFeaturesResponse(
    val message: String,
    val person_id: Int,
    val method: String,
    val features_count: Int,
    val faces_detected: Int
)

data class ProcessingStatsResponse(
    val database_stats: PersonStats,
    val current_config: CurrentConfig,
    val system_capabilities: SystemCapabilities
)

data class PersonStats(
    val total_personas: Int?,
    val por_metodo: Map<String, Int>?,
    val por_version: Map<String, Int>?
)

data class CurrentConfig(
    val enhanced_processing: Boolean,
    val feature_method: String,
    val default_threshold: Double,
    val adaptive_threshold: Boolean,
    val multiple_detectors: Boolean
)

data class SystemCapabilities(
    val dlib_available: Boolean,
    val sklearn_available: Boolean,
    val migration_enabled: Boolean
)

// Respuestas de información del sistema
data class RootInfoResponse(
    val message: String,
    val version: String,
    val status: String,
    val features: Map<String, Any>,
    val endpoints: Map<String, String>
)

data class SystemInfoResponse(
    val system: SystemInfoSystem,
    val configuration: Map<String, Any>,
    val database: Map<String, Any>,
    val dependencies: Map<String, String>,
    val capabilities: Map<String, Any>
)

data class SystemInfoSystem(
    val name: String,
    val version: String,
    val mode: String,
    val environment: String
)

// Clases de datos existentes mantenidas por compatibilidad
data class PersonRegistrationResponse(
    val message: String,
    val person_id: Int,
    val pk: String,
    val features_count: Int,
    val faces_detected: Int,
    val processing_method: String,
    val processing_time: Double,
    val system_info: SystemInfo
)

data class PersonListResponse(
    val total: Int,
    val persons: List<Person>,
    val system_stats: SystemStats?,
    val pagination: Pagination?
)

data class PersonResponse(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val id_estudiante: String?,
    val pk: String,
    val fecha_registro: String,
    val activo: Int,
    val system_info: SystemInfo?
) {
    // Función helper para obtener el valor como boolean
    val isActive: Boolean
        get() = activo == 1
}

data class Person(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val id_estudiante: String?,
    val pk: String,
    val fecha_registro: String,
    val activo: Int = 1
) {
    // Función helper para obtener el valor como boolean
    val isActive: Boolean
        get() = activo == 1
}

data class RecognitionResponse(
    val person: PersonResponse,
    val recognition_result: RecognitionResult,
    val system_info: SystemInfo?
)

data class RecognitionResult(
    val similarity: Double,
    val cosine_similarity: Double?,
    val correlation: Double?,
    val euclidean_similarity: Double?,
    val manhattan_similarity: Double?,
    val is_match: Boolean,
    val threshold: Double,
    val confidence: Double?,
    val faces_detected: Int,
    val features_compared: Int,
    val processing_method: String?
)

data class IdentificationResponse(
    val identification_result: IdentificationResult,
    val all_matches: List<IdentificationMatch>,
    val system_info: SystemInfo?
)

data class IdentificationResult(
    val best_match: IdentificationMatch?,
    val confidence: Double,
    val total_comparisons: Int,
    val faces_detected: Int,
    val processing_method: String?,
    val feature_extraction_method: String?
)

data class IdentificationMatch(
    val person: MatchedPerson,
    val similarity: Double,
    val is_match: Boolean,
    val threshold: Double,
    val confidence: Double?,
    val detailed_metrics: DetailedMetrics?
)

data class MatchedPerson(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val id_estudiante: String?,
    val metodo_caracteristicas: String?,
    val version_algoritmo: String?
)

data class DetailedMetrics(
    val cosine_similarity: Double,
    val correlation: Double,
    val euclidean_similarity: Double,
    val manhattan_similarity: Double,
    val consistency: Double,
    val adjusted_threshold: Double
)

data class SystemInfo(
    val enhanced_processing: Boolean,
    val feature_method: String?,
    val threshold: Double?,
    val comparison_method: String?
)

data class SystemStats(
    val total_personas: Int?,
    val por_metodo: Map<String, Int>?,
    val por_version: Map<String, Int>?
)

data class Pagination(
    val page: Int,
    val per_page: Int,
    val total_pages: Int
)

data class RecognitionStatsResponse(
    val system_stats: SystemStats,
    val configuration: Configuration,
    val feature_config: FeatureConfig,
    val detection_config: DetectionConfig
)

data class Configuration(
    val enhanced_processing: Boolean = false,
    val feature_method: String? = null,
    val default_threshold: Double = 0.0,
    val adaptive_threshold: Boolean = false,
    val multiple_detectors: Boolean = false,
    val use_multiple_detectors: Boolean = false,
    val use_dlib: Boolean = false,
    val comparison_weights: Map<String, Double>? = null
)

fun Configuration.toSafeConfiguration(): SafeConfiguration {
    return SafeConfiguration(
        enhanced_processing = this.enhanced_processing,
        feature_method = this.feature_method ?: "traditional",
        default_threshold = this.default_threshold,
        adaptive_threshold = this.adaptive_threshold,
        use_multiple_detectors = this.use_multiple_detectors,
        use_dlib = this.use_dlib,
        comparison_weights = this.comparison_weights
    )
}

data class SafeConfiguration(
    val enhanced_processing: Boolean,
    val feature_method: String,
    val default_threshold: Double,
    val adaptive_threshold: Boolean,
    val use_multiple_detectors: Boolean,
    val use_dlib: Boolean,
    val comparison_weights: Map<String, Double>?
)

data class FeatureConfig(
    val method: String,
    val normalize: Boolean,
    val target_size: Int
)

data class DetectionConfig(
    val use_multiple_detectors: Boolean,
    val use_dlib: Boolean
)

data class SystemStatsResponse(
    val system_info: SystemInfoDetailed,
    val database_statistics: DatabaseStatistics,
    val file_system: FileSystemStats,
    val configuration: Configuration
)

data class SystemInfoDetailed(
    val version: String,
    val status: String,
    val database: String,
    val enhanced_processing: Boolean,
    val feature_method: String,
    val default_threshold: Double
)

data class DatabaseStatistics(
    val total_persons: Int,
    val total_models: Int,
    val mysql_version: String,
    val first_register: String?,
    val last_register: String?,
    val methods: Map<String, MethodStats>
)

data class MethodStats(
    val cantidad: Int,
    val umbral_promedio: Double
)

data class FileSystemStats(
    val directories: Map<String, DirectoryInfo>,
    val total_files: Int,
    val total_size: Int,
    val total_size_formatted: String,
    val disk_usage: DiskUsage?
)

data class DirectoryInfo(
    val files: Int,
    val size: Int,
    val size_formatted: String
)

data class DiskUsage(
    val total: String,
    val used: String,
    val free: String,
    val usage_percent: Double
)

data class HealthCheckResponse(
    val status: String,
    val components: Components,
    val system_config: SystemConfig,
    val uptime: String,
    val last_check: String
)

data class Components(
    val database: DatabaseComponent,
    val facial_recognition: String,
    val file_system: String,
    val dependencies: Map<String, Boolean>
)

data class DatabaseComponent(
    val status: String,
    val connection: Boolean
)

data class SystemConfig(
    val enhanced_processing: Boolean,
    val feature_method: String,
    val adaptive_threshold: Boolean
)

data class ExportResponse(
    val message: String,
    val filename: String,
    val total_records: Int,
    val download_url: String
)

data class GeneralHealthResponse(
    val status: String,
    val database: String,
    val facial_processing: String,
    val dependencies: Map<String, String>,
    val configuration: Map<String, Any>,
    val timestamp: String
)