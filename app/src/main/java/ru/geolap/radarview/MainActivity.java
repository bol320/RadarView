package ru.geolap.radarview;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private RadarDisplay radar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radar = findViewById(R.id.radar);
        radar.setFrq(40);
        radar.setDeltaDegrees(5.0f);
    }

    public void StartAnimation(View view) {
   //     radar.start();

        LoadRadarVideo();
    }

    public void EndAnimation(View view) {
        radar.stop();
    }

    public void LoadRadarVideo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
    //            String urldisplay = "http://vm3.geokaskad.online:29292/latest?resolution=2048";
                String urldisplay = "http://5.17.3.10:9000/latest?resolution=3520";
                Bitmap bmpRadar = null;
                Bitmap.Config cfg;

                try {
                      URL url = new URL(urldisplay);
                      InputStream in = url.openStream();
                      bmpRadar = BitmapFactory.decodeStream(in);

                      Bitmap newRadarVideo = processRadarVideo(bmpRadar);

                   } catch (IOException e) {
                             throw new RuntimeException(e);
                          }
            } // end run
        }).start();
    }

    public static Bitmap processRadarVideo(Bitmap src) {
        float[] matrix = {
                1, 0, 0, 0, 0,      //red
                0, 1, 0, 0, 0,      //green
                0, 0, 1, 0, 0,      //blue
                255, 0, 0, 1, 0     //alpha
        };
        // create output bitmap
        Bitmap mutableBitmap = src.isMutable()
                ? src
                : src.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));

        canvas.drawBitmap(mutableBitmap , 0, 0, paint);

        // return final image
        return mutableBitmap;
    }

    public static void decodeBitmap(Bitmap src) {
        int width  = src.getWidth();
        int height = src.getHeight();

        int A, R, G, B;
        int pixel;
        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                // apply filtering on each channel R, G, B
                A = Color.alpha(pixel);
                R = (int)(Color.red(pixel) );
                G = (int)(Color.green(pixel) );
                B = (int)(Color.blue(pixel));

                if(A > 0 ) {
                    A= 255;
                }
            }
        }


    }
}