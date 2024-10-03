package ingsis.permission.permission.model.dto

data class CreatePermission(
    val snippetId: String,
    val userId: String,
    val permissionType: String,
)
