package cu.suitetecsa.sdk.nauta.data.repository

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import cu.suitetecsa.sdk.nauta.data.model.HttpResponse

class FuelNautaService : NautaService {
    private var uCookies: Collection<String> = listOf()
    private var cCookies: Collection<String> = listOf()

    override val userCookies: MutableMap<String, String>
        get() = mutableMapOf()
    override val connectCookies: MutableMap<String, String>
        get() = mutableMapOf()

    override var csrf: String? = null
    override var userName: String? = null
    override var csrfHw: String? = null
    override var wlanUserIp: String? = null
    override var attributeUUID: String? = null
    override var actionLogin: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var isNautaHome: Boolean = false

    private fun getCookiesWhenPortal(url: String): Collection<String> {
        return if (url.startsWith("https://www.portal.nauta.cu")) uCookies else cCookies
    }

    private fun saveCookiesWhenPortal(url: String, response: Response) {
        if (url.startsWith("https://www.portal.nauta.cu")) {
            uCookies = response.headers["Set-Cookie"]
        } else {
            cCookies = response.headers["Set-Cookie"]
        }
    }

    override fun get(url: String): String {
        val cookies = getCookiesWhenPortal(url)
        val request = if (cookies.isEmpty()) url.httpGet() else url.httpGet().header("Cookie", cookies)
        val (_, response, result) = request.responseString()
        if (cookies.isEmpty()) saveCookiesWhenPortal(url, response)
        return handleResult(response, result)
    }

    override fun get(url: String, verifyResponse: (HttpResponse) -> Unit): String {
        val cookies = getCookiesWhenPortal(url)
        val request = if (cookies.isEmpty()) url.httpGet() else url.httpGet().header("Cookie", cookies)
        val (_, response, result) = request.responseString()
        verifyResponse(response.toNautaHttpResponseText(result))
        if (cookies.isEmpty()) saveCookiesWhenPortal(url, response)
        return handleResult(response, result)
    }

    override fun post(url: String, data: Map<String, String>?): String {
        val cookies = getCookiesWhenPortal(url)
        val (_, response, result) = url.httpPost().body(Gson().toJson(data)).responseString()
        return handleResult(response, result)
    }

    override fun post(url: String, data: Map<String, String>?, verifyResponse: (HttpResponse) -> Unit): String {
        val cookies = getCookiesWhenPortal(url)
        val request = if (cookies.isEmpty()) url.httpPost().body(Gson().toJson(data)) else url.httpPost()
            .body(Gson().toJson(data)).header("Cookie", cookies)
        val (_, response, result) = request.responseString()
        verifyResponse(response.toNautaHttpResponseText(result))
        return handleResult(response, result)
    }

    private fun handleResult(
        response: Response,
        result: Result<String, FuelError>
    ): String {
        return when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                println("Error: ${ex.message}")
                ""
            }

            is Result.Success -> {
                val data = result.get()
                println("Response: $data")
                data
            }
        }
    }
}

private fun Response.toNautaHttpResponseText(result: Result<String, FuelError>): HttpResponse {
    return HttpResponse(
        this.statusCode,
        this.responseMessage,
        result.get(),
        null,
        this.url
    )
}
