package ingsis.permission.permission.persistance.entity

import ingsis.permission.permission.model.enums.PermissionTypeEnum
import jakarta.persistence.CollectionTable
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
data class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = UUID.randomUUID().toString(),
    val authorId: String = "1",
    val snippetId: String = "1",
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @ElementCollection(targetClass = PermissionTypeEnum::class)
    @CollectionTable(name = "permission_types", joinColumns = [JoinColumn(name = "permission_id")])
    @Enumerated(EnumType.STRING)
    val permissions: List<PermissionTypeEnum>,
)
