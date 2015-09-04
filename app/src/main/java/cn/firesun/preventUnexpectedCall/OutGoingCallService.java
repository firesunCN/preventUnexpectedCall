package cn.firesun.preventUnexpectedCall;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

/**
 * Created by Firesun
 * Email:firesun.cn@gmail.com
 */
public class OutGoingCallService extends Service {
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorListener sl;
    private Runnable checkRunnable;
    private int delay;
    private boolean isNotify;
    private int preventMethod;

    private ITelephony iTelephony;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        delay = Integer.valueOf(sharedPref.getString("delay_text", "20"));
        isNotify = sharedPref.getBoolean("enable_notification_checkbox", true);
        preventMethod = Integer.valueOf(sharedPref.getString("outgoing_prevent_method_list", "1"));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(sl);
    }


    private void notifyUser(String phoneNumber) {
        if (isNotify) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(getApplicationContext()).setTicker("显示于屏幕顶端状态栏的文本")
                    .setSmallIcon(R.mipmap.ic_launcher);
            Notification note = builder.setAutoCancel(true).setContentTitle("拦截误拨").setContentText(phoneNumber).build();
            nm.notify(1, note);
        }

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        switch (preventMethod) {
            //method 1
            case 1:
                final NoAccidentApplication application = (NoAccidentApplication) this.getApplicationContext();

                if (!application.getIsChecked()) {
                    checkRunnable = new Runnable() {
                        public void run() {
                            float distance = sl.getDistance();
                            if (distance >= 0.0 && distance < 5.0f && distance < sensor.getMaximumRange()) {
                                application.setIsChecked(false);
                                notifyUser(intent.getStringExtra("phone"));
                            } else {
                                Uri uri = Uri.parse("tel:" + intent.getStringExtra("phone"));
                                Intent callIntent = new Intent(Intent.ACTION_CALL, uri);
                                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(callIntent);
                                application.setIsChecked(true);
                            }
                            stopSelf();
                        }
                    };

                    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                    if (sensor != null) {
                        sl = new SensorListener();
                        sensorManager.registerListener(sl, sensor, sensorManager.SENSOR_DELAY_FASTEST);
                        Handler handler = new Handler();
                        handler.postDelayed(checkRunnable, delay);
                    } else {
                        stopSelf();
                    }

                }
                break;

            case 2:

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
                                //try to endCall
                                for (int i = 0; i < 20; i++) {
                                    if (iTelephony.endCall())
                                        break;
                                    Thread.sleep(50);
                                }
                                notifyUser(intent.getStringExtra("phone"));
                            } catch (Exception e) {
                            }
                        }

                        stopSelf();
                    }
                };

                sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                if (sensor != null) {
                    sl = new SensorListener();
                    sensorManager.registerListener(sl, sensor, sensorManager.SENSOR_DELAY_FASTEST);
                    Handler handler = new Handler();
                    handler.postDelayed(checkRunnable, delay);
                } else {

                    stopSelf();
                }

                break;
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
