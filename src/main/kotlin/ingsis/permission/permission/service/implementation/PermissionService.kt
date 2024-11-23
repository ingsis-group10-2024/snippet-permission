package ingsis.permission.permission.service.implementation

import ingsis.permission.permission.exception.InvalidPermissionType
import ingsis.permission.permission.model.dto.CreatePermission
import ingsis.permission.permission.model.dto.PaginatedUsers
import ingsis.permission.permission.model.dto.SnippetDescriptor
import ingsis.permission.permission.model.enums.PermissionTypeEnum
import ingsis.permission.permission.persistance.entity.Permission
import ingsis.permission.permission.persistance.repository.PermissionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
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
    ) {
        private val logger: Logger = LoggerFactory.getLogger(PermissionService::class.java)

        fun createPermission(input: CreatePermission): Permission {
            logger.info("Creating permission for user ${input.userId} and snippet ${input.snippetId}...")

            val type = getPermissionType(input.permissionType)

            logger.info("Permission type: $type")

            val existingPermission = findPermission(input.userId, input.snippetId)

            return if (existingPermission != null) {
                logger.info("Permission exists. Updating permission...")
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
            logger.info("Retrieved permissions: $permission for user $userId and snippet $snippetId")
            return permission?.permissions ?: emptyList()
        }

        fun shareSnippet(
            snippetId: String,
            userId: String,
            authorizationHeader: String,
            targetUserId: String,
        ): SnippetDescriptor {
            logger.info("Sharing snippet $snippetId with user $targetUserId...")
            val snippet = getSnippet(snippetId, authorizationHeader) ?: throw Exception("Snippet not found")
            logger.info("Snippet found: $snippet")

            // Check if user has the OWNER permission
            val userPermissions = getPermissions(userId, snippetId)

            if (!userPermissions.contains(PermissionTypeEnum.OWNER)) {
                logger.error("User does not have ownership of the snippet.")
                throw InvalidPermissionType("User does not have ownership of the snippet.")
            }

            logger.info("Checking if the target user already has permissions")
            // Check if the target user (the friend) already has permissions
            val existingPermission = findPermission(targetUserId, snippetId)
            if (existingPermission == null) {
                logger.info("Target user does not have permissions. Creating READ permissions to share snippet with Id: $snippetId")
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
            logger.info("Getting snippet with id $snippetId")
            logger.info("Calling snippet manager")
            val url = "http://snippet-manager:8080/manager/snippet/get?snippetId=$snippetId"

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
            logger.info("Finding permission for user $userId and snippet $snippetId")
            return repository.findByUserIdAndSnippetId(userId, snippetId)
        }

        private fun updatePermission(
            existingPermission: Permission,
            newType: PermissionTypeEnum,
        ): Permission {
            logger.info("Updating permission: $existingPermission...")
            if (!existingPermission.permissions.contains(newType)) {
                val updatedPermissions = existingPermission.permissions + newType
                // copy is used to create a new instance of the object
                val updatedPermission = existingPermission.copy(permissions = updatedPermissions)
                logger.info("Saving permission to database: $updatedPermission")
                return repository.save(updatedPermission)
            }
            return repository.save(existingPermission)
        }

        fun getUserFriends(
            name: String,
            page: Int,
            pageSize: Int,
        ): PaginatedUsers {
            logger.info("Getting friends of user $name")
            val pageable = PageRequest.of(page, pageSize)

            val permissionsPage: Page<Permission> = repository.findByUserId(name, pageable)

            val usersWithReadPermission =
                permissionsPage.content
                    .filter { permission ->
                        permission.permissions.contains(PermissionTypeEnum.READ) ||
                            permission.permissions.contains(PermissionTypeEnum.OWNER)
                    }
                    .map { it.userId }

            logger.info("Users with read permission: $usersWithReadPermission")
            return PaginatedUsers(
                users = usersWithReadPermission,
                total = permissionsPage.totalElements.toInt(),
            )
        }
    }
