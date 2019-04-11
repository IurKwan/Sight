package com.iur.rui.sight;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.iur.sight.Sight;
import com.iur.sight.SightRecordActivity;

import java.io.File;

/**
 * @author 关志锐
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button take = findViewById(R.id.button1);
        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SightRecordActivity.class);
                startActivity(intent);
            }
        });


        Button play = findViewById(R.id.button2);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Sight()
                    .path(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Sight" + File.separator)
                    .url("http://www.krbb.cn//yefiles//20190111//91ewyjaLRQ0mISx.mp4")
                    .setHttpManager(new HttpUtil())
                    .setInterface(new GlideLoader())
                    .start(MainActivity.this);
            }
        });
    }
}
