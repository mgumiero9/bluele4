package com.example.driver;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;

public class loadChronometer extends Activity implements OnClickListener {

    private android.widget.Chronometer chronometer;
    private String format;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chronometer);
        chronometer = (android.widget.Chronometer) findViewById(R.id.chronometer);
        findViewById(R.id.start_button).setOnClickListener(this);
        findViewById(R.id.stop_button).setOnClickListener(this);

        format = "%tH:%tM:%tS";
        chronometer.setFormat(format);

    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.start_button:
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                break;
            case R.id.stop_button:
                chronometer.stop();
                break;
        }
    }

}

