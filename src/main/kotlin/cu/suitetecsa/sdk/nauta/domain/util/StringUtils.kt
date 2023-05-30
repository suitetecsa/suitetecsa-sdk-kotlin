package cu.suitetecsa.sdk.nauta.domain.util

import java.security.SecureRandom

/**
 * Genera una contraseña aleatoria de alta calidad.
 *
 * @param length La longitud de la contraseña a generar.
 * @param includeLowercase Indica si se deben incluir letras minúsculas en la contraseña.
 * @param includeUppercase Indica si se deben incluir letras mayúsculas en la contraseña.
 * @param includeNumbers Indica si se deben incluir números en la contraseña.
 * @param includeSymbols Indica si se deben incluir símbolos en la contraseña.
 *
 * @return La contraseña generada.
 */
fun generatePassword(
    length: Int,
    includeLowercase: Boolean = true,
    includeUppercase: Boolean = true,
    includeNumbers: Boolean = true,
    includeSymbols: Boolean = true
): String {
    val random = SecureRandom()
    val characters = mutableListOf<Char>()

    // Agregar caracteres según las opciones de configuración
    if (includeLowercase) characters.addAll(('a'..'z'))
    if (includeUppercase) characters.addAll(('A'..'Z'))
    if (includeNumbers)characters.addAll(('0'..'9'))
    if (includeSymbols) characters.addAll("!@#\$%^&*()_-+={}[]\\|:;\"'<>,.?/".toList())

    // Generar la contraseña aleatoria
    val password = buildString {
        repeat(length) {
            append(characters[random.nextInt(characters.size)])
        }
    }

    return password
}