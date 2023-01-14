# KotLibSuitETECSA

## Una libreria escrita en Kotlin para SuitETECSA

KotLibSuitETECSA fue creada con el objetivo de ofrecer una API que interactúe con los servicios ofrecidos por [ETECSA](https://www.etecsa.cu/), para facilitar el desarrollo de aplicaciones Kotlin dedicadas a la gestión de estos mediante los portales [de usuario](https://www.portal.nauta.cu/) y [cautivo](https://secure.etecsa.net:8443/) de nauta, ahorrándoles tiempo, esfuerzos, neuronas y código a los desarrolladores.

KotLibSuitETECSA está aún en fase de desarrollo activa, por lo que aún no implementa algunas funciones
necesarias para la gestión de cuentas asociadas al servicio Nauta Hogar. Se me ha hecho difícil la implementación de dichas funciones, ya que no poseo este servicio.

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

## Funciones y propiedades de UserPortalSession

<details>
    <summary>Nauta</summary>
    <table>
        <thead>
            <tr>
                <td>Función</td>
                <td>Descripción</td>
            </tr>
        </thead>
        <tr>
            <td>init</td>
            <td>Inicializa la sesión donde se guardan las cookies y datos</td>
        </tr>
        <tr>
            <td>login</td>
            <td>Loguea al usuario en el portal y carga la información de la cuenta</td>
        </tr>
        <tr>
            <td>loadUserInfo</td>
            <td>Recupera y devuelve la información de la cuenta logueada</td>
        </tr>
        <tr>
            <td>recharge</td>
            <td>Recarga la cuenta logueada</td>
        </tr>
        <tr>
            <td>transfer</td>
            <td>Transfiere saldo a otra cuenta nauta</td>
        </tr>
        <tr>
            <td>changePassword</td>
            <td>Cambia la contraseña de la cuenta logueada</td>
        </tr>
        <tr>
            <td>changeEmailPassword</td>
            <td>Cambia la contraseña de la cuenta de correo asociada a la cuenta logueada</td>
        </tr>
        <tr>
            <td>getLasts</td>
            <td>Devuelve las últimas <b>large</b> <b>action</b> realizadas, donde <b>large</b> es la cantidad Ex: 5 y <b>action</b> las operaciones realizadas Ex: <b>Operation.CONNECTIONS</b> (las <b>action</b> disponibles son: <b>Operation.CONNECTIONS</b>, <b>Operation.RECHARGES</b>, <b>Operation.TRANSFERS</b> y <b>Operation.QUOTES_FUNDS</b>, esta última solo para nauta hogar)</td>
        </tr>
        <tr>
            <td>getConnections</td>
            <td>Devuelve las conexiones realizadas en el mes especificado incluyendo el año (<b>año-mes</b>: 2022-03)</td>
        </tr>
        <tr>
            <td>getRecharges</td>
            <td>Devuelve las recargas realizadas en el mes especificado incluyendo el año (<b>año-mes</b>: 2022-03)</td>
        </tr>
        <tr>
            <td>getTransfers</td>
            <td>Devuelve las transferencias realizadas en el mes especificado incluyendo el año (<b>año-mes</b>: 2022-03)</td>
        </tr>
    </table>
</details>

<details>
    <summary>Nauta Hogar</summary>
    <table>
        <thead>
            <tr>
                <td>Función</td>
                <td>Descripción</td>
            </tr>
        </thead>
        <tr>
            <td>transferToQuote</td>
            <td>Transfiere saldo a la cuota de nauta hogar (<b>aún sin implementar</b>)</td>
        </tr>
        <tr>
            <td>payToDebtWithCredit</td>
            <td>Paga deuda de nauta hogar con saldo (<b>aún sin implementar</b>)</td>
        </tr>
        <tr>
            <td>payToDebtWithQuoteFund</td>
            <td>Paga deuda de nauta hogar con fondo de cuota (<b>aún sin implementar</b>)</td>
        </tr>
        <tr>
            <td>getQuotesFund</td>
            <td>Devuelve los fondos de cuota realizados en el mes especificado incluyendo el año (<b>año-mes</b>: 2022-03)</td>
        </tr>
    </table>
</details>

### Propiedades

<details>
    <summary>Nauta</summary>
    <table>
        <thead>
            <tr>
                <td>Propiedad</td>
                <td>Dato devuelto</td>
            </tr>
        </thead>
        <tr>
            <td>userName</td>
            <td>Nombre de usuario de la cuenta logueada.</td>
        </tr>
        <tr>
            <td>blockingDate</td>
            <td>Fecha de bloqueo.</td>
        </tr>
        <tr>
            <td>dateOfElimination</td>
            <td>Fecha de eliminación.</td>
        </tr>
        <tr>
            <td>accountType</td>
            <td>Tipo de cuenta.</td>
        </tr>
        <tr>
            <td>serviceType</td>
            <td>Tipo de servicio.</td>
        </tr>
        <tr>
            <td>credit</td>
            <td>Saldo.</td>
        </tr>
        <tr>
            <td>time</td>
            <td>Tiempo disponible.</td>
        </tr>
        <tr>
            <td>mailAccount</td>
            <td>Cuenta de correo asociada.</td>
        </tr>
    </table>
</details>

<details>
    <summary>Nauta Hogar</summary>
    <table>
        <thead>
            <tr>
                <td>Propiedad</td>
                <td>Dato devuelto</td>
            </tr>
        </thead>
        <tr>
            <td>offer</td>
            <td>Oferta</td>
        </tr>
        <tr>
            <td>monthlyFee</td>
            <td>Cuota mensual</td>
        </tr>
        <tr>
            <td>downloadSpeeds</td>
            <td>Velocidad de bajada</td>
        </tr>
        <tr>
            <td>uploadSpeeds</td>
            <td>Velocidad de subida</td>
        </tr>
        <tr>
            <td>phone</td>
            <td>Teléfono</td>
        </tr>
        <tr>
            <td>linkIdentifiers</td>
            <td>Identificador del enlace</td>
        </tr>
        <tr>
            <td>linkStatus</td>
            <td>Estado del enlace</td>
        </tr>
        <tr>
            <td>activationDate</td>
            <td>Fecha de activación</td>
        </tr>
        <tr>
            <td>blockingDateHome</td>
            <td>Fecha de bloqueo</td>
        </tr>
        <tr>
            <td>dateOfEliminationHome</td>
            <td>Fecha de eliminación</td>
        </tr>
        <tr>
            <td>quoteFund</td>
            <td>Fondo de cuota</td>
        </tr>
        <tr>
            <td>voucher</td>
            <td>Bono</td>
        </tr>
        <tr>
            <td>debt</td>
            <td>Deuda</td>
        </tr>
    </table>
</details>

__Nota__: Los `métodos` y `propiedades` disponibles para `Nauta` también lo están para `Nauta Hogar`.

## Usando NautaClient

```kotlin
fun main() {
    val nautaCli = NautaSession()
    nautaCli.init()
    nautaCli.login(
        "user@nauta.com.cu",
        "password"
    )
    println(nautaCli.getUserTime(
        "user@nauta.com.cu"
    ))
    nautaCli.logout(
        "user@nauta.com.cu"
    )
}
```

## Funciones y propiedades de UserPortalClient

### Funciones

* init: Inicializa la session donde se guardan las cookies y datos
* login: Loguea al usuario en el portal
* logout: Cierra la sesión abierta
* getUserTime: Devuelve el tiempo disponible en la cuenta

## Contribuir

__IMPORTANTE__: KotLibSuitETESA necesita compatibilidad con nauta hogar.

Todas las contribuciones son bienvenidas. Puedes ayudar trabajando en uno de los issues existentes. Clona el repo, crea una rama para el issue que estés trabajando y cuando estés listo crea un Pull Request.

También puedes contribuir difundiendo esta herramienta entre tus amigos y en tus redes. Mientras más grande sea la comunidad más sólido será el proyecto.

Si te gusta el proyecto dale una estrella para que otros lo encuentren más fácilmente.
