package ingsis.permission.permission.service

import ingsis.permission.permission.model.enums.PermissionTypeEnum
import ingsis.permission.permission.persistance.entity.Permission

interface PermissionService {
    fun getUserPermission(
        snippetId: String,
        userId: String,
    ): Permission

    fun shareSnippet(
        snippetId: String,
        userId: String,
        targetUserId: String,
    ): Boolean

    fun getOwnerBySnippetId(snippetId: String): String

    fun addPermission(
        snippetId: String,
        userId: String,
        permissionTypeEnum: PermissionTypeEnum,
    ): Boolean
}
