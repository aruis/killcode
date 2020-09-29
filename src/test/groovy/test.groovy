import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions

vertx = Vertx.vertx()
def file = new File("/Users/liurui/Desktop/1.png")
def pic = file.bytes.encodeBase64().toString()

def kcHost = "202.98.194.175"
def kcPort = 9999

killClient = WebClient.create(vertx, new WebClientOptions().setDefaultHost(kcHost).setDefaultPort(kcPort))

killClient.post("/").sendBuffer(Buffer.buffer(pic), {
    println(it.succeeded())
    if (it.succeeded()) {
        println(it.result().bodyAsString())
    } else {
    }

})
