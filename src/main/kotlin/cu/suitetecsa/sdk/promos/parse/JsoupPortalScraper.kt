package cu.suitetecsa.sdk.promos.parse

import cu.suitetecsa.sdk.promos.domain.Promotion
import org.jsoup.Jsoup

/**
 * Esta clase implementa el scraper de portal utilizando Jsoup.
 */
class JsoupPortalScraper : PortalScraper {

    /**
     * Analiza el enlace de la promoción a partir del texto proporcionado.
     *
     * @param text El texto que contiene el enlace de la promoción.
     * @return El enlace de la promoción analizado.
     */
    private fun parsePromotionLink(text: String?): String? {
        val regex = "url\\('(.+)'\\);".toRegex()
        val matchResult = text?.let { regex.find(it) }
        return matchResult?.groupValues?.get(1)
    }

    /**
     * Analiza las promociones a partir del HTML proporcionado y devuelve una lista de objetos `Promotion`.
     *
     * @param html El HTML que contiene las promociones a analizar.
     * @return Una lista de objetos `Promotion` que representan las promociones analizadas.
     */
    override fun parsePromotions(html: String): List<Promotion> {
        val promotionList = mutableListOf<Promotion>()
        val htmlParsed = Jsoup.parse(html)
        val carousel = htmlParsed.select("div.carousel-inner")
            .select("div.carousel-item")
        carousel.forEach { item ->
            val jpgImage = parsePromotionLink(item.selectFirst("div[style]")?.attr("style"))
            val myContentDiv = item.selectFirst("div.mipromocion")?.selectFirst("div.mipromocion-contenido")
            val svgImage = myContentDiv?.selectFirst("img")?.attr("src")
            val articleLink = myContentDiv?.selectFirst("a")?.attr("href")
            promotionList.add(Promotion(jpgImage ?: "", svgImage ?: "", articleLink ?: ""))
        }
        return promotionList
    }
}
