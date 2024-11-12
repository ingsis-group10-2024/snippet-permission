package ingsis.permission.permission.service.implementation

import ingsis.permission.permission.exception.InvalidPermissionType
import ingsis.permission.permission.model.dto.CreatePermission
import ingsis.permission.permission.model.dto.PaginatedSnippetResponse
import ingsis.permission.permission.model.dto.SnippetDescriptor
import ingsis.permission.permission.model.enums.PermissionTypeEnum
import ingsis.permission.permission.persistance.entity.Permission
import ingsis.permission.permission.persistance.repository.PermissionRepository
import ingsis.permission.permission.service.PermissionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class PermissionService
    @Autowired
    constructor(
        private val repository: PermissionRepository,
        private val restTemplate: RestTemplate,
    ) : PermissionService {
        fun createPermission(input: CreatePermission): Permission {
            val type = getPermissionType(input.permissionType)
            println("Permission type: $type")
            val existingPermission = findPermission(input.userId, input.snippetId)

            return if (existingPermission != null) {
                updatePermission(existingPermission, type)
            } else {
                val permission =
                    Permission(
                        userId = input.userId,
                        snippetId = input.snippetId,
                        permissions = listOf(type), // Lista de permisos
                    )
                println("Saving permission: $permission")
                repository.save(permission)
            }
        }

        fun getPermissions(
            userId: String,
            snippetId: String,
        ): List<PermissionTypeEnum> {
            val permission = findPermission(userId, snippetId)
            println("Retrieved permission for $userId and $snippetId: $permission")
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
            authorizationHeader: String,
            targetUserId: String,
        ): SnippetDescriptor {
            val snippet = getSnippet(snippetId, authorizationHeader) ?: throw Exception("Snippet not found")
            println("Snippet: ${snippet.id}")

            // Check if user has the OWNER permission
            val userPermissions = getPermissions(userId, snippetId)
            println("$userId has permissions: $userPermissions")
            if (!userPermissions.contains(PermissionTypeEnum.OWNER)) {
                throw InvalidPermissionType("User does not have ownership of the snippet.")
            }

            // Check if the target user (the friend) already has permissions
            val existingPermission = findPermission(targetUserId, snippetId)
            if (existingPermission == null) {
                // If the target user does not have permissions, create READ permissions for the target user
                createPermission(
                    CreatePermission(
                        snippetId = snippetId,
                        userId = targetUserId,
                        permissionType = PermissionTypeEnum.READ.name,
                    ),
                )
            }
            return snippet
        }

        fun getSnippet(
            snippetId: String,
            authorizationHeader: String,
        ): SnippetDescriptor? {
            val url = "http://snippet-manager:8080/manager/snippet/get/$snippetId"

            val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
            headers.add("Authorization", authorizationHeader)
            headers.add("Content-Type", "application/json")

            val requestEntity = HttpEntity(null, headers)

            val response: ResponseEntity<SnippetDescriptor> =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    SnippetDescriptor::class.java,
                )
            return response.body
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
            println("Searching for permission with userId: $userId and snippetId: $snippetId")
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

        fun listUserSnippets(
            userId: String,
            page: Int,
            pageSize: Int,
            authorizationHeader: String,
        ): PaginatedSnippetResponse {
            val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
            headers.add("Authorization", authorizationHeader)
            headers.add("Content-Type", "application/json")

            val requestEntity = HttpEntity(null, headers)

            val url = "http://snippet-manager:8080/manager/snippet/snippets?userId={userId}&page={page}&pageSize={pageSize}"
            val params =
                mapOf(
                    "userId" to userId,
                    "page" to page.toString(),
                    "pageSize" to pageSize.toString(),
                )

            val response: ResponseEntity<PaginatedSnippetResponse> =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    PaginatedSnippetResponse::class.java,
                    params,
                )
            return response.body ?: throw RuntimeException("No response from snippet-manager")
        }

//        fun changeRule(configRule: ConfigRule, streamKey: String) {
//            val message = mapOf(
//                "name" to configRule.name,
//                "enabled" to configRule.enabled.toString(),
//                "value" to (configRule.value?.toString() ?: "null")
//            )
//
//            // Send the message to the stream
//            redisTemplate.opsForStream<String, String>().add(streamKey, message)
//        }
    }
