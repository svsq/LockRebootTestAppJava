package tk.svsq.lockreboottestappjava;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.getActivity;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.makeMainActivity;
import static java.lang.System.currentTimeMillis;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    public static final String TAG = BootReceiver.class.getSimpleName();
    public static final String ACTION_BOOT_LAUNCH = "dwall.online.intent.action.BOOT_LAUNCH";
    public static final String ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";
    public static final int BOOT_CODE = 2021;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.i(TAG, "Boot intent has just been received: " + action);
        if (action == null || action.equals("")) return;

        final AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (manager == null) {
            Log.e(TAG, "Unable to obtain alarm manager");
            return;
        }

        final ComponentName component = new ComponentName(context, MainActivity.class);
        final Intent launch = makeMainActivity(component)
                .setAction(ACTION_BOOT_LAUNCH)
                .addFlags(FLAG_ACTIVITY_NEW_TASK);

        final PendingIntent pending = getActivity(context, BOOT_CODE, launch, FLAG_IMMUTABLE + FLAG_ONE_SHOT);
        switch (action) {
            case ACTION_BOOT_COMPLETED: case ACTION_QUICKBOOT_POWERON:
                manager.set(RTC_WAKEUP, currentTimeMillis(), pending);
        }
    }
}
