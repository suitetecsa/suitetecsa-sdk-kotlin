package cu.suitetecsa.sdk.nauta.framework.network

import com.google.gson.Gson
import cu.suitetecsa.sdk.nauta.core.Action
import cu.suitetecsa.sdk.nauta.core.ActionType
import cu.suitetecsa.sdk.nauta.core.extensions.toBytes
import cu.suitetecsa.sdk.nauta.core.extensions.toPriceFloat
import cu.suitetecsa.sdk.nauta.core.extensions.toSeconds
import cu.suitetecsa.sdk.nauta.domain.model.*
import cu.suitetecsa.sdk.nauta.framework.UserPortalScrapperImpl
import cu.suitetecsa.sdk.nauta.framework.model.HttpResponse
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader

class UserPortalCommunicatorImplTest {

    private lateinit var mockNautaSession: NautaSession
    private lateinit var userPortalCommunicator: UserPortalCommunicatorImpl
    private lateinit var userPortalScraper: UserPortalScrapperImpl
    private lateinit var gson: Gson

    @BeforeEach
    fun setup() {
        mockNautaSession = mockk(relaxed = true)
        userPortalCommunicator = UserPortalCommunicatorImpl(mockNautaSession)
        userPortalScraper = UserPortalScrapperImpl()
        gson = Gson()
    }

    @Test
    fun `test performAction for successful GET request`() {
        // Arrange
        val mockResponse = HttpResponse(200, "OK", "Response content".toByteArray(), emptyMap())
        every { mockNautaSession.get(any(), any(), any(), any()) } returns ResultType.Success(mockResponse)

        // Act
        val result = userPortalCommunicator.performAction(Action.GetCaptcha) { it.text }

        // Assert
        result as ResultType.Success
        assertEquals("Response content", result.result)
    }

    @Test
    fun `test performAction for GET request with error`() {
        // Arrange
        val mockException = Exception("Connection error")
        every { mockNautaSession.get(any(), any(), any(), any()) } returns ResultType.Failure(mockException)

        // Act
        val result = userPortalCommunicator.performAction(Action.GetCaptcha) { it.text }

        // Assert
        result as ResultType.Failure
        assertEquals(mockException, result.throwable)
    }

    @Test
    fun `test performListAction for successful GET request with transformation`() {
        // Arrange
        val mockResponse = HttpResponse(200, "OK", "42\n43\n44".toByteArray(), emptyMap())
        every { mockNautaSession.get(any(), any(), any(), any()) } returns ResultType.Success(mockResponse)

        // Act
        val result = userPortalCommunicator.performListAction(
            Action.GetActions(
                3,
                "2023-08",
                0,
                false,
                ActionType.Connections
            )
        ) { it.text?.split("\n")?.map(String::toInt) ?: listOf() }

        // Assert
        result as ResultType.Success
        assertEquals(listOf(42, 43, 44), result.result)
    }

    @Test
    fun `test loadCsrf for successful GET request`() {
        // Arrange
        val mockResponse = HttpResponse(200, "OK", "CSRF-Token".toByteArray(), emptyMap())
        every { mockNautaSession.get(any(), any(), any(), any()) } returns ResultType.Success(mockResponse)

        // Act
        val result = userPortalCommunicator.loadCsrf("http://example.com") { it.text }

        // Assert
        result as ResultType.Success
        assertEquals("CSRF-Token", result.result)
    }

    @Test
    fun `test performAction for successful POST request with transformation to Connections summary`() {
        val mockResponse =
            HttpResponse(200, "OK", readResource("html/sd_summary.html")?.toByteArray() ?: "".toByteArray(), emptyMap())
        every {
            mockNautaSession.post(
                "https://www.portal.nauta.cu/useraaa/service_detail_summary/",
                mapOf(
                    "csrf" to "security6416bea61ad2b",
                    "year_month" to "2023-03",
                    "list_type" to "service_detail"
                )
            )
        } returns ResultType.Success(
            mockResponse
        )

        val result = userPortalCommunicator.performAction(
            Action.GetSummary("security6416bea61ad2b", 2023, 3, ActionType.Connections)
        ) {
            userPortalScraper.parseConnectionsSummary(it.text ?: "")
        }

        result as ResultType.Success
        assertEquals(
            ConnectionsSummary(
                47,
                "2023-03",
                "78:29:08".toSeconds(),
                "$461,40".toPriceFloat(),
                "1,57 GB".toBytes(),
                "17,40 GB".toBytes(),
                "18,97 GB".toBytes()
            ), result.result
        )
    }

