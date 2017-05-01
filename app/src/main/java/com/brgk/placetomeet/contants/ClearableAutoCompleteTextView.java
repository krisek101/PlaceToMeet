package com.brgk.placetomeet.contants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ClearableAutoCompleteTextView extends android.support.v7.widget.AppCompatAutoCompleteTextView
        implements TextWatcher {

    public ClearableAutoCompleteTextView(Context context) {
        super(context);
    }

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs,
                                         int defStyle) {
        super(context, attrs, defStyle);
    }

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Drawable clearButton = null;

    public void setClearButton(Drawable clearButton) {
        Bitmap bitmap = ((BitmapDrawable) clearButton).getBitmap();
        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 25, 25, true));
        this.clearButton = d;
        final ClearableAutoCompleteTextView _this = this;

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (_this.getCompoundDrawables()[2] == null) {
                    return false;
                }
                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                if (event.getX() > _this.getWidth() - _this.getPaddingRight()
                        - _this.clearButton.getIntrinsicWidth()) {
                    _this.setText("");
                    _this.setCompoundDrawables(null, null, null, null);
                }
                return false;
            }

        });

        this.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            // Set the bounds of the clear button
            this.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    clearButton, null);
        } else {
            this.setCompoundDrawables(null, null, null, null);
        }
    }

}