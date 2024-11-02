package ingsis.permission.permission.model.dto

data class PaginatedSnippetResponse(
    val snippets: List<SnippetDescriptor>,
    val totalPages: Int,
    val totalElements: Long
)