package cn.firesun.preventUnexpectedCall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Firesun
 * Email:firesun.cn@gmail.com
 */
public class OutgoingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, SensorService.class);
        context.startService(serviceIntent);
    }

}
