package cn.firesun.preventUnexpectedCall;

import android.app.Application;

/**
 * Created by Firesun
 * Email:firesun.cn@gmail.com
 */
public class NoAccidentApplication extends Application {
    private boolean isChecked = false;

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
