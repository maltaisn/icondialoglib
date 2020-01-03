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
import com.maltaisn.icondialog.utils.generator.MinifiedIconPackGenerator
import com.maltaisn.icondialog.utils.svg.PathFormatter
import com.maltaisn.icondialog.utils.svg.PathTokenizer
import com.maltaisn.icondialog.utils.svg.PathTransformer
import java.io.File


fun main(args: Array<String>) {
    val iconsJson = File(args[0])
    val categoriesYaml = File(args[1])
    val outputDir = File(args[2])

    val generator = FontAwesomeIconPackGenerator(outputDir, 640,
            listOf("solid"), 0, 4)
    generator.generate(iconsJson, categoriesYaml)
}

/**
 * Class used to generate an icon pack from Font Awesome `icons.xml` and `categories.yml` files
 * available in the desktop archive at [https://fontawesome.com/download] in the `metadata` folder.
 *
 * Since font awesome icons can belong to multiple categories but icon packs only allow one,
 * one must be chosen. First, icons that are in a single category are assigned. Then, after these
 * icons have been assigned tags, the icons that weren't assigned a category are assigned the one
 * with the maximum similarity of tags among the categories it belonged to. If icon belonged to no
 * categories, it is assigned to the category with maximum tag similarity among all categories.
 *
 * Additionally, [minIconsInCatg] can determine the minimum number of icons by category to avoid
 * having categories with 1-2 icons for example, or too many categories (FA has over 60 categories).
 * Category reduction uses tag similarity to re-assign icons.
 *
 * This method usually leads to good results but not always, because of the following reasons:
 * - `square-full` is assigned to 'Chess' and not 'Shapes' because it's miscategorized in input data.
 * - `crown` is assigned to 'Food' because it had no category in input data, and shares no tags
 * with more relevant categories, like 'People'.
 * - `square` is assigned to 'Moving' after being removed from removed category 'Shapes'.
 *
 * Icon are assigned an ID derived from the unicode code point and the variant index.
 * This results in IDs in a reasonably small range: 0..12,287.
 *
 * Tags are derived from icon search terms, icon name, and the name of the category is which icon
 * is. Each of these terms is split on various separators and each part becomes a tag. Tags with 2
 * characters or less and fully numeric tags are discarded. The result allows search to be performed
 * correctly but isn't translatable AT ALL. For example, "New Year's Eve" becomes 3 tags: "New",
 * "Year" and "Eve", which obviously can't be translated word by word.
 *
 * @param precision Number of decimals used in path data.
 * @param variants Selected variants. Icon without any of these variants aren't added.
 */
