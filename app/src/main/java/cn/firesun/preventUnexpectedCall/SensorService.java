package cn.firesun.preventUnexpectedCall;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

/**
 * Created by Firesun
 * Email:firesun.cn@gmail.com
 */
public class SensorService extends Service {
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorListener sl;
    private Runnable checkRunnable;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        final NoAccidentApplication application = (NoAccidentApplication) this.getApplicationContext();

        if (!application.getIsChecked()) {


            checkRunnable = new Runnable() {
                public void run() {
                    float distance = sl.getDistance();
                    if (distance >= 0.0 && distance < 5.0f && distance < sensor.getMaximumRange()) {
                        application.setIsChecked(false);
                    } else {
                        Uri uri = Uri.parse("tel:" + intent.getStringExtra("phone"));
                        Intent callIntent = new Intent(Intent.ACTION_CALL, uri);
                        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(callIntent);
                        application.setIsChecked(true);
                    }

                    sensorManager.unregisterListener(sl);
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
                stopSelf();
            }


        }

        return START_NOT_STICKY;
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
