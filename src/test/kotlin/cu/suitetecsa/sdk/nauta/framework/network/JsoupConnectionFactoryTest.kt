package cu.suitetecsa.sdk.nauta.framework.network

import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class JsoupConnectionFactoryTest {

    @MockK
    lateinit var mockConnection: Connection

    private lateinit var jsoupConnectionFactory: JsoupConnectionFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        jsoupConnectionFactory = JsoupConnectionFactory()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test createConnection with requestData`() {
        // Arrange
        val url = "https://example.com"
        val requestData = mapOf("key" to "value")

        every { mockConnection.userAgent(any()) } returns mockConnection
        every { mockConnection.headers(any()) } returns mockConnection
        every { mockConnection.data(requestData) } returns mockConnection

        mockkStatic(Jsoup::class)
        every { Jsoup.connect(url) } returns mockConnection

        // Act
        jsoupConnectionFactory.createConnection(url, requestData)

        // Assert
        verify {
            mockConnection.userAgent(JsoupConnectionFactory.USER_AGENT)
            mockConnection.headers(JsoupConnectionFactory.headers)
            mockConnection.data(requestData)
        }
        confirmVerified(mockConnection)
    }
}
