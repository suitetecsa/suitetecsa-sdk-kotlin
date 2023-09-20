package cu.suitetecsa.sdk.nauta.core

sealed class PortalManager(val baseUrl: String) {
    object Connect : PortalManager("https://$connectDomain:8443")
    object User : PortalManager("https://www.portal.nauta.cu")
}