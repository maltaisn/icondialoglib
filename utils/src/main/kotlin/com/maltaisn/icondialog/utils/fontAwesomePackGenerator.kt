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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.maltaisn.icondialog.utils.svg.PathFormatter
import com.maltaisn.icondialog.utils.svg.PathTokenizer
import com.maltaisn.icondialog.utils.svg.PathTransformer
import java.io.File


fun main(args: Array<String>) {
    val iconsJson = File(args[0])
    val categoriesYaml = File(args[1])
    val outputDir = File(args[2])

    val generator = FontAwesomeIconPackGenerator(outputDir, 24, listOf("solid"), 1)
    generator.generate(iconsJson, categoriesYaml)
}

private class FontAwesomeIconPackGenerator(
        outputDir: File, iconSize: Int,
        val variants: List<String>, val precision: Int) : MinifiedIconPackGenerator(outputDir, iconSize) {

    private var faIcons: Map<String, FaIcon> = emptyMap()
    private var faCategories: Map<String, FaCategory> = emptyMap()

    private var iconNames = mutableMapOf<Int, String>()


    fun generate(iconsJson: File, categoriesYaml: File) {
        println("Parsing input files")
        parseFaFiles(iconsJson, categoriesYaml)

        println("Creating icon pack")
        createIconPack()

        assignCategoryIds()
        createTagAliases()
        shrinkTags()
        createFiles(true)
    }

    /** Parse Font Awesome input files. */
    private fun parseFaFiles(iconsJson: File, categoriesYaml: File) {
        // Create JSON and YAML parsers
        val jsonMapper = ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerKotlinModule()
        val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

        // Parse icons and categories
        faIcons = jsonMapper.readValue<MutableMap<String, FaIcon>>(iconsJson)
        faCategories = yamlMapper.readValue<MutableMap<String, FaCategory>>(categoriesYaml)
    }

    /** Create initial icon pack with all icons and categories. */
    private fun createIconPack() {
        iconPack.clear()
        iconNames.clear()

        // Filter icons that have at least one of the selected variants.
        val icons = faIcons.filterTo(mutableMapOf()) { (_, icon) -> icon.variants.keys.any { it in variants } }

        // STEP 1 - Assign a category to each icon.
        // 1. Create of a map of icons mapped to a list of all the categories it's in.
        val catgsByIcon = icons.keys.associateWithTo(mutableMapOf()) { mutableListOf<String>() }
        for ((catgName, category) in faCategories) {
            for (iconName in category.icons) {
                catgsByIcon[iconName]?.add(catgName)
            }
        }

        // 2. Group icon names by category name. Start with icons with a single category.
        val iconsByCatg = sortedMapOf<String, MutableList<String>>()
        val catgsByIconIterator = catgsByIcon.iterator()
        for ((iconName, catgList) in catgsByIconIterator) {
            if (catgList.size == 0) {
                // Remove icons without category.
                catgsByIconIterator.remove()
                icons -= iconName
            } else if (catgList.size == 1) {
                // Icon has a single category.
                iconsByCatg.getOrPut(catgList.first()) { mutableListOf() } += iconName
                catgsByIconIterator.remove()
            }
        }

        // 3. Add remaining icons (with 2 or more categories) to the category with the most icons,
        // un-added categories counting as 0. This aims to reduce the number of categories.
        for ((iconName, catgList) in catgsByIcon) {
            val catgName = catgList.maxBy { iconsByCatg[it]?.size ?: 0 }!!
            iconsByCatg.getOrPut(catgName) { mutableListOf() } += iconName
        }

        // STEP 2 - Create icons
        // Find maximum icon size
        var maxSize = 0
        for (icon in icons.values) {
            for ((variantName, variant) in icon.variants) {
                if (variantName in variants) {
                    if (variant.width > maxSize) maxSize = variant.width
                    if (variant.height > maxSize) maxSize = variant.height
                }
            }
        }

        // Create icons and categories without assigning category IDs.
        val pathTokenizer = PathTokenizer()
        val pathFormatter = PathFormatter(precision)
        for ((catgName, catgIcons) in iconsByCatg) {
            // Create category
            val faCatg = faCategories[catgName] ?: continue
            val catg = Category(-1, catgName.normalizeName(), faCatg.name)

            for (iconName in catgIcons) {
                val faIcon = icons[iconName] ?: continue

                // Base icon ID is derived from unicode code point.
                val baseId = faIcon.codePoint.toInt(16) and 0x0FFF

                // Create tags from search terms and icon name
                val iconTags = getIconTags(faIcon).toMutableList()

                for ((i, variantName) in variants.withIndex()) {
                    val variant = faIcon.variants[variantName] ?: continue

                    // Variants add a few bits to the ID indicating variant index.
                    val id = baseId or (i shl 24)

                    // Tokenize, transform and format path data.
                    val pathTokens = pathTokenizer.tokenize(variant.pathData)
                    val scale = iconSize / maxSize.toDouble()
                    val pathTransformer = PathTransformer((maxSize - variant.width) / 2.0,
                            (maxSize - variant.height) / 2.0, scale, scale)
                    val transformedPath = pathTransformer.applyTransform(pathTokens)
                    val pathData = pathFormatter.format(transformedPath)

                    // Add icon to pack.
                    val icon = Icon(id, iconTags, pathData, iconSize, iconSize)
                    iconPack.getOrPut(catg) { mutableListOf() } += icon
                }
            }
        }
    }

    /** Reduce the number of categories by moving icons in categories with less icons. */
    private fun reduceCategories() {

    }

    /** Assign an ID to each category of the icon pack. */
    private fun assignCategoryIds() {
        for ((i, catg) in iconPack.keys.withIndex()) {
            catg.id = i
        }
    }

    private fun getIconTags(icon: FaIcon): MutableSet<Tag> {
        val tags = mutableSetOf<Tag>()

        // Add tags from search terms and icon name.
        icon.search.terms.flatMapTo(tags) { getTagsFromString(it) }
        tags += getTagsFromString(icon.name)

        // Discard tags with names of 2 letters or less, and tags with no letters.
        tags.removeIf { it.name.length <= 2 || "[a-z]".toRegex() !in it.name }

        return tags
    }

    private fun getTagsFromString(str: String) = str.split('-', '_', ' ', '&').map {
        var name = it
        if (name.endsWith("'s")) name = name.substring(0, name.length - 2)
        Tag(name.normalizeName(), listOf(name.normalizeValue()))
    }

}

private data class FaIcon(
        @JsonProperty("unicode") val codePoint: String,
        @JsonProperty("search") val search: FaIconSearch,
        @JsonProperty("label") val name: String,
        @JsonProperty("svg") val variants: Map<String, FaIconVariant>,
        @JsonIgnore var category: Int = -1)

private data class FaIconVariant(
        @JsonProperty("width") val width: Int,
        @JsonProperty("height") val height: Int,
        @JsonProperty("path") val pathData: String)

private data class FaIconSearch(
        @JsonProperty("terms") val terms: List<String>)

private data class FaCategory(
        @JsonProperty("icons") val icons: List<String>,
        @JsonProperty("label") val name: String,
        @JsonIgnore var id: Int = -1)
