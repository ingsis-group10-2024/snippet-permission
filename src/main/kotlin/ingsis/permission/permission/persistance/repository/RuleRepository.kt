package ingsis.permission.permission.persistance.repository

import ingsis.permission.permission.model.enums.RuleTypeEnum
import ingsis.permission.permission.persistance.entity.Rule
import org.springframework.data.jpa.repository.JpaRepository

interface RuleRepository : JpaRepository<Rule, String> {
    fun findByUserIdAndType(
        userId: String,
        type: RuleTypeEnum,
    ): List<Rule>

    fun findByUserIdAndNameAndType(
        userId: String,
        name: String,
        type: RuleTypeEnum,
    ): Rule?
}
