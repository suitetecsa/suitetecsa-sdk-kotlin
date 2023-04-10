# suitetecsa-sdk-kotlin

[![](https://jitpack.io/v/suitetecsa/suitetecsa-sdk-kotlin.svg)](https://jitpack.io/#suitetecsa/suitetecsa-sdk-kotlin)

`suitetecs-sdk_kotlin` es una herramienta diseñada para  interactuar con los servicios de [ETECSA](https://www.etecsa.cu/). La librería utiliza técnicas de scrapping para acceder a los portales de [acceso a internet ](https://secure.etecsa.net:8443/)y de [usuario](https://www.portal.nauta.cu/) de Nauta. Implementa funciones para todas las operaciones disponibles en ambos portales, y ofrece soporte para Nauta Hogar.

Todas las funcionalidades están disponibles desde una única clase, `NautaClient`, lo que permite interactuar con ambos portales a la vez, permitiendo obtener datos de manera rápida y eficiente, ahorrando tiempo y esfuerzos a la hora de desarrollar aplicaciones que busquen gestionar los servicios de [ETECSA](https://www.etecsa.cu/). Además, incluye funcionalidades útiles como la generación de contraseñas y la compartición de sesión.

Se han seguido los principios SOLID en su desarrollo, lo que garantiza que es fácil de entender, modificar y mantener. La mayoría de la información devuelta por las funciones son objetos, lo que simplifica el trabajo de las aplicaciones que lo utilizan.

Al ser un proyecto open-source, se valoran y se reciben contribuciones de la comunidad de desarrolladores/as.

## Funciones implementadas

- [x] [Secure Etecsa](https://secure.etecsa.net:8443/)
  
  - [x] Iniciar sesión.
  - [x] Cerrar sesión.
  - [x] Obtener el tiempo disponible en la cuenta.
  - [x] Obtener la información de la cuenta.

- [x] [Portal de Usuario](https://www.portal.nauta.cu/)
  
  - [x] Iniciar sesión.
  
  - [x] Obtener información de la cuenta.
  
  - [x] Recargar la cuenta.
  
  - [x] Transferir saldo a otra cuenta nauta.
  
  - [x] Transferir saldo para pago de cuota (`solo para cuentas Nauta Hogar`).
  
  - [x] Cambiar la contraseña de la cuenta de acceso.
  
  - [x] Cambiar la contraseña de la cuenta de correo asociada.
  
  - [x] Obtener las conexiones realizadas en el periódo `año-mes` especificado.
  
  - [x] Obtener las recargas realizadas en el periódo `año-mes` especificado.
  
  - [x] Obtener las transferencias realizadas en el periódo `año-mes` especificado.
  
  - [x] Obtener los pagos de cuotas realizados en el periódo `año-mes` especificado (`solo para cuentas Nauta Hogar`).

# Uso

Importa `suitetecsa-sdk-kotlin` en tu proyecto

```groovy
implementation("com.github.suitetecsa:suitetecsa-sdk-kotlin:0.1.5")
```

Importal `NautaSession`, `JsoupNautaProvider` y `NautaClient`

```kotlin
import cu.suitetecsa.sdk.nauta.data.repository.JSoupNautaProvider
import cu.suitetecsa.sdk.nauta.data.repository.NautaSessionProvider
import cu.suitetecsa.sdk.nauta.domain.service.NautaClient
```

Crea las instancias necesarias o inyectalas

```kotlin
    val session = NautaSessionProvider()
    val provider = JSoupNautaProvider(session)
    val client = NautaClient(provider)
```

Establece las credenciales que usaras para iniciar sesion

```kotlin
    client.setCredentials("user.name@nauta.com.cu", "somePassword")
```

Conectate a internet desde la wifi o Nauta Hogar

```kotlin
    // Para hacer login en el portal cautivo
    client.connect()
    // Obtener el tiempo restante
    val remainingTime = client.remainingTime
```

Interactua con el portal de usuario

```kotlin
    // Para hacer login en el portal de usuario
    downloadCaptcha("captchaImage.png", client.captchaImage)
    print("Introduzca el código de la imagen captcha: ")
    val keyMap = Scanner(System.`in`)
    val captchaCode = keyMap.nextLine()
    val user: NautaUser = client.login(captchaCode)
```

Otras funciones

```kotlin
    // Funciones del portal cautivo
    client.connectInformation
    client.disconnect()

    // Funciones del portal de usuario
    client.userInformation
    client.toUpBalance("rechargeCode")
    client.transferBalance(25f, "destinationAccount")
    client.payNautaHome(300f)
    client.getConnections(2023, 3)
    client.getRecharges(2023, 3)
    client.getTransfers(2023, 3)
    client.getQuotesPaid(2023, 3)
```

# Contribución

¡Gracias por tu interés en colaborar con nuestro proyecto! Nos encanta recibir contribuciones de la comunidad y valoramos mucho tu tiempo y esfuerzo.

## Cómo contribuir

Si estás interesado en contribuir, por favor sigue los siguientes pasos:

1. Revisa las issues abiertas para ver si hay alguna tarea en la que puedas ayudar.
2. Si no encuentras ninguna issue que te interese, por favor abre una nueva issue explicando el problema o la funcionalidad que te gustaría implementar. Asegúrate de incluir toda la información necesaria para que otros puedan entender el problema o la funcionalidad que estás proponiendo.
3. Si ya tienes una issue asignada o si has decidido trabajar en una tarea existente, por favor crea un fork del repositorio y trabaja en una nueva rama (`git checkout -b nombre-de-mi-rama`).
4. Cuando hayas terminado de trabajar en la tarea, crea un pull request explicando los cambios que has realizado y asegurándote de que el código cumple con nuestras directrices de estilo y calidad.
5. Espera a que uno de nuestros colaboradores revise el pull request y lo apruebe o sugiera cambios adicionales.

## Directrices de contribución

Por favor, asegúrate de seguir nuestras directrices de contribución para que podamos revisar y aprobar tus cambios de manera efectiva:

- Sigue los estándares de codificación y estilo de nuestro proyecto.
- Asegúrate de que el código nuevo esté cubierto por pruebas unitarias.
- Documenta cualquier cambio que hagas en la documentación del proyecto.

¡Gracias de nuevo por tu interés en contribuir! Si tienes alguna pregunta o necesitas ayuda, no dudes en ponerte en contacto con nosotros en la sección de issues o enviándonos un mensaje directo.

## Licencia

Este proyecto está licenciado bajo la Licencia MIT. Esto significa que tienes permiso para utilizar, copiar, modificar, fusionar, publicar, distribuir, sublicenciar y/o vender copias del software, y para permitir que las personas a las que se les proporcione el software lo hagan, con sujeción a las siguientes condiciones:

- Se debe incluir una copia de la licencia en todas las copias o partes sustanciales del software.
- El software se proporciona "tal cual", sin garantía de ningún tipo, expresa o implícita, incluyendo pero no limitado a garantías de comerciabilidad, aptitud para un propósito particular y no infracción. En ningún caso los autores o titulares de la licencia serán responsables de cualquier reclamo, daño u otra responsabilidad, ya sea en una acción de contrato, agravio o de otra manera, que surja de, fuera de o en conexión con el software o el uso u otros tratos en el software.

Puedes encontrar una copia completa de la Licencia MIT en el archivo LICENSE que se incluye en este repositorio.

## Contacto

Si tienes alguna pregunta o comentario sobre el proyecto, no dudes en ponerte en contacto conmigo a través de los siguientes medios:

- Correo electrónico: [lesclaz95@gmail.com](mailto:lesclaz95@gmail.com)
- Twitter: [@lesclaz](https://twitter.com/lesclaz)
- Telegram: [@lesclaz](https://t.me/lesclaz)

Estaré encantado de escuchar tus comentarios y responder tus preguntas. ¡Gracias por tu interés en mi proyecto!
