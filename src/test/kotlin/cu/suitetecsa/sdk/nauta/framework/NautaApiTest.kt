package cu.suitetecsa.sdk.nauta.framework

import com.google.gson.Gson
import cu.suitetecsa.sdk.nauta.core.Portal
import cu.suitetecsa.sdk.nauta.framework.model.DataSession
import cu.suitetecsa.sdk.nauta.framework.model.HttpResponse
import cu.suitetecsa.sdk.nauta.framework.model.NautaConnectInformation
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import cu.suitetecsa.sdk.nauta.framework.network.JsoupConnectPortalCommunicator
import cu.suitetecsa.sdk.nauta.framework.network.JsoupUserPortalCommunicator
import cu.suitetecsa.sdk.nauta.framework.network.NautaSession
import cu.suitetecsa.sdk.nauta.domain.model.*
import io.mockk.every
import io.mockk.mockk
import okio.ByteString.Companion.encode
import org.jsoup.Connection
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.BufferedReader
import java.io.InputStreamReader

class NautaSessionTest : NautaSession {

    private val _cookies = mutableMapOf<String, String>()
    private var portalManager = Portal.CONNECT
    private val responseMockk = mockk<Connection.Response>()

    private val landingHtml = readResource("html/landing.html")
    private val loginHtml = readResource("html/login_page.html")
    private val loggedInHtml = readResource("html/logged_in.html")
    private val userInfoConnectHtml = readResource("html/user_info_connect.html")

    private val userInfoHtml = readResource("html/user_info.html")
    private val captchaByteArray = readImageFromResources("img/captcha_image.png")
    private val csrfHtml = readResource("html/csrf_token.html")
    private val sdSummary = readResource("html/sd_summary.html")
    private val sdl20230347Html = readResource("html/sdl_2023-03_47.html")
    private val sdl2023032Html = readResource("html/sdl_2023-03_2.html")
    private val sdl2023033Html = readResource("html/sdl_2023-03_3.html")
    private val sdl2023034Html = readResource("html/sdl_2023-03_4.html")
    private val rdSummary = readResource("html/rd_summary.html")
    private val rdl2023032Html = readResource("html/rdl_2023_03_2.html")
    private val tdSummary = readResource("html/td_summary.html")
    private val qpSummary = readResource("html/qp_summary.html")
    private val qpl2023031Html = readResource("html/qpl_2023_03_1.html")

    fun readResource(path: String): String? {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader.getResourceAsStream(path)
        val reader = inputStream?.let { InputStreamReader(it) }?.let { BufferedReader(it) }
        val content = reader?.readText()
        reader?.close()
        inputStream?.close()
        return content
    }

    private fun readImageFromResources(imageName: String): ByteArray {
        return this::class.java.classLoader.getResourceAsStream(imageName)?.readBytes()
            ?: throw RuntimeException("Image not found in resources")
    }

    private fun setUp() {
        every { responseMockk.statusCode() } returns 200
        every { responseMockk.statusMessage() } returns ""
        every { responseMockk.cookies() } returns mapOf()
    }

    override val cookies: MutableMap<String, String>
        get() = _cookies

    override fun setPortalManager(portalManager: Portal) {
        this.portalManager = portalManager
    }

