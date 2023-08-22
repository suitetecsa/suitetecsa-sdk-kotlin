package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.framework.model.HttpResponse
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import io.mockk.*
import org.jsoup.Connection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NautaSessionImplTest {

    private lateinit var mockConnectionFactory: ConnectionFactory
    private lateinit var nautaSession: NautaSession

    @BeforeEach
    fun setup() {
        mockConnectionFactory = mockk(relaxed = true)
        nautaSession = NautaSessionImpl(mockConnectionFactory)
    }

    @Test
    fun `test successful GET request`() {
        // Arrange
        val mockConnection = mockk<Connection>()
        val mockResponse = mockk<Connection.Response>()
        val expectedResponse = HttpResponse(200, "OK", "Response content".toByteArray(), emptyMap())

        every { mockConnection.ignoreContentType(any()) } returns mockConnection
        every { mockConnection.timeout(any()) } returns mockConnection
        every { mockConnection.method(any()) } returns mockConnection
        every { mockConnection.execute() } returns mockResponse
        every { mockResponse.statusCode() } returns 200
        every { mockResponse.statusMessage() } returns "OK"
        every { mockResponse.bodyAsBytes() } returns "Response content".toByteArray()
        every { mockResponse.cookies() } returns emptyMap()
        every { mockConnectionFactory.createConnection(any(), any()) } returns mockConnection

        // Act
        val result = nautaSession.get("http://example.com")

        // Assert
        result as ResultType.Success
        assertEquals(expectedResponse, result.result)
    }

    @Test
    fun `test GET request with exception`() {
        // Arrange
        val mockConnection = mockk<Connection>()

        every { mockConnection.ignoreContentType(any()) } returns mockConnection
        every { mockConnection.timeout(any()) } returns mockConnection
        every { mockConnection.method(any()) } returns mockConnection
        every { mockConnection.execute() } throws Exception("Connection error")
        every { mockConnectionFactory.createConnection(any(), any()) } returns mockConnection

        // Act
        val result = nautaSession.get("http://example.com")

        // Assert
        result as ResultType.Error
        assertEquals("Connection error", result.throwable.message)
    }

    @Test
    fun `test successful POST request`() {
        // Arrange
        val mockConnection = mockk<Connection>()
        val mockResponse = mockk<Connection.Response>()
        val expectedResponse = HttpResponse(201, "Created", "Response content".toByteArray(), emptyMap())

        every { mockConnection.ignoreContentType(any()) } returns mockConnection
        every { mockConnection.timeout(any()) } returns mockConnection
        every { mockConnection.method(any()) } returns mockConnection
        every { mockConnection.cookies(any()) } returns mockConnection
        every { mockConnection.execute() } returns mockResponse
        every { mockResponse.statusCode() } returns 201
        every { mockResponse.statusMessage() } returns "Created"
        every { mockResponse.cookies() } returns emptyMap()
        every { mockResponse.bodyAsBytes() } returns "Response content".toByteArray()
        every { mockConnectionFactory.createConnection(any(), any()) } returns mockConnection

        // Act
        val result = nautaSession.post("http://example.com")

        // Assert
        result as ResultType.Success
        assertEquals(expectedResponse, result.result)
    }

    @Test
    fun `test POST request with exception`() {
        // Arrange
        val mockConnection = mockk<Connection>()

        every { mockConnection.ignoreContentType(any()) } returns mockConnection
        every { mockConnection.timeout(any()) } returns mockConnection
        every { mockConnection.method(any()) } returns mockConnection
        every { mockConnection.execute() } throws Exception("Connection error")
        every { mockConnectionFactory.createConnection(any(), any()) } returns mockConnection

        // Act
        val result = nautaSession.post("http://example.com")

        // Assert
        result as ResultType.Error
        assertEquals("Connection error", result.throwable.message)
    }
}

