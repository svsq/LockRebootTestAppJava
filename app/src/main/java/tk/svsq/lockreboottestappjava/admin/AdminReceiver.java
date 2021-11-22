package tk.svsq.lockreboottestappjava.admin;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import tk.svsq.lockreboottestappjava.MainActivity;

public class AdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        Log.w("Admin", "onReceive");
    }

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
        Log.w("Admin", "onEnabled");
        if (MainActivity.current != null) {
            MainActivity.current.recreate();
        }
    }
}