private class FontAwesomeIconPackGenerator(
        outputDir: File, iconSize: Int,
        val variants: List<String>, val precision: Int, val minIconsInCatg: Int
) : MinifiedIconPackGenerator(outputDir, iconSize) {

    private var faIcons: Map<String, FaIcon> = emptyMap()
    private var faCategories: Map<String, FaCategory> = emptyMap()


    fun generate(iconsJson: File, categoriesYaml: File) {
        // Parse
        println("Parsing input files")
        parseFaFiles(iconsJson, categoriesYaml)

        // Create
        println("Creating icon pack")
        createIconPack()
        reduceCategories()
        addCategoryNameToTags()
        assignCategoryIds()

        // Shrink
        createTagAliases()
        shrinkTags()

        // Export
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

        // Filter icons that have at least one of the selected variants and aren't blacklisted.
        val icons = faIcons.filter { (name, icon) ->
            name !in ICON_BLACKLIST && icon.variants.keys.any { it in variants }
        }

        // Create of a map of icons mapped to a list of all the categories it's in.
        val catgsByIcon = icons.keys.associateWithTo(mutableMapOf()) { mutableListOf<String>() }
        for ((catgName, category) in faCategories) {
            for (iconName in category.icons) {
                catgsByIcon[iconName]?.add(catgName)
            }
        }

        // Assign icons with a single category to this category. Icons without a category or
        // icons with multiple categories are assigned to no category.
        val iconsByCatg = mutableMapOf<String, MutableList<String>>()
        for ((iconName, catgList) in catgsByIcon) {
            val catg = if (catgList.size == 1) catgList.first() else NO_CATEGORY.name
            iconsByCatg.getOrPut(catg) { mutableListOf() } += iconName
        }

        // Find maximum icon viewport size
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

        val categories = mutableMapOf<String, Category>()
        val iconToFaIconName = mutableMapOf<Icon, String>()

        for ((catgName, catgIcons) in iconsByCatg) {
            // Create category
            val catg = if (catgName == NO_CATEGORY.name) {
                NO_CATEGORY
            } else {
                val faCatg = faCategories[catgName] ?: error("")
                Category(-1, catgName.normalizeName(), faCatg.name)
            }
            categories[catgName] = catg

            val catgTags = getTagsFromString(catg.nameValue)

            // Create category icons
            for (iconName in catgIcons) {
                val faIcon = icons[iconName] ?: error("")

                // Base icon ID is derived from unicode code point.
                val baseId = faIcon.codePoint.toInt(16) and 0x0FFF

                // Create tags from search terms, icon name and category name
                val tags = mutableSetOf<Tag>()
                faIcon.search.terms.flatMapTo(tags) { getTagsFromString(it) }
                tags += getTagsFromString(faIcon.name)
                tags += catgTags

                // Create an icon for each variant.
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
                    val icon = Icon(id, tags.toMutableList(), pathData, iconSize, iconSize)
                    iconPack.getOrPut(catg) { mutableListOf() } += icon

                    iconToFaIconName[icon] = iconName
                }
            }
        }

        // Assign a category to icons that had multiple categories.
        // Category is assigned by determining tag similarity.
        val tagFreq = getTagsFrequencyByCategory(0)
        val unassigned = iconPack[NO_CATEGORY]
        if (unassigned != null) {
            for (icon in unassigned) {
                // Get candidate categories for the icon.
                val faIconName = iconToFaIconName[icon] ?: error("")
                val catgNames = catgsByIcon[faIconName] ?: error("")
                var candidates: Collection<Category> = catgNames.map {
                    // Candidate may not exist, so create it in that case.
                    categories[it] ?: Category(-1, it.normalizeName(),
                            faCategories[it]?.name ?: NO_CATEGORY.name)
                }
                if (candidates.isEmpty()) {
                    // Icon has no candidate categories, allow all of them.
                    candidates = categories.values
                }

                // Find category with most similar tags
                val catg = candidates.maxBy { candidate ->
                    val freq = tagFreq[candidate] ?: emptyMap()
                    icon.tags.sumByDouble { freq.getOrDefault(it, 0.0) }
                }!!
                iconPack.getOrPut(catg) { mutableListOf() } += icon
            }
            iconPack -= NO_CATEGORY
        }
    }

    /** Reduce the number of categories by moving icons from categories with less icons. */
    private fun reduceCategories() {
        val tagFreq = getTagsFrequencyByCategory(minIconsInCatg)
        val packIterator = iconPack.values.iterator()
        for (catgIcons in packIterator) {
            if (catgIcons.size < minIconsInCatg) {
                // Category doesn't have enough icons to be kept.
                // Move icons to other category with similar tags.
                for (icon in catgIcons) {
                    // Find category with most similar tags
                    val otherCatg = tagFreq.maxBy { (_, freq) ->
                        icon.tags.sumByDouble { freq.getOrDefault(it, 0.0) }
                    }!!.key
                    iconPack[otherCatg]?.add(icon)
                }

                packIterator.remove()
            }
        }
    }

    /** Determine the frequency of each tag in each category that will be kept.
     * Frequency is the number of tag occurences divided by the number of icons in category. */
    private fun getTagsFrequencyByCategory(minSize: Int): Map<Category, Map<Tag, Double>> {
        val tagFreq = mutableMapOf<Category, Map<Tag, Double>>()
        for ((catg, catgIcons) in iconPack) {
            if (catgIcons.size >= minSize && catg != NO_CATEGORY) {
                val freq = mutableMapOf<Tag, Double>()
                tagFreq[catg] = freq

                // Count the number of occurences of each tag
                for (icon in catgIcons) {
                    for (tag in icon.tags) {
                        freq[tag] = freq.getOrDefault(tag, 0.0) + 1.0
                    }
                }

                // Divide the number of occurences by the total number of tags
                for ((tag, n) in freq) {
                    freq[tag] = n / catgIcons.size
                }
            }
        }
        return tagFreq
    }

    /** Add tags derived from category name on icons in it. */
    private fun addCategoryNameToTags() {
        for ((catg, catgIcons) in iconPack) {
            val catgTags = getTagsFromString(catg.nameValue)
            for (icon in catgIcons) {
                val tags = mutableSetOf<Tag>()
                tags += icon.tags
                tags += catgTags
                icon.tags.clear()
                icon.tags += tags
            }
        }
    }

    /** Assign an ID to each category of the icon pack. */
    private fun assignCategoryIds() {
        for ((i, catg) in iconPack.keys.withIndex()) {
            catg.id = i
        }
    }

    private fun getTagsFromString(str: String) = str.splitToSequence('-', '_', ' ', '&').map {
        var name = it
        if (name.endsWith("'s")) name = name.substring(0, name.length - 2)
        Tag(name.normalizeName(), listOf(name.normalizeValue()))
    }.filter {
        it.name.length > 2 && "[a-z]".toRegex() in it.name
    }.toList()

    companion object {
        /** Name of icons excluded from icon pack. */
        private val ICON_BLACKLIST = listOf("font-awesome", "font-awesome-alt",
                "font-awesome-flag", "font-awesome-logo-full")
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
