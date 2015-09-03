package cn.firesun.preventUnexpectedCall;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;


/**
 * Created by Firesun
 * Email:firesun.cn@gmail.com
 */
public class CallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //ignore video call
        Bundle bundle = intent.getExtras();
        if ((bundle != null) && (bundle.getInt("android.phone.extra.calltype") == 2))
            return;

        //load config
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        //outgoing call
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            if (sharedPref.getBoolean("enable_outgoing_checkbox", true)) {
                switch (Integer.valueOf(sharedPref.getString("outgoing_prevent_method_list", "1"))) {
                    case 1:
                        NoAccidentApplication application = (NoAccidentApplication) context.getApplicationContext();
                        if (!application.getIsChecked()) {
                            String phoneNumber = getResultData();
                            Intent serviceIntent = new Intent(context, OutGoingCallService.class);
                            serviceIntent.putExtra("phone", phoneNumber);
                            context.startService(serviceIntent);
                            setResultData(null);
                            abortBroadcast();
                        } else {
                            application.setIsChecked(false);
                        }
                        break;
                    case 2:
                        Intent serviceIntent = new Intent(context, OutGoingCallService.class);
                        serviceIntent.putExtra("phone", getResultData());
                        context.startService(serviceIntent);
                        break;
                }
            }
        }
        //incoming call
        else {
            if (sharedPref.getBoolean("enable_incoming_checkbox", true)) {
                TelephonyManager tManager =
                        (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
                Intent serviceIntent = new Intent(context, InComingCallService.class);
                switch (tManager.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        context.startService(serviceIntent);
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        context.stopService(serviceIntent);
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        context.stopService(serviceIntent);
                        break;
                }
            }
        }
    }
}
