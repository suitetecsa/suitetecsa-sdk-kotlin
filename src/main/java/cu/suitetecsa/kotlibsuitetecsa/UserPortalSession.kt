package cu.suitetecsa.kotlibsuitetecsa

import cu.suitetecsa.kotlibsuitetecsa.models.Connection
import cu.suitetecsa.kotlibsuitetecsa.models.QuoteFund
import cu.suitetecsa.kotlibsuitetecsa.models.Recharge
import cu.suitetecsa.kotlibsuitetecsa.models.Transfer
import cu.suitetecsa.kotlibsuitetecsa.utils.Operation
import org.jsoup.Connection.Response
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.net.UnknownHostException
import java.util.*
import javax.net.ssl.SSLHandshakeException

class UserPortalSession(override var cookies: MutableMap<String, String> = mutableMapOf()) : Session {
    // Constantes privadas de la session
    private var urlBase = "https://www.portal.nauta.cu/"
    private val urls = mutableMapOf(
        Operation.LOGIN to "user/login/es-es",
        Operation.LOAD_USER_INFO to "useraaa/user_info",
        Operation.RECHARGE to "useraaa/recharge_account",
        Operation.CHANGE_PASSWORD to "useraaa/change_password",
        Operation.CHANGE_EMAIL_PASSWORD to "email/change_password",
        Operation.CONNECTIONS to "useraaa/service_detail/",
        Operation.CONNECTIONS_SUMMARY to "useraaa/service_detail_summary/",
        Operation.CONNECTIONS_LIST to "useraaa/service_detail_list/",
        Operation.RECHARGES to "useraaa/recharge_detail/",
        Operation.RECHARGES_SUMMARY to "useraaa/recharge_detail_summary/",
        Operation.RECHARGES_LIST to "useraaa/recharge_detail_list/",
        Operation.QUOTES_FUNDS to "useraaa/nautahogarpaid_detail/",
        Operation.QUOTES_FUNDS_SUMMARY to
                "useraaa/nautahogarpaid_detail_summary/",
        Operation.QUOTES_FUNDS_LIST to
                "useraaa/nautahogarpaid_detail_list/",
        Operation.TRANSFERS to "useraaa/transfer_detail/",
        Operation.TRANSFERS_SUMMARY to "useraaa/transfer_detail_summary/",
        Operation.TRANSFERS_LIST to "useraaa/transfer_detail_list/"
    )
    private val attrs = mapOf(
        Attribute.USER_NAME to "usuario",
        Attribute.ACCOUNT_TYPE to "tipo de cuenta",
        Attribute.SERVICE_TYPE to "tipo de servicio",
        Attribute.CREDIT to "saldo disponible",
        Attribute.TIME to "tiempo disponible de la cuenta",
        Attribute.MAIL_ACCOUNT to "cuenta de correo",
        Attribute.OFFER to "oferta",
        Attribute.MONTHLY_FEE to "cuota mensual",
        Attribute.DOWNLOAD_SPEEDS to "velocidad de bajada",
        Attribute.UPLOAD_SPEEDS to "velocidad de subida",
        Attribute.PHONE to "teléfono",
        Attribute.LINK_IDENTIFIERS to "identificador del enlace",
        Attribute.LINK_STATUS to "estado del enlace",
        Attribute.ACTIVATION_DATE to "fecha de activación",
        Attribute.BLOCKING_DATE to "fecha de bloqueo",
        Attribute.DATE_OF_ELIMINATION to "fecha de eliminación",
        Attribute.BLOCKING_DATE_HOME to "fecha de bloqueo",
        Attribute.DATE_OF_ELIMINATION_HOME to "fecha de eliminación",
        Attribute.QUOTA_FUND to "fondo de cuota",
        Attribute.VOUCHER to "bono",
        Attribute.DEBT to "deuda"
    )

    //Atributos de la session
    private var csrf: String? = null

    // Atributos para cuentas nauta estandar.
    var userName: String? = null
    var blockingDate: String? = null
    var dateOfElimination: String? = null
    var accountType: String? = null
    var serviceType: String? = null
    var credit: String? = null
    var time: String? = null
    var mailAccount: String? = null

