package ingsis.permission.permission.service.implementation

import ingsis.permission.permission.exception.InvalidPermissionType
import ingsis.permission.permission.model.dto.CreatePermission
import ingsis.permission.permission.model.enums.PermissionTypeEnum
import ingsis.permission.permission.persistance.entity.Permission
import ingsis.permission.permission.persistance.repository.PermissionRepository
import ingsis.permission.permission.service.PermissionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PermissionService
    @Autowired
    constructor(
        private val repository: PermissionRepository,
    ) : PermissionService {
        fun createPermission(input: CreatePermission): Permission {
            val type = getPermissionType(input.permissionType)
            val existingPermission = findPermission(input.userId, input.snippetId)

            return if (existingPermission != null) {
                updatePermission(existingPermission, type)
            } else {
                repository.save(
                    Permission(
                        userId = input.userId,
                        snippetId = input.snippetId,
                        permissions = listOf(type),
                    ),
                )
            }
        }

        override fun getUserPermission(
            snippetId: String,
            userId: String,
        ): Permission {
            TODO("Not yet implemented")
        }

        override fun shareSnippet(
            snippetId: String,
            userId: String,
            targetUserId: String,
        ): Boolean {
            TODO("Not yet implemented")
        }

        override fun getOwnerBySnippetId(snippetId: String): String {
            TODO("Not yet implemented")
        }

        override fun addPermission(
            snippetId: String,
            userId: String,
            permissionTypeEnum: PermissionTypeEnum,
        ): Boolean {
            TODO("Not yet implemented")
        }

        private fun getPermissionType(permissionType: String): PermissionTypeEnum {
            return try {
                PermissionTypeEnum.valueOf(permissionType)
            } catch (e: IllegalArgumentException) {
                throw InvalidPermissionType(permissionType)
            }
        }

        private fun findPermission(
            userId: String,
            snippetId: String,
        ): Permission? {
            return repository.findByUserIdAndSnippetId(userId, snippetId)
        }

        private fun updatePermission(
            existingPermission: Permission,
            newType: PermissionTypeEnum,
        ): Permission {
            if (!existingPermission.permissions.contains(newType)) {
                val updatedPermissions = existingPermission.permissions + newType
                // copy is used to create a new instance of the object
                val updatedPermission = existingPermission.copy(permissions = updatedPermissions)
                return repository.save(updatedPermission)
            }
            return repository.save(existingPermission)
        }
    }
