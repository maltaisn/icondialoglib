import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory

val forcePngGeneration = false

val imageWidth = 500
val iconSize = 48
val iconPadding = 2

fun main() {
    println("Loading icons and labels data")

    // Load labels
    val labelFile = File("../src/main/res/xml/icd_labels.xml")
    val labelXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(labelFile)
    val labelElements = labelXml.getElementsByTagName("label")
    val labels = List(labelElements.length) {
        val name = labelElements.item(it).attributes.getNamedItem("name").nodeValue
        Label(name)
    }

    // Load icon labels
    val iconFile = File("../src/main/res/xml/icd_icons.xml")
    val iconXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(iconFile)
    val iconElements = iconXml.getElementsByTagName("icon")
    val icons = List(iconElements.length) {
        val attrs = iconElements.item(it).attributes
        val id = attrs.getNamedItem("id").nodeValue.toInt()
        val iconLabels = attrs.getNamedItem("labels").nodeValue.split(",")
        Icon(id, iconLabels)
    }

    // For each label, find by which icons it's used
    for (label in labels) {
        for (icon in icons) {
            if (label.name in icon.labels) {
                label.icons += icon.id
            }
        }
    }

    // Convert all SVG files to PNG with inkscape.
    // This takes approx. 5 min.
    val svgDir = File("svg/")
    val pngDir = File("png/")
    if (forcePngGeneration || !pngDir.exists()) {
        pngDir.delete()
        pngDir.mkdirs()
        for ((i, icon) in icons.withIndex()) {
            val svg = File(svgDir, "${icon.id}.svg")
            val png = File(pngDir, "${icon.id}.png")
            Runtime.getRuntime().exec("\"D:/Program Files/Inkscape/inkscape.exe\" " +
                    "-f ${svg.path} -e ${png.path} -w $iconSize -h $iconSize -z")
            print("\rConverting icons SVG to PNG: $i / ${icons.size}")
            Thread.sleep(1)
        }
        println()
    }

    // Load all PNG icons
    val iconImages = icons.associate {
        it.id to ImageIO.read(File(pngDir, "${it.id}.png"))
    }

    // Create the label images
    // This takes approx. 30 sec.
    val imagesDir = File("label-images/")
    imagesDir.delete()
    imagesDir.mkdirs()
    val font = Font("Arial", Font.BOLD, 24)
    val iconsInWidth = imageWidth / (iconSize + iconPadding)
    for ((i, label) in labels.withIndex()) {
        // Create an empty white image with the correct dimensions
        val height = 36 + ceil(label.icons.size / iconsInWidth.toFloat()) * (iconSize + iconPadding)
        val buffImage = BufferedImage(imageWidth, height.toInt(), BufferedImage.TYPE_INT_RGB)
        val graphics = buffImage.createGraphics()
        graphics.background = Color.WHITE
        graphics.clearRect(0, 0, buffImage.width, buffImage.height)

        // Print the label name centered
        graphics.color = Color.BLACK
        graphics.font = font
        val textWidth = graphics.fontMetrics.stringWidth(label.name)
        graphics.drawString(label.name, (buffImage.width - textWidth) / 2, 26)

        // Draw the icons
        var x = 1
        var y = 34
        for ((j, iconId) in label.icons.withIndex()) {
            graphics.drawImage(iconImages[iconId], x, y, null)
            if (j != 0 && j % 10 == 0) {
                x = 1
                y += iconSize + iconPadding
            } else {
                x += iconSize + iconPadding
            }
        }

        // Export the image
        ImageIO.write(buffImage, "png", File(imagesDir, "${label.name}.png"))

        print("\rGenerating label images: $i / ${labels.size} (${label.name})")
    }

    println()
    println("Done")
}

data class Icon(val id: Int, val labels: List<String>)

data class Label(val name: String) {
    val icons = ArrayList<Int>()
}

main()