    // Atributos para cuentas nauta hogar
    var offer: String? = null
    var monthlyFee: String? = null
    var downloadSpeeds: String? = null
    var uploadSpeeds: String? = null
    var phone: String? = null
    var linkIdentifiers: String? = null
    var linkStatus: String? = null
    var activationDate: String? = null
    var blockingDateHome: String? = null
    var dateOfEliminationHome: String? = null
    var quoteFund: String? = null
    var voucher: String? = null
    var debt: String? = null

    private fun throwIfError(response: Response) {
        if (response.statusCode() != 200) {
            throw OperationException(
                "Fallo al realizar la operación: ${response.statusCode()} - ${response.statusMessage()}"
            )
        }
    }

    @kotlin.jvm.Throws(CommunicationException::class)
    private fun getCsrf(action: Operation) {
        try {
            urls[action]?.let {
                if (cookies.isEmpty()) {
                    cookies = getCookies("${urlBase}${it}")
                }
                val soup = connect("${urlBase}${it}").get()
                val input = soup.select("input[name='csrf']").first()
                csrf = input?.attr("value")
            }
        } catch (e: UnknownHostException) {
            throw CommunicationException("No pudimos encontrar el servidor: ${e.message}")
        } catch (e2: SSLHandshakeException) {
            throw CommunicationException(
                "Se encuentra bajo un portal cautivo o están intentado suplantar el sitio."
            )
        }
    }

    private val isSessionInitialized: Boolean
        get() {
            return csrf != null
        }

    fun getCaptchaAsBytes(): ByteArray {
        try {
            return connect("https://www.portal.nauta.cu/captcha/?")
                .ignoreContentType(true)
                .timeout(25000)
                .cookies(cookies)
                .execute()
                .bodyAsBytes()
        } catch (e: UnknownHostException) {
            throw CommunicationException("No pudimos encontrar el servidor: ${e.message}")
        } catch (e2: SSLHandshakeException) {
            throw CommunicationException(
                "Se encuentra bajo un portal cautivo o están intentado suplantar el sitio."
            )
        }
    }

    fun init() {
        getCsrf(Operation.LOGIN)
    }

    fun login(
        userName: String,
        password: String,
        captchaCode: String,
        cookies: MutableMap<String, String>
    ) {
        if (isSessionInitialized) {
            this.cookies = cookies
            val soup = connect(
                "${urlBase}${urls[Operation.LOGIN]}",
                mapOf(
                    "csrf" to csrf!!,
                    "login_user" to userName,
                    "password_user" to password,
                    "captcha" to captchaCode,
                    "btn_submit" to ""
                )
            ).post()
            updateAttrs(soup)
        }
    }

    fun loadUserInfo(cookies: MutableMap<String, String>) {
        this.cookies = cookies
        val soup = urls[Operation.LOAD_USER_INFO]?.let {
            connect("${urlBase}${it}").get()
        }

        soup?.let { updateAttrs(it) }
    }

    fun recharge(rechargeCode: String, cookies: MutableMap<String, String>) {
        getCsrf(Operation.RECHARGE)
        postAction(
            cookies,
            mapOf(
                "csrf" to csrf!!,
                "recharge_code" to rechargeCode,
                "btn_submit" to ""
            ),
            Operation.RECHARGE
        )
    }

