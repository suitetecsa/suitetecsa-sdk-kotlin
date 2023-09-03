package cu.suitetecsa.sdk.promos.domain

/**
 * Clase de datos que representa una promoción.
 *
 * @param svgImage La URL de la imagen SVG de la promoción.
 * @param jpgImage La URL de la imagen JPG de la promoción.
 * @param articleLink El enlace al artículo de la promoción.
 */
data class Promotion(
    val svgImage: String,
    val jpgImage: String,
    val articleLink: String
)
