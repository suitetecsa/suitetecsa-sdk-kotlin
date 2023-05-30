package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.framework.model.DataSession
import cu.suitetecsa.sdk.nauta.framework.model.ResultType

interface ConnectPortalCommunicator {
    var dataSession: DataSession
    val remainingTime: ResultType<String?>

    fun checkConnection(): ResultType<String?>
    fun getLoginPage(url: String, data: Map<String, String>): ResultType<String?>
    fun connect(url: String, data: Map<String, String>): ResultType<String?>

    fun getNautaConnectInformation(url: String, data: Map<String, String>): ResultType<String?>
    fun disconnect(): ResultType<String?>
}