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


import android.graphics.Canvas;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Item decoration used to create sticky headers
 * Adapted from answer at https://stackoverflow.com/a/44327350/5288316
 */
class StickyHeaderDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = StickyHeaderDecoration.class.getSimpleName();

    private StickyHeaderImpl stickyHeaderImpl;
    private RecyclerView.ViewHolder headerViewHolder;
    private int stickyHeaderHeight;

    StickyHeaderDecoration(RecyclerView parent, @NonNull StickyHeaderImpl impl) {
        stickyHeaderImpl = impl;

        parent.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                // Intercept click if it's on sticky header, doesn't intercept scrolling event
                return motionEvent.getAction() == MotionEvent.ACTION_DOWN
                        && motionEvent.getY() <= stickyHeaderHeight;
            }

            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {}

            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        stickyHeaderHeight = -1;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        View topChild = parent.getChildAt(0);
        if (topChild == null) {
            // Empty list
            return;
        }

        int topChildPosition = parent.getChildAdapterPosition(topChild);
        if (topChildPosition == RecyclerView.NO_POSITION) {
            // Again, empty list
            return;
        }

        setHeaderViewHolder(topChildPosition, parent);

        View childInContact = getChildInContact(parent, headerViewHolder.itemView);
        if (childInContact != null) {
            int childInContactPos = parent.getChildAdapterPosition(childInContact);
            if (childInContactPos != RecyclerView.NO_POSITION && stickyHeaderImpl.isHeader(childInContactPos)) {
                if (childInContactPos == 0) {
                    // Don't draw pushed header for first header
                    return;
                }

                // If item in contact is another header, draw translated header to create push effect
                c.save();
                c.translate(0, childInContact.getTop() - headerViewHolder.itemView.getHeight());
                headerViewHolder.itemView.draw(c);
                c.restore();
                return;
            }
        }

        // Draw header on top
        c.save();
        c.translate(0, 0);
        headerViewHolder.itemView.draw(c);
        c.restore();
    }

    private void setHeaderViewHolder(int position, View parent) {
        if (headerViewHolder == null) {
            // Header view holder was not yet created
            headerViewHolder = stickyHeaderImpl.onCreateHeaderViewHolder();
        }

        // Bind sticky header view holder data
        int headerPos = stickyHeaderImpl.getHeaderPositionForItem(position);
        stickyHeaderImpl.onBindHeaderViewHolder(headerViewHolder, headerPos);

        if (stickyHeaderHeight == -1) {
            // Measure parent RecyclerView
            int parentWidth = parent.getWidth() + parent.getPaddingStart() + parent.getPaddingEnd();
            int widthSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

            // Measure sticky header
            View header = headerViewHolder.itemView;
            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                    parent.getPaddingLeft() + parent.getPaddingRight(), header.getLayoutParams().width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                    parent.getPaddingTop() + parent.getPaddingBottom(), header.getLayoutParams().height);
            header.measure(childWidthSpec, childHeightSpec);
            stickyHeaderHeight = header.getMeasuredHeight();
            header.layout(0, 0, header.getMeasuredWidth(), stickyHeaderHeight);
        }
    }

    private static View getChildInContact(RecyclerView parent, View with) {
        // Get first child in contact with sticky header
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getTop() > with.getTop() && child.getTop() <= with.getBottom()) {
                // This child overlaps the contact point
                return child;
            }
        }
        return null;
    }

    interface StickyHeaderImpl {
        /**
         * Whether item at position is a header item or not
         * @param position item position
         * @return true if it is
         */
        boolean isHeader(int position);

        /**
         * Get the header for the item at a position
         * @param position item position
         * @return header position for that item
         */
        int getHeaderPositionForItem(int position);

        /**
         * Get the sticky header view holder
         * @return the view holder
         */
        RecyclerView.ViewHolder onCreateHeaderViewHolder();

        /**
         * Bind the sticky header view holder to a header item data
         * @param viewHolder sticky header view holder
         * @param position header item position
         */
        void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int position);
    }
}
