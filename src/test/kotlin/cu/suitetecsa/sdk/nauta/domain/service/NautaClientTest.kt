package cu.suitetecsa.sdk.nauta.domain.service

import com.google.gson.Gson
import cu.suitetecsa.sdk.nauta.core.exceptions.LoadInfoException
import cu.suitetecsa.sdk.nauta.core.Portal
import cu.suitetecsa.sdk.nauta.data.model.HttpResponse
import cu.suitetecsa.sdk.nauta.data.model.ResultType
import cu.suitetecsa.sdk.nauta.domain.*
import cu.suitetecsa.sdk.nauta.domain.model.*
import cu.suitetecsa.sdk.nauta.data.repository.JSoupNautaScrapper
import cu.suitetecsa.sdk.nauta.data.repository.NautaSession
import io.mockk.every
import io.mockk.mockk
import org.jsoup.Connection
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.test.assertFailsWith

class NautaSessionTest: NautaSession {
    private val cCookies = mutableMapOf<String, String>()
    private val uCookies = mutableMapOf<String, String>()
    private val responseMockk = mockk<Connection.Response>()


    override val userCookies: MutableMap<String, String>
        get() = uCookies
    override val connectCookies: MutableMap<String, String>
        get() = cCookies
    override var csrf: String? = null
    override var userName: String? = null
    override var csrfHw: String? = null
    override var wlanUserIp: String? = null
    override var attributeUUID: String? = null
    override var actionLogin: String? = null
    override var isNautaHome: Boolean = false

    private val landingHtml = readResource("html/landing.html")
    private val loginHtml = readResource("html/login_page.html")
    private val loggedInHtml = readResource("html/logged_in.html")

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
        every { responseMockk.cookies() } returns mapOf()
    }

    override fun get(
        portalManager: Portal,
        url: String,
        params: Map<String, String>?,
        ignoreContentType: Boolean,
        timeout: Int?
    ): ResultType<HttpResponse> {
        setUp()
        when (url) {
            "http://www.cubadebate.cu/" -> every { responseMockk.body() } returns landingHtml!!
            "https://www.portal.nauta.cu/captcha/?" -> every { responseMockk.bodyAsBytes() } returns captchaByteArray
            "https://www.portal.nauta.cu/user/login/es-es" -> every { responseMockk.body() } returns csrfHtml!!
            "https://www.portal.nauta.cu/useraaa/user_info" -> every { responseMockk.body() } returns userInfoHtml!!
            "https://www.portal.nauta.cu/useraaa/service_detail/" -> every { responseMockk.body() } returns csrfHtml!!
            "https://www.portal.nauta.cu/useraaa/transfer_detail/" -> every { responseMockk.body() } returns csrfHtml!!
            "https://www.portal.nauta.cu/useraaa/recharge_account" -> every { responseMockk.body() } returns csrfHtml!!
            "https://www.portal.nauta.cu/useraaa/transfer_balance" -> every { responseMockk.body() } returns csrfHtml!!
            "https://www.portal.nauta.cu/useraaa/change_password" -> every { responseMockk.body() } returns csrfHtml!!
            "https://www.portal.nauta.cu/mail/change_password" -> every { responseMockk.body() } returns csrfHtml!!
            "https://www.portal.nauta.cu/useraaa/nautahogarpaid_detail/" -> every { responseMockk.body() } returns csrfHtml!!
            "https://www.portal.nauta.cu/useraaa/recharge_detail/" -> every { responseMockk.body() } returns csrfHtml!!
            "https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47" -> every { responseMockk.body() } returns sdl20230347Html!!
            "https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47/2" -> every { responseMockk.body() } returns sdl2023032Html!!
            "https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47/3" -> every { responseMockk.body() } returns sdl2023033Html!!
            "https://www.portal.nauta.cu/useraaa/service_detail_list/2023-03/47/4" -> every { responseMockk.body() } returns sdl2023034Html!!
            "https://www.portal.nauta.cu/useraaa/recharge_detail_list/2023-03/2" -> every { responseMockk.body() } returns rdl2023032Html!!
            "https://www.portal.nauta.cu/useraaa/nautahogarpaid_detail_list/2023-03/1" -> every { responseMockk.body() } returns qpl2023031Html!!
        }
        return responseMockk
    }

    override fun post(portalManager: Portal, url: String, data: Map<String, String>): ResultType<HttpResponse> {
        setUp()
        when (url) {
            "https://secure.etecsa.net:8443" -> every { responseMockk.body() } returns loginHtml!!
            "https://secure.etecsa.net:8443//LoginServlet" -> every { responseMockk.body() } returns loggedInHtml!!
            "https://www.portal.nauta.cu/user/login/es-es" -> every { responseMockk.body() } returns userInfoHtml!!
            "https://www.portal.nauta.cu/useraaa/recharge_account" -> every { responseMockk.body() } returns csrfHtml!!
            "https://www.portal.nauta.cu/useraaa/transfer_balance" -> every { responseMockk.body() } returns csrfHtml!!
            "https://www.portal.nauta.cu/useraaa/service_detail_summary/" -> every { responseMockk.body() } returns sdSummary!!
            "https://www.portal.nauta.cu/useraaa/recharge_detail_summary/" -> every { responseMockk.body() } returns rdSummary!!
            "https://www.portal.nauta.cu/useraaa/transfer_detail_summary/" -> every { responseMockk.body() } returns tdSummary!!
            "https://www.portal.nauta.cu/useraaa/nautahogarpaid_detail_summary/" -> every { responseMockk.body() } returns qpSummary!!
        }
        return responseMockk
    }

}

