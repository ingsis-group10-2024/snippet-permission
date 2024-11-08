package ingsis.permission.permission.model.dto

data class TestCaseDTO(
    val id: String? = null,
    val name: String,
    val input: List<String>,
    val output: List<String>,
)
