package cn.firesun.preventUnexpectedCall;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;


/**
 * Created by Firesun
 * Email:firesun.cn@gmail.com
 */
public class InComingCallService extends Service {
    RelativeLayout mRelativeLayout;
    WindowManager.LayoutParams wmParams;
    WindowManager mWindowManager;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorListener sl;
    private Runnable checkRunnable;
    private boolean detectEnable = false;
    private int delay;
    private int preventMethod;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        delay = Integer.valueOf(sharedPref.getString("delay_text", "20"));
        preventMethod = Integer.valueOf(sharedPref.getString("incoming_prevent_method_list", "1"));

        createFloatView();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showLockScreen() {
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        wmParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.flags|=LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        mRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.activity_lock_screen, null);
        mWindowManager.addView(mRelativeLayout, wmParams);
    }


    private void createFloatView() {

        switch (preventMethod) {
            case 1:
                checkRunnable = new Runnable() {
                    public void run() {
                        detectEnable = true;
                        float distance = sl.getDistance();
                        if (distance >= 0.0 && distance < 5.0f && distance < sensor.getMaximumRange()) {
                            showLockScreen();
                        } else {
                            stopSelf();
                        }
                    }
                };

                sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                if (sensor != null) {
                    sl = new SensorListener();
                    sensorManager.registerListener(sl, sensor, sensorManager.SENSOR_DELAY_NORMAL);
                    Handler handler = new Handler();
                    handler.postDelayed(checkRunnable, delay);
                } else {
                    stopSelf();
                }
                break;

            case 2:
                showLockScreen();
                checkRunnable = new Runnable() {
                    public void run() {
                        detectEnable = true;
                        float distance = sl.getDistance();
                        if (!(distance >= 0.0 && distance < 5.0f && distance < sensor.getMaximumRange())) {
                            stopSelf();
                        }
                    }
                };

                sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                if (sensor != null) {
                    sl = new SensorListener();
                    sensorManager.registerListener(sl, sensor, sensorManager.SENSOR_DELAY_NORMAL);
                    Handler handler = new Handler();
                    handler.postDelayed(checkRunnable, delay);
                } else {
                    stopSelf();
                }
                break;
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sl != null) {
            sensorManager.unregisterListener(sl);
        }

        if (mRelativeLayout != null) {
            mWindowManager.removeView(mRelativeLayout);
        }
        detectEnable = false;
    }


    private class SensorListener implements SensorEventListener {
        private float distance = -1;

        public float getDistance() {
            return distance;
        }

        public void setDistance(float distance) {
            this.distance = distance;
        }

        public void onSensorChanged(SensorEvent event) {

            float distance = event.values[0];
            setDistance(distance);
            if (detectEnable) {
                if (!(distance >= 0.0 && distance < 5.0f && distance < sensor.getMaximumRange())) {
                    stopSelf();
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    }

}