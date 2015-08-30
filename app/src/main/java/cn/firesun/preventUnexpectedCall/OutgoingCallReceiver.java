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

        NoAccidentApplication application = (NoAccidentApplication) context.getApplicationContext();
        if (!application.getIsChecked()) {
            String phoneNumber = getResultData();
            Intent serviceIntent = new Intent(context, SensorService.class);
            serviceIntent.putExtra("phone", phoneNumber);
            context.startService(serviceIntent);
            setResultData(null);
            abortBroadcast();
        } else {
            application.setIsChecked(false);
        }

    }

}
