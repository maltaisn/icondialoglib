/*
 * Copyright (c) 2018 Nicolas Maltais
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.maltaisn.icondialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Icon {

    private static final String TAG = Icon.class.getSimpleName();

    static final int LABEL_ID_IGNORE = -1;

    final int id;
    Label[] labels;
    final Category category;

    final byte[] pathData;

    Drawable drawable;

    /**
     * Create new icon
     * @param id ID to assign
     * @param category category object
     * @param labels array of labels
     * @param pathData icon path data, {@link String#getBytes()}
     */
    Icon(int id, Category category, Label[] labels, byte[] pathData) {
        this.id = id;
        this.category = category;
        this.labels = labels;
        this.pathData = pathData;
    }

    /**
     * Get icon's ID
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get icon category.
     * @return the category object
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Get the labels of this icon
     * @return the array of labels
     */
    public Label[] getLabels() {
        return Arrays.copyOf(labels, labels.length);
    }

    /**
     * Get icon's vector drawable
     * @param context any context
     * @return the drawable, or null if drawable couldn't be loaded
     */
    public synchronized Drawable getDrawable(@NonNull Context context) {
        if (drawable == null) {
            byte[] binXml = createBinaryDrawableXml(pathData);
            try {
                // Get the binary XML parser (XmlBlock.Parser) and use it to create the drawable
                // This should be equivalent to AssetManager#getXml()
                @SuppressLint("PrivateApi")
                Class<?> xmlBlock = Class.forName("android.content.res.XmlBlock");
                Constructor xmlBlockConstr = xmlBlock.getConstructor(byte[].class);
                Method xmlParserNew = xmlBlock.getDeclaredMethod("newParser");
                xmlBlockConstr.setAccessible(true);
                xmlParserNew.setAccessible(true);
                XmlPullParser parser = (XmlPullParser) xmlParserNew.invoke(
                        xmlBlockConstr.newInstance((Object) binXml));

                if (Build.VERSION.SDK_INT >= 24) {
                    drawable = Drawable.createFromXml(context.getResources(), parser);
                } else {
                    // Before API 24, vector drawables aren't rendered correctly without compat lib
                    final AttributeSet attrs = Xml.asAttributeSet(parser);
                    int type = parser.next();
                    while (type != XmlPullParser.START_TAG) {
                        type = parser.next();
                    }
                    drawable = VectorDrawableCompat.createFromXmlInner(context.getResources(), parser, attrs, null);
                }

            } catch (Exception e) {
                // Could not load icon
                Log.e(TAG, "Could not create vector drawable for icon " + id, e);
                return null;
            }
        }

        //noinspection ConstantConditions
        return drawable.getConstantState().newDrawable();
    }

    private static final short[] BIN_XML_START = {
            0x03, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x1C, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x44, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x11,
            0x00, 0x00, 0x00, 0x21, 0x00, 0x00, 0x00, 0x32, 0x00, 0x00, 0x00, 0x3E, 0x00, 0x00, 0x00,
            0x49, 0x00, 0x00, 0x00, 0x76, 0x00, 0x00, 0x00, 0x7D, 0x00, 0x00, 0x00, 0x86, 0x00, 0x00,
            0x00, 0x06, 0x06, 0x68, 0x65, 0x69, 0x67, 0x68, 0x74, 0x00, 0x05, 0x05, 0x77, 0x69, 0x64,
            0x74, 0x68, 0x00, 0x0D, 0x0D, 0x76, 0x69, 0x65, 0x77, 0x70, 0x6F, 0x72, 0x74, 0x57, 0x69,
            0x64, 0x74, 0x68, 0x00, 0x0E, 0x0E, 0x76, 0x69, 0x65, 0x77, 0x70, 0x6F, 0x72, 0x74, 0x48,
            0x65, 0x69, 0x67, 0x68, 0x74, 0x00, 0x09, 0x09, 0x66, 0x69, 0x6C, 0x6C, 0x43, 0x6F, 0x6C,
            0x6F, 0x72, 0x00, 0x08, 0x08, 0x70, 0x61, 0x74, 0x68, 0x44, 0x61, 0x74, 0x61, 0x00, 0x2A,
            0x2A, 0x68, 0x74, 0x74, 0x70, 0x3A, 0x2F, 0x2F, 0x73, 0x63, 0x68, 0x65, 0x6D, 0x61, 0x73,
            0x2E, 0x61, 0x6E, 0x64, 0x72, 0x6F, 0x69, 0x64, 0x2E, 0x63, 0x6F, 0x6D, 0x2F, 0x61, 0x70,
            0x6B, 0x2F, 0x72, 0x65, 0x73, 0x2F, 0x61, 0x6E, 0x64, 0x72, 0x6F, 0x69, 0x64, 0x00, 0x04,
            0x04, 0x70, 0x61, 0x74, 0x68, 0x00, 0x06, 0x06, 0x76, 0x65, 0x63, 0x74, 0x6F, 0x72, 0x00
    };

    private static final short[] BIN_XML_END = {
            0x80, 0x01, 0x08, 0x00, 0x20, 0x00, 0x00, 0x00, 0x55, 0x01, 0x01, 0x01, 0x59, 0x01, 0x01,
            0x01, 0x02, 0x04, 0x01, 0x01, 0x03, 0x04, 0x01, 0x01, 0x04, 0x04, 0x01, 0x01, 0x05, 0x04,
            0x01, 0x01, 0x02, 0x01, 0x10, 0x00, 0x74, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x08, 0x00, 0x00, 0x00, 0x14, 0x00, 0x14, 0x00,
            0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x08, 0x00, 0x00, 0x05, 0x01, 0x18, 0x00, 0x00, 0x06, 0x00,
            0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x08, 0x00, 0x00, 0x05, 0x01,
            0x18, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF,
            0x08, 0x00, 0x00, 0x04, 0x00, 0x00, 0xC0, 0x41, 0x06, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00,
            0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x08, 0x00, 0x00, 0x04, 0x00, 0x00, 0xC0, 0x41, 0x02, 0x01,
            0x10, 0x00, 0x4C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0x07, 0x00, 0x00, 0x00, 0x14, 0x00, 0x14, 0x00, 0x02, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF,
            0xFF, 0x08, 0x00, 0x00, 0x1D, 0x00, 0x00, 0x00, 0xFF, 0x06, 0x00, 0x00, 0x00, 0x05, 0x00,
            0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x03, 0x09, 0x00, 0x00, 0x00, 0x03,
            0x01, 0x10, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0x07, 0x00, 0x00, 0x00, 0x03, 0x01, 0x10, 0x00, 0x18, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x08, 0x00,
            0x00, 0x00
    };

    /**
     * Create a vector drawable binary XML from path data so that it can be parsed and created.
     * This is kind of stupid having to write something so that it can immediately be parsed but
     * there is no other solution that avoid using a thousand XML drawables
     * See https://justanapplication.wordpress.com/category/android/android-binary-xml/
     * @param pathData vector path data
     * @return binary XML byte array
     */
    private static byte[] createBinaryDrawableXml(byte[] pathData) {
        int pathSpLength = pathData.length + (pathData.length > 127 ? 5 : 3);
        int spPaddingLength = (BIN_XML_START.length + pathSpLength) % 4;
        if (spPaddingLength != 0) spPaddingLength = 4 - spPaddingLength;
        int totalLength = BIN_XML_START.length + pathSpLength + spPaddingLength + BIN_XML_END.length;

        ByteBuffer bb = ByteBuffer.allocate(totalLength);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        // Write XML chunk header and string pool
        for (short b : BIN_XML_START) {
            bb.put((byte) b);
        }

        // Write XML size and string pool size
        bb.position(4);
        bb.putInt(totalLength);
        bb.position(12);
        bb.putInt((BIN_XML_START.length - 8) + pathSpLength + spPaddingLength);

        bb.position(BIN_XML_START.length);

        // Write path data
        if (pathData.length > 127) {
            byte high = (byte) ((pathData.length & 0xFF00 | 0x8000) >>> 8);
            byte low = (byte) (pathData.length & 0xFF);
            bb.put(high);
            bb.put(low);
            bb.put(high);
            bb.put(low);
        } else {
            byte len = (byte) pathData.length;
            bb.put(len);
            bb.put(len);
        }
        bb.put(pathData);
        bb.put((byte) 0);

        // Padding to align on 32-bit
        if (spPaddingLength > 0) {
            bb.put(new byte[spPaddingLength]);
        }

        // Write XML tag and attributes data
        for (short b : BIN_XML_END) {
            bb.put((byte) b);
        }

        return bb.array();
    }

    @Override
    public String toString() {
        return "[id=" + id + ", category=" + category.id + ", " + labels.length + " labels]";
    }
}
