package com.brgk.placetomeet;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

/**
 * TODO: document your custom view class.
 */
public class RightSliderItem extends LinearLayout {
    private String address = "NOT INSERTED";
    private int number = -1;

    private Marker marker = null;

    TextView numberView;
    TextView addressView;

    Context context;

    public RightSliderItem(Context context) {
        super(context);
        this.context = context;

        init();
    }

    public RightSliderItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public RightSliderItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LinearLayout v = (LinearLayout) inflate(context, R.layout.right_slider_item, this);

        numberView = (TextView) v.findViewById(R.id.right_slider_item_number);
        addressView = (TextView) v.findViewById(R.id.right_slider_item_address);

        numberView.setText(number + "");
        addressView.setText(address);
    }


    @SuppressWarnings("unused")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        addressView.setText(address);
    }

    @SuppressWarnings("unused")
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
        numberView.setText(number+"");
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}
