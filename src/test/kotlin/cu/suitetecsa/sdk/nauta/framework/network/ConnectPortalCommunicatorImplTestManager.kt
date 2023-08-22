package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.core.Action
import cu.suitetecsa.sdk.nauta.framework.model.HttpResponse
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConnectPortalCommunicatorImplTest {

    private lateinit var mockNautaSession: NautaSession
    private lateinit var connectPortalCommunicator: ConnectPortalCommunicatorImpl

    @BeforeEach
    fun setup() {
        mockNautaSession = mockk(relaxed = true)
        connectPortalCommunicator = ConnectPortalCommunicatorImpl(mockNautaSession)
    }

    @Test
    fun `test performAction for successful GET request`() {
        // Arrange
        val mockResponse = HttpResponse(200, "OK", "Response content".toByteArray(), emptyMap())
        every { mockNautaSession.get(any(), any()) } returns ResultType.Success(mockResponse)

        // Act
        val result = connectPortalCommunicator.performAction(Action.CheckConnection()) { it.text }

        // Assert
        result as ResultType.Success
        assertEquals("Response content", result.result)
    }

    @Test
    fun `test performAction for GET request with error`() {
        // Arrange
        val mockException = Exception("Connection error")
        every { mockNautaSession.get(any(), any()) } returns ResultType.Failure(mockException)

        // Act
        val result = connectPortalCommunicator.performAction(Action.CheckConnection()) { it.text }

        // Assert
        result as ResultType.Failure
        assertEquals(mockException, result.throwable)
    }

    @Test
    fun `test performAction for successful POST request`() {
        // Arrange
        val mockResponse = HttpResponse(201, "Created", null, emptyMap())
        every { mockNautaSession.post(any(), any()) } returns ResultType.Success(mockResponse)

        // Act
        val result = connectPortalCommunicator.performAction(
            Action.GetPage(
                "http://example.com",
                emptyMap()
            )
        ) { it.statusMessage }

        // Assert
        result as ResultType.Success
        assertEquals("Created", result.result)
    }

    @Test
    fun `test performAction for POST request with error`() {
        // Arrange
        val mockException = Exception("Connection error")
        every { mockNautaSession.post(any(), any()) } returns ResultType.Failure(mockException)

        // Act
        val result = connectPortalCommunicator.performAction(
            Action.GetPage(
                "http://example.com",
                emptyMap()
            )
        ) { it.statusMessage }

        // Assert
        result as ResultType.Failure
        assertEquals(mockException, result.throwable)
    }

    @Test
    fun `test performAction for successful GET request with transformation`() {
        // Arrange
        val mockResponse = HttpResponse(200, "OK", "42".toByteArray(), emptyMap())
        every { mockNautaSession.get(any(), any()) } returns ResultType.Success(mockResponse)

        // Act
        val result = connectPortalCommunicator.performAction(Action.CheckConnection()) { it.text?.toInt() }

        // Assert
        result as ResultType.Success
        assertEquals(42, result.result)
    }
}
