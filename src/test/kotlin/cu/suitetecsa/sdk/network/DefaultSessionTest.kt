package cu.suitetecsa.sdk.network

import cu.suitetecsa.sdk.nauta.domain.model.ResultType
import org.jsoup.Connection
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class DefaultSessionTest {

    private val connectionFactory: ConnectionFactory = mock()
    private val connection: Connection = mock()
    private val response: Connection.Response = mock()
    private val session = DefaultSession(connectionFactory)

    @Test
    fun testGetWhenGivenValidParametersThenReturnCorrectResult() {
        val url = "http://valid.url"
        val params = mapOf("param1" to "value1")
        val timeout = 30000
        val ignoreContentType = false

        whenever(connectionFactory.createConnection(url, params, mapOf())).thenReturn(connection)
        whenever(connection.ignoreContentType(ignoreContentType)).thenReturn(connection)
        whenever(connection.timeout(timeout)).thenReturn(connection)
        whenever(connection.method(Connection.Method.GET)).thenReturn(connection)
        whenever(connection.execute()).thenReturn(response)
        whenever(response.statusCode()).thenReturn(200)
        whenever(response.statusMessage()).thenReturn("OK")
        whenever(response.bodyAsBytes()).thenReturn("response".toByteArray())

        val result = session.get(url, params, ignoreContentType, timeout)

        assertTrue(result is ResultType.Success)
    }

    @Test
    fun testGetWhenGivenInvalidParametersThenReturnFailure() {
        val url = "http://invalid.url"
        val params = mapOf("param1" to "value1")
        val timeout = 30000
        val ignoreContentType = false

        whenever(connectionFactory.createConnection(url, params, mapOf())).thenReturn(connection)
        whenever(connection.ignoreContentType(ignoreContentType)).thenReturn(connection)
        whenever(connection.timeout(timeout)).thenReturn(connection)
        whenever(connection.method(Connection.Method.GET)).thenReturn(connection)
        whenever(connection.execute()).thenThrow(RuntimeException())

        val result = session.get(url, params, ignoreContentType, timeout)

        assertTrue(result is ResultType.Failure)
    }

    @Test
    fun testGetWhenExceptionThrownThenReturnFailure() {
        val url = "http://valid.url"
        val params = mapOf("param1" to "value1")
        val timeout = 30000
        val ignoreContentType = false
        val exception = RuntimeException()

        whenever(connectionFactory.createConnection(url, params, mapOf())).thenReturn(connection)
        whenever(connection.ignoreContentType(ignoreContentType)).thenReturn(connection)
        whenever(connection.timeout(timeout)).thenReturn(connection)
        whenever(connection.method(Connection.Method.GET)).thenReturn(connection)
        whenever(connection.execute()).thenThrow(exception)

        val result = session.get(url, params, ignoreContentType, timeout)

        assertTrue(result is ResultType.Failure)
        assertEquals(exception, (result as ResultType.Failure).throwable)
    }

    @Test
    fun testPostWhenGivenValidInputsThenReturnSuccess() {
        val url = "http://valid.url"
        val data = mapOf("param1" to "value1")

        whenever(connectionFactory.createConnection(url, data, mapOf())).thenReturn(connection)
        whenever(connection.method(Connection.Method.POST)).thenReturn(connection)
        whenever(connection.execute()).thenReturn(response)
        whenever(response.statusCode()).thenReturn(200)
        whenever(response.statusMessage()).thenReturn("OK")
        whenever(response.bodyAsBytes()).thenReturn("response".toByteArray())

        val result = session.post(url, data)

        assertTrue(result is ResultType.Success)
    }

    @Test
    fun testPostWhenGivenInvalidUrlThenReturnFailure() {
        val url = "http://invalid.url"
        val data = mapOf("param1" to "value1")

        whenever(connectionFactory.createConnection(url, data, mapOf())).thenReturn(connection)
        whenever(connection.method(Connection.Method.POST)).thenReturn(connection)
        whenever(connection.execute()).thenThrow(RuntimeException())

        val result = session.post(url, data)

        assertTrue(result is ResultType.Failure)
    }

    @Test
    fun testPostWhenGivenNoDataThenReturnSuccess() {
        val url = "http://valid.url"

        whenever(connectionFactory.createConnection(url, null, mapOf())).thenReturn(connection)
        whenever(connection.method(Connection.Method.POST)).thenReturn(connection)
        whenever(connection.execute()).thenReturn(response)
        whenever(response.statusCode()).thenReturn(200)
        whenever(response.statusMessage()).thenReturn("OK")
        whenever(response.bodyAsBytes()).thenReturn("response".toByteArray())

        val result = session.post(url, null)

        assertTrue(result is ResultType.Success)
    }

    @Test
    fun testPostWhenResponseHasNewCookiesThenUpdateCookiesMap() {
        val url = "http://valid.url"
        val data = mapOf("param1" to "value1")
        val newCookies = mapOf("cookie1" to "value1")

        whenever(connectionFactory.createConnection(url, data, mapOf())).thenReturn(connection)
        whenever(connection.method(Connection.Method.POST)).thenReturn(connection)
        whenever(connection.execute()).thenReturn(response)
        whenever(response.cookies()).thenReturn(newCookies)

        session.post(url, data)

        verify(connectionFactory).createConnection(url, data, mapOf())
    }
}