package com.brgk.placetomeet.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.brgk.placetomeet.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

public class FullScreenImage extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.full_size_image);

        byte[] bytes = getIntent().getByteArrayExtra("imagebitmap");
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        PhotoView photoView = (PhotoView) findViewById(R.id.imgDisplay);

        photoView.setImageBitmap(bmp);

        showActionBar();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void showActionBar() {
        ActionBar mActionBar = getSupportActionBar();
        if(mActionBar != null) {
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
        }
    }
}