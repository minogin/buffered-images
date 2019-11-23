package com.minogin.graphics

import org.apache.commons.io.IOUtils
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints.*
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.*
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.FileImageOutputStream
import javax.imageio.stream.MemoryCacheImageOutputStream
import javax.servlet.http.HttpServletResponse
import kotlin.math.roundToInt

/**
 * Convenient methods for Java AWT
 *
 * @author Andrey Minogin
 */
enum class ImageType {
    JPEG,
    PNG;

    companion object {
        fun of(awtType: Int): ImageType =
            when (awtType) {
                TYPE_INT_RGB -> JPEG
                TYPE_INT_ARGB -> PNG
                else -> throw IllegalArgumentException(awtType.toString())
            }

        fun of(f: File): ImageType =
            when (val ext = f.extension.toLowerCase()) {
                "jpg", "jpeg" -> JPEG
                "png" -> PNG
                else -> throw Exception("Unknown file format: $ext")
            }

        fun of(filePath: String): ImageType = of(File(filePath))
    }

    fun toAWT(): Int = when (this) {
        ImageType.JPEG -> TYPE_INT_RGB
        ImageType.PNG -> TYPE_INT_ARGB
    }

    fun toContentType(): String = when (this) {
        JPEG -> "image/jpeg"
        PNG -> "image/png"
    }

    fun toImageWriter(): ImageWriter = when (this) {
        JPEG -> ImageIO.getImageWritersByFormatName("jpeg").next()
        PNG -> ImageIO.getImageWritersByFormatName("png").next()
    }

    fun toImageWriteParam(writer: ImageWriter): ImageWriteParam {
        val param = writer.defaultWriteParam
        when (this) {
            ImageType.JPEG -> param.apply {
                compressionMode = ImageWriteParam.MODE_EXPLICIT
                compressionQuality = 1f
            }
            ImageType.PNG -> param.apply {}
        }
        return param
    }
}

class BufferedImages {
    companion object {
        fun create(width: Int, height: Int, type: ImageType): BufferedImage =
            BufferedImage(
                if (width > 0) width else 1,
                if (height > 0) height else 1,
                type.toAWT()
            )

        /**
         * Loads image to int buffer instead of byte
         */
        fun load(inputStream: InputStream, type: ImageType): BufferedImage {
            inputStream.use {
                val ins = ImageIO.createImageInputStream(inputStream)
                val reader = ImageIO.getImageReaders(ins).next()
                try {
                    reader.input = ins
                    val param = reader.defaultReadParam.apply {
                        destination = BufferedImage(reader.getWidth(0), reader.getHeight(0), type.toAWT())
                    }
                    return reader.read(0, param)
                } finally {
                    reader.dispose()
                }
            }
        }

        /**
         * Loads image to int buffer instead of byte
         */
        fun load(file: File, type: ImageType = ImageType.of(file)): BufferedImage {
            return load(FileInputStream(file), type)
        }

        /**
         * Loads image to int buffer instead of byte
         */
        fun load(path: String, type: ImageType = ImageType.of(path)): BufferedImage {
            return load(File(path), type)
        }

        /** Useful for MVC controllers */
        fun send(imagePath: String, response: HttpServletResponse) {
            response.contentType = ImageType.of(imagePath).toContentType()
            response.setContentLength(File(imagePath).length().toInt())

            val ins = FileInputStream(imagePath)
            ins.use {
                val bos = BufferedOutputStream(response.outputStream)
                IOUtils.copy(ins, bos)
                bos.flush()
            }
        }
    }
}

fun BufferedImage.getImageType(): ImageType = ImageType.of(this.type)

/** Pixel by pixel images comparison. Useful for unit tests. */
fun BufferedImage.isEqualTo(img: BufferedImage): Boolean {
    if (width != img.width || height != img.height)
        return false

    for (y in 0 until height) {
        for (x in 0 until width) {
            if (getRGB(x, y) != img.getRGB(x, y))
                return false
        }
    }

    return true
}

/**
 * This method should be used instead of
 * [Image.getScaledInstance] as it is much faster giving
 * the same quality.
 *
 * See [this link](https://community.oracle.com/docs/DOC-983611) for more information.  */
fun BufferedImage.resize(targetWidth: Int, targetHeight: Int, type: ImageType = getImageType()): BufferedImage {
    if (targetWidth == width && targetHeight == height)
        return this

    val w = if (width / 2 > targetWidth) width / 2 else targetWidth
    val h = if (height / 2 > targetHeight) height / 2 else targetHeight

    BufferedImage(w, h, type.toAWT()).apply {
        createGraphics().apply {
            setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR)
            drawImage(this@resize, 0, 0, w, h, null)
            dispose()
        }

        return resize(targetWidth, targetHeight, type)
    }
}

/** Scale image to the given width preserving aspect ratio.  */
fun BufferedImage.scaleToNewWidth(targetWidth: Int, type: ImageType = getImageType()): BufferedImage {
    if (targetWidth == width)
        return this

    val targetHeight = (targetWidth.toDouble() * height.toDouble() / width.toDouble()).roundToInt()
    return resize(targetWidth, targetHeight, type)
}

