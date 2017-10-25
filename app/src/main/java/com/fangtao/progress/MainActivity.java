package com.fangtao.progress;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WaveProgressView waveProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        waveProgressView = (WaveProgressView) findViewById(R.id.waveProgressView);
        waveProgressView.setProgressNum(80,3000);

    }
}