    override fun get(
        url: String,
        params: Map<String, String>?,
        ignoreContentType: Boolean,
        timeout: Int?
    ): ResultType<HttpResponse> {
        setUp()
        when (url) {
            "http://www.cubadebate.cu/" -> every { responseMockk.bodyAsBytes() } returns landingHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/captcha/?" -> every { responseMockk.bodyAsBytes() } returns captchaByteArray
            "https://www.portal.nauta.cu/user/login/es-es" -> every { responseMockk.bodyAsBytes() } returns csrfHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/user_info" -> every { responseMockk.bodyAsBytes() } returns userInfoHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/service_detail/" -> every { responseMockk.bodyAsBytes() } returns csrfHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/transfer_detail/" -> every { responseMockk.bodyAsBytes() } returns csrfHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/recharge_account" -> every { responseMockk.bodyAsBytes() } returns csrfHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/transfer_balance" -> every { responseMockk.bodyAsBytes() } returns csrfHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/change_password" -> every { responseMockk.bodyAsBytes() } returns csrfHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/mail/change_password" -> every { responseMockk.bodyAsBytes() } returns csrfHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/nautahogarpaid_detail/" -> every { responseMockk.bodyAsBytes() } returns csrfHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/recharge_detail/" -> every { responseMockk.bodyAsBytes() } returns csrfHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47" -> every { responseMockk.bodyAsBytes() } returns sdl20230347Html!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47/2" -> every { responseMockk.bodyAsBytes() } returns sdl2023032Html!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47/3" -> every { responseMockk.bodyAsBytes() } returns sdl2023033Html!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47/4" -> every { responseMockk.bodyAsBytes() } returns sdl2023034Html!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/recharge_detail_list/2023-03/2" -> every { responseMockk.bodyAsBytes() } returns rdl2023032Html!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/nautahogarpaid_detail_list/2023-03/1" -> every { responseMockk.bodyAsBytes() } returns qpl2023031Html!!.encode().toByteArray()
        }
        return ResultType.Success(
            HttpResponse(
                statusCode = responseMockk.statusCode(),
                statusMassage = responseMockk.statusMessage(),
                content = responseMockk.bodyAsBytes(),
                cookies = responseMockk.cookies()
            )
        )
    }

    override fun post(url: String, data: Map<String, String>): ResultType<HttpResponse> {
        setUp()
        when (url) {
            "https://secure.etecsa.net:8443" -> every { responseMockk.bodyAsBytes() } returns loginHtml!!.encode().toByteArray()
            "https://secure.etecsa.net:8443//LoginServlet" -> every { responseMockk.bodyAsBytes() } returns loggedInHtml!!.encode().toByteArray()
            "https://secure.etecsa.net:8443/EtecsaQueryServlet" -> every { responseMockk.bodyAsBytes() } returns userInfoConnectHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/user/login/es-es" -> every { responseMockk.bodyAsBytes() } returns userInfoHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/recharge_account" -> every { responseMockk.bodyAsBytes() } returns csrfHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/transfer_balance" -> every { responseMockk.bodyAsBytes() } returns csrfHtml!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/service_detail_summary/" -> every { responseMockk.bodyAsBytes() } returns sdSummary!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/recharge_detail_summary/" -> every { responseMockk.bodyAsBytes() } returns rdSummary!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/transfer_detail_summary/" -> every { responseMockk.bodyAsBytes() } returns tdSummary!!.encode().toByteArray()
            "https://www.portal.nauta.cu/useraaa/nautahogarpaid_detail_summary/" -> every { responseMockk.bodyAsBytes() } returns qpSummary!!.encode().toByteArray()
        }
        return ResultType.Success(
            HttpResponse(
                statusCode = responseMockk.statusCode(),
                statusMassage = responseMockk.statusMessage(),
                content = responseMockk.bodyAsBytes(),
                cookies = responseMockk.cookies()
            )
        )
    }
}

class NautaApiTest {
    private val session = NautaSessionTest()
    private val connectPortalCommunicator = JsoupConnectPortalCommunicator(session)
    private val connectPortalScraper = JsoupConnectPortalScraper()
    private val userPortalCommunicator = JsoupUserPortalCommunicator(NautaSessionTest())
    private val userPortalScraper = JsoupUserPortalScrapper()
    private val api =
        NautaApi(connectPortalCommunicator, connectPortalScraper, userPortalCommunicator, userPortalScraper)
    private val gson = Gson()

    private val userInfoJson = session.readResource("json/user_info.json")
    private val userInfoConnectJson = session.readResource("json/user_info_connect.json")
    private val connectsSummaryJson = session.readResource("json/connects_summary.json")
    private val connectsJson = session.readResource("json/connects_2023_03.json")
    private val rechargesSummaryJson = session.readResource("json/recharges_summary.json")
    private val rechargesJson = session.readResource("json/recharges_2023_03.json")
    private val transfersSummaryJson = session.readResource("json/transfers_summary.json")
    private val transfersJson = session.readResource("json/transfers_2023_03.json")
    private val quotesPaidSummaryJson = session.readResource("json/quotes_paid_summary.json")
    private val quotesPaidJson = session.readResource("json/quotes_paid_2023_03.json")

