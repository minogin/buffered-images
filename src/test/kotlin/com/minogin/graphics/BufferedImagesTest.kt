package com.minogin.graphics

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

const val IMAGES_PATH = "src/test/resources/images/"
const val TEMP_IMAGE_PATH = IMAGES_PATH + "temp.jpg"

class BufferedImagesTest {
    @Test
    fun resize() {
        BufferedImages.load(IMAGES_PATH + "1.jpg")
            .resize(1000, 1000, ImageType.JPEG)
            .save(TEMP_IMAGE_PATH, ImageType.JPEG)

        val expected = BufferedImages.load("src/test/resources/images/resize/1000x1000.jpg")
        val actual = BufferedImages.load(IMAGES_PATH + "temp.jpg")
        assertTrue(expected.isEqualTo(actual))
    }

    @AfterEach
    fun clean() {
        File(TEMP_IMAGE_PATH).delete()
    }

    @Test
    fun subimage() {
        val image = BufferedImages.load(IMAGES_PATH + "2.jpg") // 1000x667
        image.subimage(100, 100, 200, 200)

        image.subimage(-5, 100, 200, 200)
        image.subimage(100, 100, 2000, 200)
        image.subimage(-5, 100, 2000, 200)

        image.subimage(100, -5, 200, 200)
        image.subimage(100, 100, 200, 2000)
        image.subimage(100, -5, 200, 2000)

        image.subimage(-5, -5, 2000, 2000)
    }

    @Test
    fun subimage_part() {
        val image = BufferedImages.load(IMAGES_PATH + "2.jpg") // 1000x667
        var subimage = image.subimage(500, 333, 1000, 667)
        subimage.save("d:/temp/s1.jpg", ImageType.JPEG)

        subimage = image.subimage(500, 333, 3000, 3000)
        subimage.save("d:/temp/s2.jpg", ImageType.JPEG)

        subimage = image.subimage(500, -333, 1000, 667)
        subimage.save("d:/temp/s3.jpg", ImageType.JPEG)
    }

    @Test
    fun subimage_zero() {
        val image = BufferedImages.load(IMAGES_PATH + "2.jpg") // 1000x667
        var subimage = image.subimage(-1000, -1000, 0, 0)
        subimage.save("d:/temp/sz.jpg", ImageType.JPEG)

        subimage = image.subimage(3000, 3000, 1000, 1000)
        subimage.save("d:/temp/sz.jpg", ImageType.JPEG)
    }

    @Test
    fun imageRotates() {
        val image = BufferedImages.load(IMAGES_PATH + "2.jpg") // 1000x667
        var rotated = image.rotateQuadrant(90);
        rotated.save("d:/temp/r.jpg", ImageType.JPEG)
    }
}