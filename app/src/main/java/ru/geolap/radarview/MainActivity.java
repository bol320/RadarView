package ru.geolap.radarview;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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
        radar.start();
    }

    public void EndAnimation(View view) {
        radar.stop();
    }
}