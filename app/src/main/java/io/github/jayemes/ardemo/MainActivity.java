package io.github.jayemes.ardemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView versionTV = findViewById(R.id.versionTV);

        String date = DateFormat.format("dd-MM HH:mm", BuildConfig.BUILD_TIME).toString();
        versionTV.setText(String.format("AR Demo %s", date));

        Button ArButton = findViewById(R.id.ar_button);
        Button NoArButton = findViewById(R.id.no_ar_button);

        ArButton.setOnClickListener(view -> {
            startActivity(new Intent(this, ArActivity.class));
        });

        NoArButton.setOnClickListener(view -> {
            startActivity(new Intent(this, NoArActivity.class));
        });

    }
}