    fun transfer(
        mountToTransfer: String,
        accountToTransfer: String,
        password: String,
        cookies: MutableMap<String, String>
    ) {
        getCsrf(Operation.TRANSFER)
        postAction(
            cookies,
            mapOf(
                "csrf" to csrf!!,
                "transfer" to mountToTransfer,
                "password_user" to password,
                "id_cuenta" to accountToTransfer,
                "action" to "checkdata"
            ),
            Operation.TRANSFER
        )
    }

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        cookies: MutableMap<String, String>
    ) {
        getCsrf(Operation.CHANGE_PASSWORD)
        postAction(
            cookies,
            mapOf(
                "csrf" to csrf!!,
                "old_password" to oldPassword,
                "new_password" to newPassword,
                "repeat_new_password" to newPassword,
                "btn_submit" to ""
            ),
            Operation.CHANGE_PASSWORD
        )
    }

    fun changeEmailPassword(
        oldPassword: String,
        newPassword: String,
        cookies: MutableMap<String, String>
    ) {
        getCsrf(Operation.CHANGE_EMAIL_PASSWORD)
        postAction(
            cookies,
            mapOf(
                "csrf" to csrf!!,
                "old_password" to oldPassword,
                "new_password" to newPassword,
                "repeat_new_password" to newPassword,
                "btn_submit" to ""
            ),
            Operation.CHANGE_EMAIL_PASSWORD
        )
    }

    fun getLasts(
        action: Operation,
        large: Int,
        cookies: MutableMap<String, String>
    ): List<Any> {
        val cal = Calendar.getInstance()
        cal.time = Date()
        var year = cal.get(Calendar.YEAR)
        var month = cal.get(Calendar.MONTH) + 1
        val lasts = mutableListOf<Any>()

        var actions = when (action) {
            Operation.CONNECTIONS -> {
                getConnections(year, month, cookies)
            } Operation.RECHARGES -> {
                getRecharges(year, month, cookies)
            } Operation.TRANSFERS -> {
                getTransfers(year, month, cookies)
            } Operation.QUOTES_FUNDS -> {
                getQuotesFund(year, month, cookies)
            } else -> {
                listOf<Any>()
            }
        }
        if (actions.isNotEmpty()) { lasts.addAll(actions) }

        while (lasts.size < large) {
            if (month == 1) {
                month = 12
                year -= 1
            } else { month -= 1 }
            actions = when (action) {
                Operation.CONNECTIONS -> {
                    getConnections(year, month, cookies)
                } Operation.RECHARGES -> {
                    getRecharges(year, month, cookies)
                } Operation.TRANSFERS -> {
                    getTransfers(year, month, cookies)
                } Operation.QUOTES_FUNDS -> {
                    getQuotesFund(year, month, cookies)
                } else -> {
                    listOf()
                }
            }
            if (actions.isNotEmpty()) { lasts.addAll(actions) }
        }
        return lasts.subList(0, large)
    }

    fun getConnections(
        year: Int,
        month: Int,
        cookies: MutableMap<String, String>
    ): List<Connection> {
        val connections = mutableListOf<Connection>()
        getAction(year, month, Operation.CONNECTIONS, cookies)?.let {
            for (tr in it) {
                val tds = tr.select("td")
                connections.add(
                    Connection(
                        tds[0].text(),
                        tds[1].text(),
                        tds[2].text(),
                        tds[3].text(),
                        tds[4].text(),
                        tds[5].text()
                    )
                )
            }
        }
        return connections
    }

    fun getRecharges(
        year: Int,
        month: Int,
        cookies: MutableMap<String, String>
    ): List<Recharge> {
        val recharges = mutableListOf<Recharge>()
        getAction(year, month, Operation.RECHARGES, cookies)?.let {
            for (tr in it) {
                val tds = tr.select("td")
                recharges.add(
                    Recharge(
                        tds[0].text(),
                        tds[1].text(),
                        tds[2].text(),
                        tds[3].text()
                    )
                )
            }
        }
        return recharges
    }

    fun getTransfers(
        year: Int,
        month: Int,
        cookies: MutableMap<String, String>
    ): List<Transfer> {
        val transfers = mutableListOf<Transfer>()
        getAction(year, month, Operation.TRANSFERS, cookies)?.let {
            for (tr in it) {
                val tds = tr.select("td")
                transfers.add(
                    Transfer(
                        tds[0].text(),
                        tds[1].text(),
                        tds[2].text()
                    )
                )
            }
        }
        return transfers
    }

    fun getQuotesFund(
        year: Int,
        month: Int,
        cookies: MutableMap<String, String>
    ): List<QuoteFund> {
        val quotesFund = mutableListOf<QuoteFund>()
        getAction(year, month, Operation.QUOTES_FUNDS, cookies)?.let {
            for (tr in it) {
                val tds = tr.select("td")
                quotesFund.add(
                    QuoteFund(
                        tds[0].text(),
                        tds[1].text(),
                        tds[2].text(),
                        tds[3].text(),
                        tds[4].text()
                    )
                )
            }
        }
        return quotesFund
    }

    private fun getAction(
        year: Int,
        month: Int,
        action: Operation,
        cookies: MutableMap<String, String>
    ): Elements? {
        this.cookies = cookies

        val listsTypes = mapOf(
            Operation.CONNECTIONS to mapOf(
                "base" to "service_detail",
                "summary" to "service_detail_summary",
                "list" to "service_detail_list"
            ),
            Operation.RECHARGES to mapOf(
                "base" to "recharge_detail",
                "summary" to "recharge_detail_summary",
                "list" to "recharge_detail_list"
            ),
            Operation.TRANSFERS to mapOf(
                "base" to "transfer_detail",
                "summary" to "transfer_detail_summary",
                "list" to "transfer_detail_list"
            ),
            Operation.QUOTES_FUNDS to mapOf(
                "base" to "nautahogarpaid_detail",
                "summary" to "nautahogarpaid_detail_summary",
                "list" to "nautahogarpaid_detail_list"
            )
        )

        val yearMonth = "${year}-${"%02d".format(month)}"
        getCsrf(action)
        val url = when (action) {
            Operation.CONNECTIONS -> {
                urls[Operation.CONNECTIONS_LIST]
            } Operation.RECHARGES -> {
                urls[Operation.RECHARGES_LIST]
            } Operation.TRANSFERS -> {
                urls[Operation.TRANSFERS_LIST]
            } Operation.QUOTES_FUNDS -> {
                urls[Operation.QUOTES_FUNDS_LIST]
            } else -> {
                null
            }
        }
        val soup = connect(
            "${urlBase}${url!!}",
            mapOf(
                "csrf" to csrf!!,
                "year_month" to yearMonth,
                "list_type" to listsTypes[action]!!["list"]!!
            )
        ).post()
        val table = soup.select("table[class='striped bordered highlight responsive-table']")
            .first()
        val trs = table?.select("tr")
        trs?.removeAt(0)
        return trs
    }

    private fun postAction(
        cookies: MutableMap<String, String>,
        data: Map<String, String>,
        action: Operation
    ): Document? {
        this.cookies = cookies
        val soup = urls[action]?.let {
            connect("${urlBase}${it}", data).post()
        }
        return soup
    }

    private fun updateAttrs(soup: Document) {

        userName = getAttr(Attribute.USER_NAME, soup)
        blockingDate = getAttr(Attribute.BLOCKING_DATE, soup)
        dateOfElimination = getAttr(Attribute.DATE_OF_ELIMINATION, soup)
        accountType = getAttr(Attribute.ACCOUNT_TYPE, soup)
        serviceType = getAttr(Attribute.SERVICE_TYPE, soup)
        credit = getAttr(Attribute.CREDIT, soup)
        time = getAttr(Attribute.TIME, soup)
        mailAccount = getAttr(Attribute.MAIL_ACCOUNT, soup)

        offer = getAttr(Attribute.OFFER, soup)
        monthlyFee = getAttr(Attribute.MONTHLY_FEE, soup)
        downloadSpeeds = getAttr(Attribute.DOWNLOAD_SPEEDS, soup)
        uploadSpeeds = getAttr(Attribute.UPLOAD_SPEEDS, soup)
        phone = getAttr(Attribute.PHONE, soup)
        linkIdentifiers = getAttr(Attribute.LINK_IDENTIFIERS, soup)
        linkStatus = getAttr(Attribute.LINK_STATUS, soup)
        activationDate = getAttr(Attribute.ACTIVATION_DATE, soup)
        blockingDateHome = getAttr(Attribute.BLOCKING_DATE_HOME, soup)
        dateOfEliminationHome = getAttr(Attribute.DATE_OF_ELIMINATION_HOME, soup)
        quoteFund = getAttr(Attribute.QUOTA_FUND, soup)
        voucher = getAttr(Attribute.VOUCHER, soup)
        debt = getAttr(Attribute.DEBT, soup)

    }

    private fun getAttr(attr: Attribute, soup: Document): String? {
        val index: Int = if (attr == Attribute.BLOCKING_DATE_HOME || attr == Attribute.DATE_OF_ELIMINATION_HOME) {
            1
        } else {
            0
        }
        var count = 0
        for (div in soup.select("div.card-panel").first()?.select("div.m6")!!) {
            if (div.select("h5").first()?.text()?.trim()?.lowercase() == attrs[attr]) {
                if (index == 1 && count == 0) {
                    count += 1
                    continue
                }
                return div.select("p").first()?.text()
            }
        }
        return null
    }
}
