package ingsis.permission.permission.utils

import ingsis.permission.permission.model.dto.TestCaseDTO
import ingsis.permission.permission.persistance.entity.TestCaseEntity

fun TestCaseEntity.toDTO() =
    TestCaseDTO(
        id = this.id,
        name = this.name,
        input = this.input,
        output = this.output,
    )
