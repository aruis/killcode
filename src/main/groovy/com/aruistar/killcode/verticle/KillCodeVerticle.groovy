package com.aruistar.killcode.verticle

import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import net.sourceforge.tess4j.Tesseract

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

@Slf4j
class KillCodeVerticle extends AbstractVerticle {

    Tesseract tess = new Tesseract()

    @Override
    void start() throws Exception {

        def port = config().getInteger("port", 9999)
        def router = Router.router(vertx)

        router.post("/")
                .handler(BodyHandler.create())
                .handler({ routingContext ->

                    def response = routingContext.response()
                    def pic = routingContext.bodyAsString.decodeBase64()

                    readCodePic(pic).setHandler({
                        if (it.succeeded()) {
                            response.end(it.result())
                        } else {
                            response.setStatusCode(500).end(it.cause().message)
                        }
                    })

                })

        HttpServer server = vertx.createHttpServer()
        server.requestHandler(router).listen(port, {
            log.info("server on $port ," + it.succeeded())
        })

        vertx.eventBus().consumer("kill_code", { message ->
            readCodePic(message.body()).setHandler({
                if (it.succeeded()) {
                    message.reply(it.result())
                } else {
                    message.fail(500, it.cause().message)
                }
            })
        })
    }

    Future readCodePic(byte[] bytes) {
        InputStream codePic = new ByteArrayInputStream(bytes)
        def fu = Future.future()
        log.info("read code pic")
        vertx.executeBlocking({
            BufferedImage image = ImageIO.read(codePic)
//            image = ImageHelper.convertImageToGrayscale(image)
//            image = ImageHelper.convertImageToBinary(image)
            String code = tess.doOCR(image).replace("\n", "")
            log.info("read code pic success , result is :" + code)
            it.complete(code)
        }, {
            if (it.succeeded()) {
                fu.complete(it.result())
            } else {
                fu.fail("read code pic error")
                it.cause().printStackTrace()
            }
        })

        return fu
    };
}
