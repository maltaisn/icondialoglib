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

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener


/**
 * Item decoration used to create sticky headers.
 * Adapted from answer at [https://stackoverflow.com/a/44327350/5288316].
 */
internal class StickyHeaderDecoration(
        parent: RecyclerView,
        private val callback: Callback,
        private val stickyHeaderViewType: Int
) : ItemDecoration(), OnItemTouchListener {

    private var headerViewHolder: RecyclerView.ViewHolder? = null
    private var stickyHeaderHeight: Int = -1

    init {
        parent.addOnItemTouchListener(this)
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)

        val topChild = parent.getChildAt(0) ?: return  // Return if list is empty.

        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) return  // Again, empty list.

        val viewHolder = setHeaderViewHolder(topChildPosition, parent)
        val childInContact = getChildInContact(parent, viewHolder.itemView)
        if (childInContact != null) {
            val childInContactPos = parent.getChildAdapterPosition(childInContact)
            if (childInContactPos != RecyclerView.NO_POSITION && callback.isHeader(childInContactPos)) {
                if (childInContactPos == 0) {
                    // Don't draw pushed header for first header.
                    return
                }

                // If item in contact is another header, draw translated header to create push effect.
                canvas.save()
                canvas.translate(0f, childInContact.top - viewHolder.itemView.height.toFloat())
                viewHolder.itemView.draw(canvas)
                canvas.restore()
                return
            }
        }

        // Draw header on top
        canvas.save()
        canvas.translate(0f, 0f)
        viewHolder.itemView.draw(canvas)
        canvas.restore()
    }

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) =
            // Intercept click if it's on sticky header, doesn't intercept scrolling event
            (motionEvent.action == MotionEvent.ACTION_DOWN && motionEvent.y <= stickyHeaderHeight)

    override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) = Unit

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit

    private fun setHeaderViewHolder(position: Int, parent: RecyclerView): RecyclerView.ViewHolder {
        var viewHolder = headerViewHolder
        if (viewHolder == null) {
            // Header view holder was not yet created
            viewHolder = callback.onCreateViewHolder(parent, stickyHeaderViewType)
            headerViewHolder = viewHolder
        }

        // Bind sticky header view holder data
        val headerPos = callback.getHeaderPositionForItem(position)
        callback.onBindViewHolder(viewHolder, headerPos)
        if (stickyHeaderHeight == -1) {
            // Measure parent RecyclerView
            val parentWidth = parent.width + parent.paddingLeft + parent.paddingRight
            val widthSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

            // Measure sticky header
            val header = viewHolder.itemView
            val childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                    parent.paddingLeft + parent.paddingRight, header.layoutParams.width)
            val childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                    parent.paddingTop + parent.paddingBottom, header.layoutParams.height)
            header.measure(childWidthSpec, childHeightSpec)
            stickyHeaderHeight = header.measuredHeight
            header.layout(0, 0, header.measuredWidth, stickyHeaderHeight)
        }

        return viewHolder
    }

    private fun getChildInContact(parent: RecyclerView, with: View): View? {
        // Get first child in contact with sticky header
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.top > with.top && child.top <= with.bottom) {
                // This child overlaps the contact point
                return child
            }
        }
        return null
    }

    interface Callback {
        /** Whether item at [pos] is a header item or not. */
        fun isHeader(pos: Int): Boolean

        /** Get the header position for the item at a [pos]. */
        fun getHeaderPositionForItem(pos: Int): Int

        /** Get the sticky header view holder. */
        fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

        /** Bind the sticky header view [holder] to a header item data at [pos]. */
        fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int)
    }

}
