package com.anis.greenindoorar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import static androidx.constraintlayout.motion.widget.MotionScene.TAG;

public class SnakePlantActivityActivity extends AppCompatActivity {
    ImageView backButton;
    Button viewAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake_plant_activity);

        backButton = findViewById(R.id.backImg);
        viewAR = findViewById(R.id.viewBtn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), MainActivity.class);
                startActivity(i);
            }
        });


        viewAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(v.getContext(), ViewSnakePlantActivity.class);
//                startActivity(i);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("viewsnackplant:*/")));
            }
        });
        Log.d(TAG, "check data:"+ viewAR);


    }
}