    @Test
    fun `test performAction for successful POST request with transformation to Recharges summary`() {
        val mockResponse =
            HttpResponse(200, "OK", readResource("html/rd_summary.html")?.toByteArray() ?: "".toByteArray(), emptyMap())
        every {
            mockNautaSession.post(
                "https://www.portal.nauta.cu/useraaa/recharge_detail_summary/",
                mapOf(
                    "csrf" to "security6416bea61ad2b",
                    "year_month" to "2023-03",
                    "list_type" to "recharge_detail"
                )
            )
        } returns ResultType.Success(
            mockResponse
        )

        val result = userPortalCommunicator.performAction(
            Action.GetSummary("security6416bea61ad2b", 2023, 3, ActionType.Recharges)
        ) {
            userPortalScraper.parseRechargesSummary(it.text ?: "")
        }

        result as ResultType.Success
        assertEquals(
            RechargesSummary(
                2,
                "2023-03",
                "$450,00".toPriceFloat()
            ), result.result
        )
    }

    @Test
    fun `test performAction for successful POST request with transformation to Transfers summary`() {
        val mockResponse =
            HttpResponse(200, "OK", readResource("html/td_summary.html")?.toByteArray() ?: "".toByteArray(), emptyMap())
        every {
            mockNautaSession.post(
                "https://www.portal.nauta.cu/useraaa/transfer_detail_summary/",
                mapOf(
                    "csrf" to "security6416bea61ad2b",
                    "year_month" to "2023-03",
                    "list_type" to "transfer_detail"
                )
            )
        } returns ResultType.Success(
            mockResponse
        )

        val result = userPortalCommunicator.performAction(
            Action.GetSummary("security6416bea61ad2b", 2023, 3, ActionType.Transfers)
        ) {
            userPortalScraper.parseTransfersSummary(it.text ?: "")
        }

        result as ResultType.Success
        assertEquals(
            TransfersSummary(
                0,
                "2023-03",
                "$0,00".toPriceFloat()
            ), result.result
        )
    }

    @Test
    fun `test performAction for successful POST request with transformation to QuotesPaid summary`() {
        val mockResponse =
            HttpResponse(200, "OK", readResource("html/qp_summary.html")?.toByteArray() ?: "".toByteArray(), emptyMap())
        every {
            mockNautaSession.post(
                "https://www.portal.nauta.cu/useraaa/nautahogarpaid_detail_summary/",
                mapOf(
                    "csrf" to "security6416bea61ad2b",
                    "year_month" to "2023-03",
                    "list_type" to "nautahogarpaid_detail"
                )
            )
        } returns ResultType.Success(
            mockResponse
        )

        val result = userPortalCommunicator.performAction(
            Action.GetSummary("security6416bea61ad2b", 2023, 3, ActionType.QuotesPaid)
        ) {
            userPortalScraper.parseQuotesPaidSummary(it.text ?: "")
        }

        result as ResultType.Success
        assertEquals(
            QuotesPaidSummary(
                1,
                "2023-03",
                "$300,00".toPriceFloat()
            ), result.result
        )
    }

