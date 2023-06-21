package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.core.Portal
import cu.suitetecsa.sdk.nauta.core.exceptions.OperationException
import cu.suitetecsa.sdk.nauta.core.throwExceptionOnFailure
import cu.suitetecsa.sdk.nauta.framework.model.HttpResponse
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import org.jsoup.Connection

class DefaultNautaSession : NautaSession {
    override val cookies = mutableMapOf<String, String>()
    private var portalManager: Portal = Portal.CONNECT

    override fun setPortalManager(portalManager: Portal) {
        this.portalManager = portalManager
    }

    override fun get(
        url: String,
        params: Map<String, String>?,
        ignoreContentType: Boolean,
        timeout: Int?
    ): ResultType<HttpResponse> {
        return try {
            val response =
                connection(portalManager, url, params, ignoreContentType, timeout).method(Connection.Method.GET).execute()
            response.throwExceptionOnFailure(OperationException::class.java, "Fail")
            ResultType.Success(
                HttpResponse(
                    statusCode = response.statusCode(),
                    statusMassage = response.statusMessage(),
                    content = response.bodyAsBytes(),
                    cookies = response.cookies()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResultType.Error(e)
        }
    }

    override fun post(url: String, data: Map<String, String>): ResultType<HttpResponse> {
        return try {
            val response = connection(portalManager, url, data).method(Connection.Method.POST).execute()
            response.throwExceptionOnFailure(OperationException::class.java, "Fail")
            ResultType.Success(
                HttpResponse(
                    statusCode = response.statusCode(),
                    statusMassage = response.statusMessage(),
                    content = response.bodyAsBytes(),
                    cookies = response.cookies()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResultType.Error(e)
        }
    }
}