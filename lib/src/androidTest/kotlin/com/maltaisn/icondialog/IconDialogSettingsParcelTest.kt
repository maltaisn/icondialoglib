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

package com.maltaisn.icondialog

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.test.R
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals


@RunWith(AndroidJUnit4::class)
internal class IconDialogSettingsParcelTest {

    @Test
    fun parcelTest() {
        val settings1 = IconDialogSettings {
            searchVisibility = IconDialog.SearchVisibility.NEVER
            titleVisibility = IconDialog.TitleVisibility.NEVER
            headersVisibility = IconDialog.HeadersVisibility.HIDE
            dialogTitle = R.string.custom_title
            maxSelection = 10
            showMaxSelectionMessage = true
            showClearBtn = false
            showSelectBtn = false
        }

        // Write
        val parcel = Parcel.obtain()
        settings1.writeToParcel(parcel, settings1.describeContents())
        parcel.setDataPosition(0)

        // Read
        val settings2 = IconDialogSettings.CREATOR.createFromParcel(parcel)
        assertEquals(settings1.searchVisibility, settings2.searchVisibility)
        assertEquals(settings1.titleVisibility, settings2.titleVisibility)
        assertEquals(settings1.headersVisibility, settings2.headersVisibility)
        assertEquals(settings1.dialogTitle, settings2.dialogTitle)
        assertEquals(settings1.maxSelection, settings2.maxSelection)
        assertEquals(settings1.showMaxSelectionMessage, settings2.showMaxSelectionMessage)
        assertEquals(settings1.showClearBtn, settings2.showClearBtn)
        assertEquals(settings1.showSelectBtn, settings2.showSelectBtn)
    }

}
