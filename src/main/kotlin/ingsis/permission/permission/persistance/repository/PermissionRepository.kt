package ingsis.permission.permission.persistance.repository

import ingsis.permission.permission.persistance.entity.Permission
import org.springframework.data.jpa.repository.JpaRepository

interface PermissionRepository : JpaRepository<Permission, String> {

}