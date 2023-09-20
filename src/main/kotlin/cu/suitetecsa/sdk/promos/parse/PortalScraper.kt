package cu.suitetecsa.sdk.promos.parse

import cu.suitetecsa.sdk.promos.domain.Promotion

/**
 * Esta interfaz define un scraper de portal que proporciona un método para analizar promociones a partir de HTML y
 * devolver una lista de objetos `Promotion`.
 */
interface PortalScraper {
    /**
     * Analiza las promociones a partir del HTML proporcionado y devuelve una lista de objetos `Promotion`.
     *
     * @param html El HTML que contiene las promociones a analizar.
     * @return Una lista de objetos `Promotion` que representan las promociones analizadas.
     */
    fun parsePromotions(html: String): List<Promotion>
}
