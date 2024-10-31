package ingsis.permission.permission.service.implementation

import ingsis.permission.permission.exception.InvalidPermissionType
import ingsis.permission.permission.model.dto.CreatePermission
import ingsis.permission.permission.model.enums.PermissionTypeEnum
import ingsis.permission.permission.persistance.entity.Permission
import ingsis.permission.permission.persistance.repository.PermissionRepository
import ingsis.permission.permission.service.PermissionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.client.RestTemplate


@Service
class PermissionService
    @Autowired
    constructor(
        private val repository: PermissionRepository,
        private val restTemplate: RestTemplate,
    ) : PermissionService {

        // URL of the services
        private val snippetManagerUrl = "http://snippet-manager:8080/snippets/user"
        private val snippetRunnerUrl = "http://snippet-runner:8080/linting/status"

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

        fun getPermissions(
            userId: String,
            snippetId: String,
        ): List<PermissionTypeEnum> {
            val permission = findPermission(userId, snippetId)
            return permission?.permissions ?: emptyList()
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


        // Extract the JWT token from the authentication object
        private fun getJwtToken(): String {
            val authentication = SecurityContextHolder.getContext().authentication
            val jwt = authentication.principal as Jwt
            println("JWT Token: ${jwt.tokenValue}") // DEBUG
            return jwt.tokenValue
        }


    fun listUserSnippets(userId: String, page: Int, pageSize: Int): PaginatedSnippetResponse {
        // Create headers with the JWT token
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(getJwtToken())
        }

        // Configure the request
        val params = mapOf(
            "userId" to userId,
            "page" to page.toString(),
            "pageSize" to pageSize.toString()
        )

        // Call the snippets service
        val entity = HttpEntity(null, headers)
        val paginatedSnippets = restTemplate.exchange(
            "$snippetManagerUrl?userId={userId}&page={page}&pageSize={pageSize}",
            HttpMethod.GET,
            entity,
            PaginatedSnippetResponse::class.java,
            params
        ).body!!
    }
}
