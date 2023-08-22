package cu.suitetecsa.sdk.nauta.framework

import cu.suitetecsa.sdk.nauta.core.extensions.toBytes
import cu.suitetecsa.sdk.nauta.core.extensions.toPriceFloat
import cu.suitetecsa.sdk.nauta.core.extensions.toSeconds
import cu.suitetecsa.sdk.nauta.domain.model.ConnectionsSummary
import cu.suitetecsa.sdk.nauta.domain.model.RechargesSummary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader

class UserPortalScrapperImplTest {
    private lateinit var userPortalScraper: UserPortalScrapperImpl

    @BeforeEach
    fun setup() {
        userPortalScraper = UserPortalScrapperImpl()
    }

    @Test
    fun `test parseConnectionsSummary for successful parse`() {
        val html = readResource("html/sd_summary.html") ?: ""

        val result = userPortalScraper.parseConnectionsSummary(html)

        assertEquals(
            ConnectionsSummary(
                47,
                "2023-03",
                "78:29:08".toSeconds(),
                "$461,40".toPriceFloat(),
                "1,57 GB".toBytes(),
                "17,40 GB".toBytes(),
                "18,97 GB".toBytes()
            ), result
        )
    }

    @Test
    fun `test parseRechargesSummary for successful parse`() {
        val html = readResource("html/rd_summary.html") ?: ""

        val result = userPortalScraper.parseRechargesSummary(html)

        assertEquals(RechargesSummary(2, "2023-03", "$450,00".toPriceFloat()), result)
    }

    @Test
    fun `test parseTransfersSummary for successful parse`() {
        val html = readResource("html/td_summary.html") ?: ""

        val result = userPortalScraper.parseRechargesSummary(html)

        assertEquals(RechargesSummary(0, "2023-03", "$0,00".toPriceFloat()), result)
    }

    @Test
    fun `test parseQuotesPaidSummary for successful parse`() {
        val html = readResource("html/qp_summary.html") ?: ""

        val result = userPortalScraper.parseRechargesSummary(html)

        assertEquals(RechargesSummary(1, "2023-03", "$300,00".toPriceFloat()), result)
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