    @Test
    fun isNautaHome() {
        login()
        assertTrue(api.isNautaHome)
    }

    @Test
    fun isConnected() {
        assertEquals(api.isConnected, false)
    }

    @Test
    fun connect() {
        api.setCredentials("user.name@nauta.com.cu", "somePassword")
        api.connect()
        val dataSession = api.dataSession
        val dataCompare = DataSession(
            username = "user.name@nauta.com.cu",
            csrfHw = "1fe3ee0634195096337177a0994723fb",
            wlanUserIp =  "10.190.20.96",
            attributeUUID =  "B2F6AAB9A9868BABC0BDC6B7A235ABE2"
        )
        assertEquals(dataSession, dataCompare)
    }

    @Test
    fun disconnect() {
        connect()
        api.disconnect()
    }

    @Test
    fun getConnectInformation() {
        api.setCredentials("user.name@nauta.com.cu", "somePassword")
        val connectInformation = api.connectInformation
        assertEquals(connectInformation, gson.fromJson(userInfoConnectJson, NautaConnectInformation::class.java))
    }

    @Test
    fun getUserInformation() {
        login()
        val user = api.userInformation
        assertEquals(user, gson.fromJson(userInfoJson, NautaUser::class.java))
    }

    @Test
    fun login() {
        api.setCredentials("user.name@nauta.com.cu", "somePassword")
        api.captchaImage
        val user = api.login("captchaCode")
        assertEquals(user, gson.fromJson(userInfoJson, NautaUser::class.java))
    }

    @Test
    fun topUp() {
        login()
        api.topUp("1234567890123456")
    }

    @Test
    fun transferFunds() {
        login()
        api.transferFunds(25.0f, "otherUser.name@nauta.com.cu")
    }

    @Test
    fun changePassword() {
        login()
        api.changePassword("lololala")
    }

    @Test
    fun changeEmailPassword() {
        login()
        api.changeEmailPassword("lololala", "lalalolo")
    }

    @Test
    fun getConnectionsSummary() {
        login()
        val summary = api.getConnectionsSummary(2023, 3)
        assertEquals(summary, gson.fromJson(connectsSummaryJson, ConnectionsSummary::class.java))
    }

    @Test
    fun getRechargesSummary() {
        login()
        val rechargesSummary = api.getRechargesSummary(2023, 3)
        assertEquals(rechargesSummary, gson.fromJson(rechargesSummaryJson, RechargesSummary::class.java))
    }

    @Test
    fun getTransfersSummary() {
        login()
        val summary = api.getTransfersSummary(2023, 3)
        assertEquals(summary, gson.fromJson(transfersSummaryJson, TransfersSummary::class.java))
    }

    @Test
    fun getQuotesPaidSummary() {
        login()
        val summary = api.getQuotesPaidSummary(2023, 3)
        assertEquals(summary, gson.fromJson(quotesPaidSummaryJson, QuotesPaidSummary::class.java))
    }

    @Test
    fun getConnections() {
        login()
        val connections = api.getConnections(api.getConnectionsSummary(2023, 3))
        val compare = gson.fromJson(connectsJson, Array<cu.suitetecsa.sdk.nauta.domain.model.Connection>::class.java).toList()
        assertEquals(connections, compare)
    }

    @Test
    fun getRecharges() {
        login()
        val recharges = api.getRecharges(api.getRechargesSummary(2023, 3))
        assertEquals(recharges, gson.fromJson(rechargesJson, Array<Recharge>::class.java).toList())
    }

    @Test
    fun getTransfers() {
        login()
        val transfers = api.getTransfers(api.getTransfersSummary(2023, 3))
        assertEquals(gson.toJson(transfers), transfersJson)
    }

    @Test
    fun getQuotesPaid() {
        login()
        val quotesPaid = api.getQuotesPaid(api.getQuotesPaidSummary(2023, 3))
        assertEquals(quotesPaid, gson.fromJson(quotesPaidJson, Array<QuotePaid>::class.java).toList())
    }
}