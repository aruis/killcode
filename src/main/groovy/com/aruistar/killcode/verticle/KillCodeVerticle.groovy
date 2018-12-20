package com.aruistar.killcode.verticle

import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.util.ImageHelper

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

@Slf4j
class KillCodeVerticle extends AbstractVerticle {

    Tesseract tess = new Tesseract()

    @Override
    void start() throws Exception {

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
            image = ImageHelper.convertImageToGrayscale(image)
            image = ImageHelper.convertImageToBinary(image)
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
