package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.core.Action
import cu.suitetecsa.sdk.nauta.core.Portal
import cu.suitetecsa.sdk.nauta.core.connectPortal
import cu.suitetecsa.sdk.nauta.core.makeUrl
import cu.suitetecsa.sdk.nauta.framework.model.DataSession
import cu.suitetecsa.sdk.nauta.framework.model.ResultType

class JsoupConnectPortalCommunicator(private val nautaSession: NautaSession) : ConnectPortalCommunicator {

    init {
        nautaSession.setPortalManager(Portal.CONNECT)
    }

    private var _dataSession: DataSession = DataSession("", "", "", "")
    override var dataSession: DataSession
        get() = _dataSession
        set(value) {
            _dataSession = value
        }
    override val remainingTime: ResultType<String?>
        get() {
            val data = mapOf(
                "op" to "getLeftTime",
                "ATTRIBUTE_UUID" to _dataSession.attributeUUID,
                "CSRFHW" to _dataSession.csrfHw,
                "wlanuserip" to _dataSession.wlanUserIp,
                "username" to _dataSession.username
            )

            return when (val response = nautaSession.post(makeUrl(Action.LOAD_USER_INFORMATION, connectPortal), data)) {
                is ResultType.Error -> ResultType.Error(response.throwable)
                is ResultType.Success -> ResultType.Success(response.result.text)
            }
        }

    override fun checkConnection(): ResultType<String?> {
        return when (val response = nautaSession.get(makeUrl(Action.CHECK_CONNECTION, connectPortal))) {
            is ResultType.Error -> ResultType.Error(response.throwable)
            is ResultType.Success -> ResultType.Success(response.result.text)
        }
    }

    override fun getLoginPage(url: String, data: Map<String, String>): ResultType<String?> {
        return when (val response = nautaSession.post(url, data)) {
            is ResultType.Error -> ResultType.Error(response.throwable)
            is ResultType.Success -> {
                response.result.cookies?.forEach { (key, value) ->
                    nautaSession.cookies[key] = value
                }
                ResultType.Success(response.result.text)
            }
        }
    }

    override fun connect(url: String, data: Map<String, String>): ResultType<String?> {
        return when (val response = nautaSession.post(url, data)) {
            is ResultType.Error -> ResultType.Error(response.throwable)
            is ResultType.Success -> ResultType.Success(response.result.text)
        }
    }

    override fun getNautaConnectInformation(url: String, data: Map<String, String>): ResultType<String?> {
        return when (val response = nautaSession.post(url, data)) {
            is ResultType.Error -> ResultType.Error(response.throwable)
            is ResultType.Success -> ResultType.Success(response.result.text)
        }
    }

    override fun disconnect(): ResultType<String?> {
        val data = mapOf(
            "username" to _dataSession.username,
            "wlanuserip" to _dataSession.wlanUserIp,
            "CSRFHW" to _dataSession.csrfHw,
            "ATTRIBUTE_UUID" to dataSession.attributeUUID
        )
        return when (val response = nautaSession.get(makeUrl(Action.LOGOUT, connectPortal), data)) {
            is ResultType.Error -> ResultType.Error(response.throwable)
            is ResultType.Success -> {
                ResultType.Success(response.result.text)
            }
        }
    }
}