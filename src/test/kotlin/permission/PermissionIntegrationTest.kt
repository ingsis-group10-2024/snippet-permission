package ingsis.permission.permission.integration

import com.fasterxml.jackson.databind.ObjectMapper
import config.TestSecurityConfig
import ingsis.permission.permission.model.dto.CreatePermission
import ingsis.permission.permission.model.dto.PermissionRequest
import ingsis.permission.permission.model.enums.PermissionTypeEnum
import ingsis.permission.permission.persistance.entity.Permission
import ingsis.permission.permission.persistance.repository.PermissionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig::class)
@Transactional
class PermissionIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var permissionRepository: PermissionRepository

    @BeforeEach
    fun setup() {
        permissionRepository.deleteAll()
    }

    @Test
    fun `test createPermission endpoint`() {
        val createPermission = CreatePermission(
            snippetId = "snippet123",
            userId = "user123",
            permissionType = "READ"
        )
        val requestBody = objectMapper.writeValueAsString(createPermission) // lo convierto en JSON

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn()

        val responseBody = result.response.contentAsString
        val createdPermission = objectMapper.readValue(responseBody, Permission::class.java)

        assertNotNull(createdPermission)
        assertEquals("user123", createdPermission.userId)
        assertEquals("snippet123", createdPermission.snippetId)
        assertEquals(listOf(PermissionTypeEnum.READ), createdPermission.permissions)
    }
}