class NautaClientTest {
    private val session = NautaSessionTest()
    private val provider = JSoupNautaScrapper(session)
    private val client = NautaClient(provider)
    private val gson = Gson()

    private val userInfoJson = session.readResource("json/user_info.json")
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
        assertTrue(client.isNautaHome)
    }

    @Test
    fun setDataSession() {
        assertFailsWith<LoadInfoException> {
            client.dataSession = mapOf()
        }
    }

    @Test
    fun getCredentials() {
        login()
        val credentials = client.getCredentials()
        assertEquals(credentials, Pair("user.name@nauta.com.cu", "somePassword"))
    }

    @Test
    fun setCredentials() {
        assertFailsWith<LoadInfoException> {
            client.setCredentials("", "")
        }
    }

    @Test
    fun connect() {
        client.setCredentials("user.name@nauta.com.cu", "somePassword")
        client.connect()
        val dataSession = client.dataSession
        val dataCompare = mapOf(
            "username" to "user.name@nauta.com.cu",
            "CSRFHW" to "1fe3ee0634195096337177a0994723fb",
            "wlanuserip" to "10.190.20.96",
            "ATTRIBUTE_UUID" to "B2F6AAB9A9868BABC0BDC6B7A235ABE2"
        )
        assertEquals(dataSession, dataCompare)
    }

    @Test
    fun login() {
        client.setCredentials("user.name@nauta.com.cu", "somePassword")
        client.captchaImage
        val user = client.login("captchaCode")
        assertEquals(user, gson.fromJson(userInfoJson, NautaUser::class.java))
    }

    @Test
    fun getConnectInformation() {
        login()
        val connections = client.getConnections(2023, 3)
        val compare = gson.fromJson(connectsJson, Array<cu.suitetecsa.sdk.nauta.domain.model.Connection>::class.java)
        assertEquals(connections, compare.toList())
    }

    @Test
    fun getUserInformation() {
        login()
        val user = client.userInformation
        assertEquals(user, gson.fromJson(userInfoJson, NautaUser::class.java))
    }

    @Test
    fun toUpBalance() {
        login()
        client.toUpBalance("1234567890123456")
    }

    @Test
    fun transferBalance() {
        login()
        client.transferBalance(25.0f, "otherUser.name@nauta.com.cu")
    }

    @Test
    fun payNautaHome() {
        login()
        client.payNautaHome(300f)
    }

    @Test
    fun changePassword() {
        login()
        client.changePassword("lololala")
    }

    @Test
    fun changeEmailPassword() {
        login()
        client.changeEmailPassword("lololala", "lalalolo")
    }

    @Test
    fun getConnectionsSummary() {
        login()
        val summary = client.getConnectionsSummary(2023, 3)
        assertEquals(summary, gson.fromJson(connectsSummaryJson, ConnectionsSummary::class.java))
    }

    @Test
    fun getConnections() {
        login()
        val connections = client.getConnections(2023, 3)
        val compare = gson.fromJson(connectsJson, Array<cu.suitetecsa.sdk.nauta.domain.model.Connection>::class.java).toList()
        assertEquals(connections, compare)
    }

    @Test
    fun getRechargesSummary() {
        login()
        val rechargesSummary = client.getRechargesSummary(2023, 3)
        assertEquals(rechargesSummary, gson.fromJson(rechargesSummaryJson, RechargesSummary::class.java))
    }

    @Test
    fun getRecharges() {
        login()
        val recharges = client.getRecharges(2023, 3)
        assertEquals(recharges, gson.fromJson(rechargesJson, Array<Recharge>::class.java).toList())
    }

    @Test
    fun getTransfersSummary() {
        login()
        val summary = client.getTransfersSummary(2023, 3)
        assertEquals(summary, gson.fromJson(transfersSummaryJson, TransfersSummary::class.java))
    }

    @Test
    fun getTransfers() {
        login()
        val transfers = client.getTransfers(2023, 3)
        assertEquals(gson.toJson(transfers), transfersJson)
    }

    @Test
    fun getQuotesPaidSummary() {
        login()
        val summary = client.getQuotesPaidSummary(2023, 3)
        assertEquals(summary, gson.fromJson(quotesPaidSummaryJson, QuotesPaidSummary::class.java))
    }

    @Test
    fun getQuotesPaid() {
        login()
        val quotesPaid = client.getQuotesPaid(2023, 3)
        assertEquals(quotesPaid, gson.fromJson(quotesPaidJson, Array<QuotePaid>::class.java).toList())
    }
}