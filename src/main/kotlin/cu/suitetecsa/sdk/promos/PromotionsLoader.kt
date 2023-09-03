package cu.suitetecsa.sdk.promos

import cu.suitetecsa.sdk.nauta.domain.model.ResultType
import cu.suitetecsa.sdk.network.Action
import cu.suitetecsa.sdk.network.JsoupPortalCommunicator
import cu.suitetecsa.sdk.network.PortalCommunicator
import cu.suitetecsa.sdk.promos.domain.Promotion
import cu.suitetecsa.sdk.promos.parse.JsoupPortalScraper
import cu.suitetecsa.sdk.promos.parse.PortalScraper

/**
 * Clase que carga las promociones utilizando un comunicador de portal y un scraper de portal.
 *
 * @param communicator El comunicador de portal utilizado para realizar la acción de carga.
 * @param scraper El scraper de portal utilizado para analizar las promociones.
 */
class PromotionsLoader(private val communicator: PortalCommunicator, private val scraper: PortalScraper) {
    private val action = object : Action {
        override fun url(): String = "https://www.etecsa.cu/"
    }

    /**
     * Carga las promociones utilizando el comunicador de portal y el scraper de portal.
     *
     * @return Objeto `ResultType` que encapsula el resultado de la carga de promociones.
     */
    fun loadPromotions(): ResultType<List<Promotion>> {
        return communicator.performAction(action) { response ->
            scraper.parsePromotions(response.text ?: "")
        }
    }

    /**
     * Builder para construir una instancia de `PromotionsLoader`.
     */
    class Builder {
        private var communicator: PortalCommunicator? = null
        private var scraper: PortalScraper? = null

        /**
         * Establece el comunicador de portal utilizado para la carga de promociones.
         *
         * @param communicator El comunicador de portal a utilizar.
         * @return El builder actualizado.
         */
        fun communicator(communicator: PortalCommunicator): Builder {
            this.communicator = communicator
            return this
        }

        /**
         * Establece el scraper de portal utilizado para la carga de promociones.
         *
         * @param scraper El scraper de portal a utilizar.
         * @return El builder actualizado.
         */
        fun scraper(scraper: PortalScraper): Builder {
            this.scraper = scraper
            return this
        }

        /**
         * Construye una instancia de `PromotionsLoader` utilizando el comunicador de portal y el scraper de portal especificados.
         *
         * @return La instancia de `PromotionsLoader` creada.
         */
        fun build(): PromotionsLoader {
            return PromotionsLoader(
                communicator ?: JsoupPortalCommunicator.Builder().build(),
                scraper ?: JsoupPortalScraper()
            )
        }
    }
}
