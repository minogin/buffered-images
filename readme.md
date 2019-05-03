# Description
Kotlin utility functions for Java AWT BufferedImage

### Sample usage

```kotlin
val image = BufferedImages.load("large-cat-2000x2000.png")
image = image.subimage(500, 500, 1000, 1000)

val g = image.createBestQualityGraphics()
try {
    g.drawImage...
    g.fillRect...
}
finally {
    g.dispose()
}

image = image.resize(100, 100)  // This is way faster than AWT!
image = image.toJpeg()
image.save("small-cat.jpg")
```    

### Features
* Easily create, load, save, send images
* Fast resize
* High-quality graphics instantiation
* JPEG / PNG conversion
* Subimage not failing on out-of-borders
* Get image size (for content-length) without loading the whole image 
* Only two image types - JPEG and transparent PNG, no need to deal with different AWT types
* Functions are `java.awt.image.BufferedImage` class extensions 

### Please note!
          
* We assume that PNG is transparent, i.e. always have alpha-channel. 

# Installation and usage

Simply clone com.minogin.graphics.BufferedImages.kt to your project 

# Documentation

## Image type

`com.minogin.graphics.ImageType`

Either JPEG or PNG.

Could be detected: `of(awtType: Int)`, `of(f: File):`, `of(filePath: String)`

Could be converted: `toAWT()`, `toContentType()`, `toImageWriter()`, `toImageWriteParam(writer: ImageWriter)` 

## Functions

*BufferedImages companion object*

#### Create new image
``BufferedImages.create(width: Int, height: Int, type: ImageType): BufferedImage``

#### Load image from file or path

``BufferedImages.load(inputStream: InputStream, type: ImageType): BufferedImage``

``BufferedImages.load(file: File, type: ImageType = ImageType.of(file)): BufferedImage``

``BufferedImages.load(path: String, type: ImageType = ImageType.of(path)): BufferedImage``

All functions load image to Int instead of Byte buffer

#### Send image to HTTP response with correct content-length

``BufferedImages.send(imagePath: String, response: HttpServletResponse)``

Useful for Spring MVC controllers

\
*java.awt.image.BufferedImage extensions*

#### Fast resize

``fun resize(targetWidth: Int, targetHeight: Int, type: ImageType = getImageType()): BufferedImage``

``fun scaleToNewWidth(targetWidth: Int, type: ImageType = getImageType()): BufferedImage``

``fun scaleToNewHeight(newHeight: Int, type: ImageType = getImageType()): BufferedImage``

Way faster than AWT getScaledInstance(...)

#### Get size without loading the image

``data class ImageSize(val width: Int, val height: Int)``

``fun getImageSize(inputStream: InputStream): ImageSize``

``fun getImageSize(path: String): ImageSize``

#### Fail-safe subimage 

``fun subimage(x: Int, y: Int, w: Int, h: Int): BufferedImage``

Doesn't fail on out of border values

#### Graphics

``fun createBestQualityGraphics(): Graphics2D``

Creates graphics with high quality hints applied (for text rendering either).

#### Type conversion

``fun toJpeg(): BufferedImage``

``fun toPng(): BufferedImage``

#### Saving

``fun save(file: File, type: ImageType = getImageType())``

``fun save(path: String, type: ImageType = getImageType())``

#### Send image to HTTP response with correct content-length

``fun send(response: HttpServletResponse, type: ImageType = getImageType())``

Better use ``BufferedImages.send(...)`` as it doesn't buffer the whole image to determine its size. 

Useful for Spring MVC controllers.

#### Image type

``fun getImageType(): ImageType``

#### Unit testing

``fun isEqualTo(img: BufferedImage): Boolean``

Pixel by pixel images comparison.
 

# Repository

https://github.com/minogin/buffered-images

# Contacts

Please contact me at [minogin@gmail.com](mailto:minogin@gmail.com) or at GitHub 

# License

[ISC](https://opensource.org/licenses/ISC)
