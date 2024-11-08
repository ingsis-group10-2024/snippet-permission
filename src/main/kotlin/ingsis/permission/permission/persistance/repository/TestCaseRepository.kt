package ingsis.permission.permission.persistance.repository

import ingsis.permission.permission.persistance.entity.TestCaseEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TestCaseRepository : JpaRepository<TestCaseEntity, String> {
    override fun findById(id: String): Optional<TestCaseEntity>
}
