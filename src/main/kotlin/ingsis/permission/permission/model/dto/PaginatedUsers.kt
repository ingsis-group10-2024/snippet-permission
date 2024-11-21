package ingsis.permission.permission.model.dto

data class PaginatedUsers(
    val users: List<String>,
    val total: Int, // All permissions found
)
