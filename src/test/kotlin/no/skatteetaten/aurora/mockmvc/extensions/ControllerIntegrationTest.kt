package no.skatteetaten.aurora.mockmvc.extensions

import assertk.assertThat
import assertk.assertions.isEqualTo
import no.skatteetaten.aurora.mockmvc.extensions.testutils.TestController
import no.skatteetaten.aurora.mockmvc.extensions.testutils.TestObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import java.io.File

@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
@SpringBootTest(classes = [TestController::class])
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class ControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `Get request with json response`() {
        mockMvc.get(urlTemplate = UrlTemplate("/test")) {
            it.statusIsOk().responseJsonPath("$.value").equalsValue("test")
        }
    }

    @Test
    fun `Get request with header`() {
        mockMvc.get(
            headers = HttpHeaders().authorization("test"),
            urlTemplate = UrlTemplate("/test-with-header")
        ) {
            it.statusIsOk().responseJsonPath("$.header").equalsValue("test")
        }
    }

    @Test
    fun `Get request with object response`() {
        mockMvc.get(urlTemplate = UrlTemplate("/test-with-object")) {
            it.statusIsOk().responseJsonPath("$").equalsObject(TestObject())
        }
    }

    @Test
    fun `Get request with rest docs`() {
        val restDocsIdentifier = "get-with-restdocs"
        mockMvc.get(
            docsIdentifier = restDocsIdentifier,
            urlTemplate = UrlTemplate("/test")
        ) {
            it.statusIsOk().responseJsonPath("$.value").equalsValue("test")
        }

        val stubFileName = File("target/generated-snippets/stubs").listFiles().first().name
        assertThat("$restDocsIdentifier.json").isEqualTo(stubFileName)
    }
}