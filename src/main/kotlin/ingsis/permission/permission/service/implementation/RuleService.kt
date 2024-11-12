package ingsis.permission.permission.service.implementation

import ingsis.permission.permission.exception.RuleNotFoundException
import ingsis.permission.permission.exception.UnauthorizedAccessException
import ingsis.permission.permission.model.dto.RuleDto
import ingsis.permission.permission.model.enums.RuleTypeEnum
import ingsis.permission.permission.persistance.entity.Rule
import ingsis.permission.permission.persistance.repository.RuleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RuleService
    @Autowired
    constructor(
        private val ruleRepository: RuleRepository,
    ) {
        fun createOrUpdateRules(
            newRules: List<RuleDto>,
            url: String,
            token: String,
            ruleType: RuleTypeEnum,
            userId: String,
        ): List<RuleDto> {
            val rulesToSave =
                newRules.map { dto ->
                    val existingRule =
                        if (dto.id != null) {
                            ruleRepository.findByUserIdAndNameAndType(userId = userId, name = dto.name, type = ruleType)
                        } else {
                            null
                        }
                    if (existingRule != null) {
                        // Update existing rule
                        existingRule.apply {
                            isActive = dto.isActive
                            value = dto.value
                        }
                        existingRule
                    } else {
                        // If rule is not found, create a new one
                        Rule(
                            userId = userId,
                            name = dto.name,
                            isActive = dto.isActive,
                            value = dto.value,
                            type = ruleType,
                        )
                    }
                }
            return ruleRepository.saveAll(rulesToSave).map { RuleDto(it) }
        }

        fun deleteRule(
            userId: String,
            ruleId: String,
        ) {
            val rule =
                ruleRepository.findById(ruleId).orElse(null)
                    ?: throw RuleNotFoundException("Rule not found with id: $ruleId")

            if (rule.userId != userId) {
                throw UnauthorizedAccessException("User does not have permission to delete this rule")
            }
            ruleRepository.delete(rule)
        }

        fun getFormatRules(userId: String): List<RuleDto> {
            val formatRules = ruleRepository.findByUserIdAndType(userId, RuleTypeEnum.FORMAT)
            return formatRules.map { RuleDto(it) }
        }

        fun getLintingRules(userId: String): List<RuleDto> {
            val lintingRules = ruleRepository.findByUserIdAndType(userId, RuleTypeEnum.LINT)
            return lintingRules.map { RuleDto(it) }
        }
    }
