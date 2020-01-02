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


sealed class XmlElement {

    /**
     * Simple representation of a XML tag, with a [name], a map of [attributes] and [children] elements.
     * XML documents can be constructed with a DSL syntax and converted to text with [toXml].
     */
    data class XmlTag(val name: String,
                      val attributes: Map<String, Any?>,
                      val children: List<XmlElement>) : XmlElement() {

        /** DSL constructor using [XmlBuilder]. */
        constructor(name: String, vararg attributes: Pair<String, Any?>,
                    children: XmlBuilder.() -> Unit = {}) :
                this(name, attributes.toMap(), XmlBuilder().apply(children).children)

        fun toXml(indent: Boolean = true) =
                toXml(if (indent) 0 else INDENT_NONE).toString()

        private fun toXml(indent: Int): CharSequence {
            val xml = StringBuilder()
            xml.appendIndent(indent)

            // Start tag and attributes
            xml.append('<')
            xml.append(name)
            for ((name, value) in attributes) {
                if (value != null) {
                    xml.append(' ')
                    xml.append(name)
                    xml.append("=\"")
                    xml.append(value.toString().escapeXml())
                    xml.append('"')
                }
            }

            // Children elements
            if (children.isEmpty()) {
                // Use self-closing tag
                xml.append("/>")
                if (indent != INDENT_NONE) xml.appendln()
            } else {
                // Append children elements
                val isTextOnly = (children.size == 1 && children.first() is XmlText)
                xml.append('>')
                if (!isTextOnly && indent != INDENT_NONE) xml.appendln()
                for (child in children) {
                    xml.append(when (child) {
                        is XmlTag -> child.toXml(
                                if (indent == INDENT_NONE) INDENT_NONE else indent + 1)
                        is XmlText -> child.text.escapeXml()
                    })
                }

                // End tag
                if (!isTextOnly) xml.appendIndent(indent)
                xml.append("</")
                xml.append(name)
                xml.append('>')
                if (indent != INDENT_NONE) xml.appendln()
            }
            return xml
        }

        companion object {
            const val INDENT_NONE = -1
        }

    }

    data class XmlText(val text: String) : XmlElement()
}

@DslMarker
private annotation class XmlDsl

class XmlBuilder {
    val children = mutableListOf<XmlElement>()

    /** Add a text element inside the current tag. */
    fun text(text: String) {
        children += XmlElement.XmlText(text)
    }

    /** Add a child tag inside the current tag. */
    fun tag(name: String, vararg attributes: Pair<String, Any?>,
            @XmlDsl children: XmlBuilder.() -> Unit) {
        this.children += XmlElement.XmlTag(name, *attributes, children = children)
    }
}

private fun StringBuilder.appendIndent(level: Int = 1) = repeat(level) { append(INDENT) }

/**
 * Escape [this] string to be used as a XML attribute value or text.
 * This uses simplified and modified rules but works for the use case here.
 */
private fun String.escapeXml() = this
        .replace("'", "`")  // Special escape used by icon pack parser
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

private const val INDENT = "    "