    @Test
    fun `test performListAction for successful GET request with transformation to list connections`() {
        val mockPageOneResponse = HttpResponse(
            200,
            "OK",
            readResource("html/sdl_2023-03_47.html")?.toByteArray() ?: "".toByteArray(),
            emptyMap()
        )
        val mockPageTwoResponse = HttpResponse(
            200,
            "OK",
            readResource("html/sdl_2023-03_2.html")?.toByteArray() ?: "".toByteArray(),
            emptyMap()
        )
        val mockPageThreeResponse = HttpResponse(
            200,
            "OK",
            readResource("html/sdl_2023-03_3.html")?.toByteArray() ?: "".toByteArray(),
            emptyMap()
        )
        val mockPageFourResponse = HttpResponse(
            200,
            "OK",
            readResource("html/sdl_2023-03_4.html")?.toByteArray() ?: "".toByteArray(),
            emptyMap()
        )
        val jsonText = readResource("json/connects_2023_03.json")
        val responseExpected = gson.fromJson(jsonText, Array<Connection>::class.java).toList()

        every {
            mockNautaSession.get("https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47")
        } returns ResultType.Success(mockPageOneResponse)
        every {
            mockNautaSession.get("https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47/2")
        } returns ResultType.Success(mockPageTwoResponse)
        every {
            mockNautaSession.get("https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47/3")
        } returns ResultType.Success(mockPageThreeResponse)
        every {
            mockNautaSession.get("https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47/4")
        } returns ResultType.Success(mockPageFourResponse)

        val result = userPortalCommunicator.performListAction(
            Action.GetActions(47, "2023-03", 47, false, ActionType.Connections)
        ) { userPortalScraper.parseConnections(it.text ?: "") }

        result as ResultType.Success
        assertEquals(responseExpected, result.result)
    }

    @Test
    fun `test performListAction for successful GET request with transformation to list recharges`() {
        val mockPageOneResponse = HttpResponse(
            200,
            "OK",
            readResource("html/rdl_2023_03_2.html")?.toByteArray() ?: "".toByteArray(),
            emptyMap()
        )
        every {
            mockNautaSession.get("https://www.portal.nauta.cu/useraaa/recharge_detail_list/2023-03/2")
        } returns ResultType.Success(mockPageOneResponse)

        val result = userPortalCommunicator.performListAction(
            Action.GetActions(2, "2023-03", 0, false, ActionType.Recharges)
        ) { userPortalScraper.parseRecharges(it.text ?: "") }

        val jsonText = readResource("json/recharges_2023_03.json")
        val responseExpected = gson.fromJson(jsonText, Array<Recharge>::class.java).toList()

        result as ResultType.Success
        assertEquals(responseExpected, result.result)
    }

    @Test
    fun `test performListAction for successful GET request with transformation to list transfers`() {

        val result = userPortalCommunicator.performListAction(
            Action.GetActions(0, "2023-03", 0, false, ActionType.Transfers)
        ) { userPortalScraper.parseTransfers(it.text ?: "") }

        result as ResultType.Success
        assertEquals(emptyList<Transfer>(), result.result)
    }

    @Test
    fun `test performListAction for successful GET request with transformation to list quotesPaid`() {
        val mockPageOneResponse = HttpResponse(
            200,
            "OK",
            readResource("html/qpl_2023_03_1.html")?.toByteArray() ?: "".toByteArray(),
            emptyMap()
        )
        val jsonText = readResource("json/quotes_paid_2023_03.json")
        val responseExpected = gson.fromJson(jsonText, Array<QuotePaid>::class.java).toList()

        every {
            mockNautaSession.get("https://www.portal.nauta.cu/useraaa/nautahogarpaid_detail_list/2023-03/1")
        } returns ResultType.Success(mockPageOneResponse)

        val result = userPortalCommunicator.performListAction(
            Action.GetActions(1, "2023-03", 0, false, ActionType.QuotesPaid)
        ) { userPortalScraper.parseQuotesPaid(it.text ?: "") }

        result as ResultType.Success
        assertEquals(responseExpected, result.result)
    }

    private fun readResource(path: String): String? {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader.getResourceAsStream(path)
        val reader = inputStream?.let { InputStreamReader(it) }?.let { BufferedReader(it) }
        val content = reader?.readText()
        reader?.close()
        return content
    }
}
