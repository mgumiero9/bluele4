package com.example.driver;

import android.app.Activity;
import android.util.Log;

/**
 * Created by Gumiero on 25/09/2014.
 */
public class TrainingHelper extends Activity {

    private int TH_HEIGHT = 0 ;
    private int TH_HEART_RATE = 0;
    private int TH_TRAVELED_DISTANCE = 0 ;
    private int TH_WORKOUT_DURATION = 0 ;
    private int TH_CALORIES_BURNED = 0 ;
    private int TH_MAX_HEART_RATE = 0 ;
    private int TH_QTY_STEPS = 0 ;
    private int TH_INSTANT_SPEED = 0 ;
    private int TH_AVG_SPEED = 0 ;
    private int TH_CADENCE_RATCHET = 0 ;
    private int TH_CADENCE_WHEEL = 0 ;

    public Integer getDevicesData() {

        String Str = Integer.toString(R.id.txtParameter);

        if (Str.contains("height")) {


            int TH_HEIGHT = R.id.txtValue;
            System.out.print(TH_HEIGHT);
            Log.v("TH", Integer.toString(TH_HEIGHT));
        }
        return TH_HEIGHT;
    }

  //  public void minuteTimer() {

       // TimerTask() {

         //   public final void wait (60000);

           // public void run() {

//                getDevicesData();

    //        }
      //  };
    //}
}
