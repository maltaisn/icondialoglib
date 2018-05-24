package com.maltaisn.icondialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class IconView extends AppCompatImageView {

    public IconView(Context context) {
        super(context);
    }

    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setIcon(@NonNull Icon icon) {
        setImageDrawable(icon.getDrawable(getContext()));
    }
}
