package ru.geolap.radarview;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    RadarView mRadarView = null;
    private RadarDisplay radar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radar = findViewById(R.id.radar);
        radar.setFrq(50);
        radar.setDeltaDegrees(4.0f);

/*
        mRadarView = (RadarView) findViewById(R.id.radarView);
        mRadarView.setShowCircles(true);
*/
    }

    public void StartAnimation(View view) {
 //       if (mRadarView != null) mRadarView.startAnimation();
        radar.start();
    }

    public void EndAnimation(View view) {
  //      if (mRadarView != null) mRadarView.stopAnimation();
        radar.stop();
    }
}