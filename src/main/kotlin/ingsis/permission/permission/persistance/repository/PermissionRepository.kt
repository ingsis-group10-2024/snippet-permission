package ingsis.permission.permission.persistance.repository

import ingsis.permission.permission.persistance.entity.Permission
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PermissionRepository : JpaRepository<Permission, String> {
    fun findByUserIdAndSnippetId(
        userId: String,
        snippetId: String,
    ): Permission?

    fun findByUserId(
        userId: String,
        pageable: Pageable,
    ): Page<Permission>
}
