
### Usando UserPortalSession
```kotlin
fun main() {
    val userPortalCli = UserPortalSession()
    userPortalCli.init()
    downloadCaptcha("captcha_image.png", userPortalCli.getCaptchaAsBytes())
    val captchaCode: String
    println("Introduzca el código de la imagen captcha: ")
    val keyMap = Scanner(System.`in`)
    captchaCode = keyMap.nextLine()
    userPortalCli.login(
        "user.name@nauta.com.cu",
        "password",
        captchaCode,
        userPortalCli.cookies
    )
    println(userPortalCli.userName)
    println(userPortalCli.credit)
    println(userPortalCli.accountType)

    val lastsConnections = userPortalCli.getLasts(
        Operation.CONNECTIONS,
        5,
        userPortalCli.cookies
    ) as List<Connection>
    for (connection in lastsConnections) {
        println(connection.startSession)
        println(connection.endSession)
        println(connection.import_)
    }
}

fun downloadCaptcha(path: String, captchaImage: ByteArray) {
    var out: ByteArrayOutputStream? = null
    BufferedInputStream(ByteArrayInputStream(captchaImage)).use { `in` ->
        out = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        var n: Int
        while (-1 != `in`.read(buf).also { n = it }) {
            out!!.write(buf, 0, n)
        }
        out!!.close()
    }
    val response = out!!.toByteArray()
    FileOutputStream(path).use { fos -> fos.write(response) }
}
```