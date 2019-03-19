package no.skatteetaten.aurora.mockmvc.extensions.mockwebserver

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.support.expected
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity

data class TestObject(val value: String)

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class MockWebServerTest {

    private val server = MockWebServer()
    private val url = server.url("/")

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `Test execute with MockResponse`() {
        val request = server.execute(MockResponse().setBody("test")) {
            val response = RestTemplate().getForEntity<String>(url.toString())
            assertThat(response).isOk()
            assertThat(response.body).isEqualTo("test")
        }

        assertThat(request).hasDefaultPath()
    }

    @Test
    fun `Test execute with status and response object`() {
        val request = server.execute(201, TestObject("test")) {
            val response = RestTemplate().postForEntity<String>(url.toString(), TestObject("test-request-body"))
            assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
            assertThat(response.body).isEqualTo("""{"value":"test"}""")
        }

        assertThat(request).hasDefaultPath()
        assertThat(request.bodyAsString()).isEqualTo("""{"value":"test-request-body"}""")
    }

    @Test
    fun `Test execute with request and response object`() {
        val request = server.execute(TestObject("test")) {
            val response = RestTemplate().postForEntity<String>(url.toString(), TestObject("test-request-body"))
            assertThat(response).isOk()
            assertThat(response.body).isEqualTo("""{"value":"test"}""")
        }
        val body = request.bodyAsObject<TestObject>()

        assertThat(request).hasDefaultPath()
        assertThat(body.value).isEqualTo("test-request-body")
    }

    @Test
    fun `Test execute with vararg response objects`() {
        val requests = server.execute(TestObject("test"), TestObject("test")) {
            val response1 = RestTemplate().getForEntity<String>(url.toString())
            val response2 = RestTemplate().getForEntity<String>(url.toString())
            assertThat(response1).isOk()
            assertThat(response2).isOk()
            assertThat(response1.body).isEqualTo("""{"value":"test"}""")
            assertThat(response2.body).isEqualTo("""{"value":"test"}""")
        }

        assertThat(requests.size).isEqualTo(2)
        assertThat(requests[0]).hasDefaultPath()
        assertThat(requests[1]).hasDefaultPath()
    }

    @Test
    fun `Set json file as body`() {
        val request = server.execute(MockResponse().setJsonFileAsBody("test.json")) {
            val response = RestTemplate().getForEntity<String>(url.toString())
            assertThat(response).isOk()
            assertThat(response.body).isEqualTo("""{"key":"test123"}""")
        }

        assertThat(request).hasDefaultPath()
    }

    private fun Assert<ResponseEntity<*>>.isOk() = given { request ->
        if (request.statusCode == HttpStatus.OK) return
        expected("Status 200 - OK")
    }

    private fun Assert<RecordedRequest>.hasDefaultPath() = given { request ->
        if (request.path == "/") return
        expected("Request to contain '/' path")
    }
}