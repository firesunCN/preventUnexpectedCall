package cn.firesun.preventUnexpectedCall;

import android.app.Application;

/**
 * Created by Firesun
 * Email:firesun.cn@gmail.com
 */
public class NoAccidentApplication extends Application {
    private boolean hasCheckedThisCall = false;

    public boolean getHasCheckedThisCall() {
        return hasCheckedThisCall;
    }

    public void setHasCheckedThisCall(boolean isChecked) {
        this.hasCheckedThisCall = isChecked;
    }
}
