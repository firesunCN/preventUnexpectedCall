package cn.firesun.preventUnexpectedCall;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

/**
 * Created by Firesun
 * Email:firesun.cn@gmail.com
 */
public class SensorService extends Service {
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorListener sl;
    private ITelephony iTelephony;
    private Runnable checkRunnable;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Method method = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null,
                    new Object[]{TELEPHONY_SERVICE});
            iTelephony = ITelephony.Stub.asInterface(binder);
        } catch (Exception e) {
        }

        checkRunnable = new Runnable() {
            public void run() {
                float distance = sl.getDistance();
                if (distance >= 0.0 && distance < 5.0f && distance < sensor.getMaximumRange()) {
                    try {
                        iTelephony.endCall();
                        Thread.sleep(50);
                        iTelephony.endCall();
                    } catch (Exception e) {
                    }
                }

                sensorManager.unregisterListener(sl);
                sl = null;
                stopSelf();
            }
        };

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (sensor != null) {
            sl = new SensorListener();
            sensorManager.registerListener(sl, sensor, sensorManager.SENSOR_DELAY_FASTEST);
            Handler handler = new Handler();
            handler.postDelayed(checkRunnable, 20);
        } else {
            sensorManager.unregisterListener(sl);
            sl = null;
            stopSelf();
        }
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
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    }

}
