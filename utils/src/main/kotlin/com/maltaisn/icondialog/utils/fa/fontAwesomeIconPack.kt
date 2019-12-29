/*
 * Copyright 2019 Nicolas Maltais
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

package com.maltaisn.icondialog.utils.fa

import com.beust.jcommander.JCommander
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.maltaisn.icondialog.utils.*
import com.maltaisn.icondialog.utils.fa.data.FaCategory
import com.maltaisn.icondialog.utils.fa.data.FaIcon
import com.maltaisn.icondialog.utils.svg.PathFormatter
import com.maltaisn.icondialog.utils.svg.PathTokenizer
import com.maltaisn.icondialog.utils.svg.PathTransformer
import java.io.File


fun main(args: Array<String>) {
    // Parse parameters
    val params = FaParameters()
    JCommander.newBuilder().addObject(params).build().parse(*args)

    // Parse input files
    val jsonMapper = ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerKotlinModule()
    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    println("Parsing input files")
    val faIcons: MutableMap<String, FaIcon> = jsonMapper.readValue(File(params.iconsJson))
    val faCategories: Map<String, FaCategory> = yamlMapper.readValue(File(params.categoriesYaml))

    println("Converting to icon pack format")

    // Remove icons that doesn't have any of the selected variants
    faIcons.entries.removeIf { (_, icon) -> icon.variants.keys.none { it in params.variants } }

    // STEP 1 - Create a list of categories and assign a category to each icon.
    // 1. Create of a map of icons mapped to a list of all the categories it's in.
    val catgsByIcon = faIcons.keys.associateWithTo(mutableMapOf()) { mutableListOf<String>() }
    for ((catgName, category) in faCategories) {
        for (iconName in category.icons) {
            catgsByIcon[iconName]?.add(catgName)
        }
    }

    // 2. Group icons by category ID. Start with icons with a single category.
    val iconsByCatg = sortedMapOf<String, MutableList<String>>()
    val catgsByIconIterator = catgsByIcon.iterator()
    for ((iconName, catgList) in catgsByIconIterator) {
        if (catgList.size == 0) {
            // Remove icons without category.
            catgsByIconIterator.remove()
            faIcons -= iconName
        } else if (catgList.size == 1) {
            // Icon has a single category.
            iconsByCatg.getOrPut(catgList.first()) { mutableListOf() } += iconName
            catgsByIconIterator.remove()
        }
    }

    // 3. Add remaining icons (with 2 or more categories) to the category with the least icons,
    // un-added categories counting as most. This aims to balance number of icons in each category.
    for ((iconName, catgList) in catgsByIcon) {
        val catgName = catgList.minBy { iconsByCatg[it]?.size ?: Int.MAX_VALUE }!!
        iconsByCatg.getOrPut(catgName) { mutableListOf() } += iconName
    }

    // 4. Assign ID to each category and set it to icons.
    // 5. Create pack categories from FA categories
    var catgId = 0
    val categories = mutableListOf<Category>()
    for ((catgName, icons) in iconsByCatg.entries) {
        val faCatg = faCategories[catgName] ?: continue
        faCatg.id = catgId
        for (iconName in icons) {
            faIcons[iconName]?.category = catgId
        }
        categories += Category(faCatg.id, catgName.normalizeName(), faCatg.name)
        catgId++
    }

    // STEP 2 - Create a list of icons from FA icons
    // 1. Find maximum icon dimensions
    var maxSize = 0
    for (icon in faIcons.values) {
        for ((variantName, variant) in icon.variants) {
            if (variantName in params.variants) {
                if (variant.width > maxSize) maxSize = variant.width
                if (variant.height > maxSize) maxSize = variant.height
            }
        }
    }

    // 2. Assign ID and transform path data of each icon.
    val pathTokenizer = PathTokenizer()
    val pathFormatter = PathFormatter(params.precision)
    val icons = mutableListOf<Icon>()
    val tags = mutableSetOf<Tag>()
    for (faIcon in faIcons.values) {
        // Base icon ID is derived from unicode code point.
        val baseId = faIcon.id.toInt(16) and 0x0FFF

        // Create tags from search terms, icon name and category name
        val iconTags = getIconTags(faIcon, categories[faIcon.category])
        val tagNames = iconTags.map { it.name }
        tags += iconTags

        for ((i, variantName) in params.variants.withIndex()) {
            val variant = faIcon.variants[variantName] ?: continue

            // Variants add a few bits to the ID indicating variant index.
            val id = baseId or (i shl 24)

            // Tokenize, transform and format path data.
            val pathTokens = pathTokenizer.tokenize(variant.pathData)
            val scale = params.iconSize / maxSize.toDouble()
            val pathTransformer = PathTransformer((maxSize - variant.width) / 2.0,
                    (maxSize - variant.height) / 2.0, scale, scale)
            val transformedPath = pathTransformer.applyTransform(pathTokens)
            val pathData = pathFormatter.format(transformedPath)

            icons += Icon(id, faIcon.category, tagNames, pathData,
                    params.iconSize, params.iconSize)
        }
    }

    // STEP 3 - Create icon pack files
    println("Exporting icon pack")
    val outputDir = File(params.outputDir)
    createIconsXml(icons, categories, outputDir, params.iconSize, params.iconSize)
    createTagsXml(tags.sorted(), outputDir)

    println("DONE: ${icons.size} icons, ${categories.size} categories, ${tags.size} tags")
}

private fun getIconTags(icon: FaIcon, category: Category): List<Tag> {
    val tags = mutableSetOf<Tag>()

    // Add tags from search terms and icon name.
    icon.search.terms.flatMapTo(tags) { getTagsFromString(it) }
    tags += getTagsFromString(icon.name)

    // Add tags from category name
    tags += getTagsFromString(category.nameValue)

    // Discard tags with names of 2 letters or less, and tags with no letters.
    tags.removeIf {
        it.name.length <= 2 || "[a-z]".toRegex() !in it.name
    }

    return tags.sorted()
}

private fun getTagsFromString(str: String) = str.split('-', '_', ' ', '&').map {
    var name = it
    if (name.endsWith("'s")) name = name.substring(0, name.length - 2)
    Tag(name.normalizeName(), listOf(name.normalizeValue()))
}
