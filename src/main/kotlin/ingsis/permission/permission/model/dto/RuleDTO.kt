package ingsis.permission.permission.model.dto

data class RuleDTO(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val value: Any?
)
