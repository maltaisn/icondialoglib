/*
 * Copyright 2020 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.icondialog.utils

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.awt.Color
import java.awt.Font
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil


// Whether to overwrite existing PNG icons.
// PNG generation is a long step so this is should be false.
const val FORCE_PNG_GENERATION = false

// Generated images settings
const val IMAGE_WIDTH = 500
const val ICON_SIZE = 48
const val ICON_PADDING = 2

/**
 * This program expects the following arguments:
 * 1. Icon XML file path
 * 2. Output directory path
 * 3. Path to inkscape executable.
 *
 * Note: doesn't support icons without category.
 */
fun main(args: Array<String>) {
    val iconsFile = File(args[0])
    val outputDir = File(args[1])
    val inkscape = File(args[2])

    println("Loading icons and tags data")

    // Create XML parser and parse icons
    val xmlParser = XmlMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerKotlinModule()
    val iconsXml: Icons = xmlParser.readValue(iconsFile)

    // Find which icons use each tag
    val icons = iconsXml.category.flatMapTo(mutableListOf()) { it.icon }
    icons.sortBy { it.id }
    val tagsUses = mutableMapOf<String, MutableList<Int>>()
    for (icon in icons) {
        val tags = icon.tags.split(',')
        for (tag in tags) {
            if (!tag.startsWith('_')) {
                tagsUses.getOrPut(tag) { mutableListOf() } += icon.id
            }
        }
    }

    // Set global width and height on icons
    for (icon in icons) {
        if (icon.width == -1) icon.width = iconsXml.width
        if (icon.height == -1) icon.height = iconsXml.height
    }

    // Create PNG output dir.
    val pngDir = File(outputDir, "png")
    val svgFile = File(outputDir, "icon.svg")
    if (FORCE_PNG_GENERATION) {
        pngDir.delete()
    }
    pngDir.mkdirs()

    // Convert path object for all icons
    val paths = mutableListOf<Path2D>()
    for ((i, icon) in icons.withIndex()) {
        val png = File(pngDir, "${icon.id}.png")
        if (!png.exists()) {
            // Build SVG and save to temp file
            val svg = """<svg width="${icon.width}" height="${icon.height}"><path d="${icon.pathData}"/></svg>"""
            svgFile.writeText(svg)

            // Use inkscape to convert SVG to PNG.
            ProcessBuilder(inkscape.absolutePath,
                    "-f", svgFile.absolutePath, "-e", png.path,
                    "-w", ICON_SIZE.toString(), "-h", ICON_SIZE.toString(), "-z")
                    .start().waitFor()
        }
        print("\rConverting icons SVG to PNG: ${i + 1} / ${icons.size} (ID ${icon.id})")
    }
    println()

    // Load all PNG icons
    val iconImages = icons.associate {
        it.id to ImageIO.read(File(pngDir, "${it.id}.png"))
    }

    // Create the tag images.
    val tagImagesDir = File(outputDir, "images")
    tagImagesDir.delete()
    tagImagesDir.mkdirs()

    val font = Font("Arial", Font.BOLD, 24)
    val iconsInWidth = IMAGE_WIDTH / (ICON_SIZE + ICON_PADDING)
    var i = 0
    for ((tagName, tagUses) in tagsUses) {
        // Create an empty white image with the correct dimensions
        val height = 36 + ceil(tagUses.size / iconsInWidth.toFloat()) * (ICON_SIZE + ICON_PADDING)
        val buffImage = BufferedImage(IMAGE_WIDTH, height.toInt(), BufferedImage.TYPE_INT_RGB)
        val graphics = buffImage.createGraphics()
        graphics.background = Color.WHITE
        graphics.clearRect(0, 0, buffImage.width, buffImage.height)

        // Print the tag name centered
        graphics.color = Color.BLACK
        graphics.font = font
        val textWidth = graphics.fontMetrics.stringWidth(tagName)
        graphics.drawString(tagName, (buffImage.width - textWidth) / 2, 26)

        // Draw the icons
        var x = 1
        var y = 34
        for ((j, iconId) in tagUses.withIndex()) {
            graphics.drawImage(iconImages[iconId], x, y, null)
            if (j != 0 && j % 10 == 0) {
                x = 1
                y += ICON_SIZE + ICON_PADDING
            } else {
                x += ICON_SIZE + ICON_PADDING
            }
        }

        // Export the image
        ImageIO.write(buffImage, "png", File(tagImagesDir, "$tagName.png"))

        print("\rGenerating tag images: ${i + 1} / ${tagsUses.size} ($tagName)")
        i++
    }

    println()
    println("Done")
}

data class Icons(@JsonProperty("width") val width: Int,
                 @JsonProperty("height") val height: Int,
                 @JacksonXmlElementWrapper(useWrapping = false) val category: List<Category>)

data class Category(@JacksonXmlElementWrapper(useWrapping = false) val icon: List<Icon>)

data class Icon(@JsonProperty("id") val id: Int,
                @JsonProperty("tags") val tags: String = "",
                @JsonProperty("width") var width: Int = -1,
                @JsonProperty("height") var height: Int = -1,
                @JsonProperty("path") val pathData: String)