/** Scales image to the given height preserving aspect ratio.  */
fun BufferedImage.scaleToNewHeight(newHeight: Int, type: ImageType = getImageType()): BufferedImage {
    if (newHeight == height)
        return this

    val targetWidth = (newHeight.toDouble() * width.toDouble() / height.toDouble()).roundToInt()
    return resize(targetWidth, newHeight, type)
}

/** Useful for png to jpeg conversion */
fun BufferedImage.toJpeg(): BufferedImage {
    return BufferedImage(width, height, ImageType.JPEG.toAWT()).apply {
        createGraphics().apply {
            drawImage(this@toJpeg, 0, 0, null)
            dispose()
        }
    }
}

fun BufferedImage.toPng(): BufferedImage {
    return BufferedImage(width, height, ImageType.PNG.toAWT()).apply {
        createGraphics().apply {
            drawImage(this@toPng, 0, 0, null)
            dispose()
        }
    }
}

fun BufferedImage.save(file: File, type: ImageType = getImageType()) {
    file.parentFile.mkdirs()

    val writer = type.toImageWriter()
    val param = type.toImageWriteParam(writer)
    val os = FileImageOutputStream(file)
    os.use {
        try {
            writer.output = os
            val iioImage = IIOImage(this, null, null)
            writer.write(null, iioImage, param)
            os.flush()
        } finally {
            writer.dispose()
        }
    }
}

fun BufferedImage.save(path: String, type: ImageType = getImageType()) {
    save(File(path), type)
}

private const val INITIAL_BUFSIZE = 4096

/** If possible use [sendImage] instead of
 * this method due to overhead caused by determining image size.  */
fun BufferedImage.send(response: HttpServletResponse, type: ImageType = getImageType()) {
    response.contentType = type.toContentType()

    val bos = ByteArrayOutputStream(INITIAL_BUFSIZE)
    bos.use {
        val imageos = MemoryCacheImageOutputStream(bos)
        val writer = type.toImageWriter()
        try {
            val param = type.toImageWriteParam(writer)
            val iioImage = IIOImage(this, null, null)
            writer.output = imageos
            writer.write(null, iioImage, param)
            bos.flush()
        } finally {
            writer.dispose()
        }

        val byteInputStream = ByteArrayInputStream(bos.toByteArray())
        byteInputStream.use {
            val os = response.outputStream
            response.setContentLength(bos.size())
            IOUtils.copy(byteInputStream, os)
        }
    }
}

/** Use this method instead of [createGraphics] for best shape and text quality. */
fun BufferedImage.createBestQualityGraphics(): Graphics2D =
    createGraphics().apply {
        setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
        setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY)

        setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON)
        setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_ON)
    }

/** Boundary safe [getSubImage] */
fun BufferedImage.subimage(x: Int, y: Int, w: Int, h: Int): BufferedImage {
    if (w < 0 || h < 0)
        throw IllegalArgumentException("Width and height should be non-negative: ($w; $h)")

    var x1 = x
    var x2 = x + w     // w >= 0 => x1 <= x2
    x1 = x1.coerceIn(0, width)
    x2 = x2.coerceIn(0, width)

    var y1 = y
    var y2 = y + h     // h >= 0 => y1 <= y2
    y1 = y1.coerceIn(0, height)
    y2 = y2.coerceIn(0, height)

    if (x2 - x1 == 0 || y2 - y1 == 0)
        return BufferedImages.create(1, 1, getImageType())

    return getSubimage(x1, y1, x2 - x1, y2 - y1)
}

fun BufferedImage.rotateQuadrant(angleDegree: Int): BufferedImage {
    val w: Double
    val h: Double
    val tx: Double
    val ty: Double
    when {
        angleDegree == 0 -> return this
        angleDegree % 180 == 0 -> {
            w = width.toDouble()
            h = height.toDouble()
            tx = 0.0
            ty = 0.0
        }
        angleDegree % 90 == 0 -> {
            w = height.toDouble()
            h = width.toDouble()
            tx = (w - h) / 2
            ty = (h - w) / 2
        }
        else -> throw IllegalArgumentException("Angle must be a multiple of 90")
    }

    val rotatedImage = BufferedImages.create(w.roundToInt(), h.roundToInt(), getImageType())
    val g = rotatedImage.createBestQualityGraphics()

    val cx = w / 2
    val cy = h / 2

    try {
        g.translate(tx, ty)
        g.rotate(Math.toRadians(angleDegree.toDouble()), cx - tx, cy - ty)
        g.drawRenderedImage(this, null)
    } finally {
        g.dispose()
    }

    return rotatedImage
}

data class ImageSize(val width: Int, val height: Int)

/** Get image size without loading the image.  */
fun getImageSize(inputStream: InputStream): ImageSize {
    val ins = ImageIO.createImageInputStream(inputStream)
    ins.use {
        val reader = ImageIO.getImageReaders(ins).next()
        try {
            reader.input = ins
            return ImageSize(reader.getWidth(0), reader.getHeight(0))
        } finally {
            reader.dispose()
        }
    }
}

/** Get image size without loading the image.  */
fun getImageSize(path: String): ImageSize = getImageSize(FileInputStream(path))
