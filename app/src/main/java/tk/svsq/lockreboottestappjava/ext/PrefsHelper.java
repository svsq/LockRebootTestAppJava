package tk.svsq.lockreboottestappjava.ext;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class PrefsHelper {

    Context context;

    private final SharedPreferences sharedPreferences;

    private static final String IS_LOCKED_KEY = "IS_LOCKED";
    private static final String PREFS_KEY = "LockRebootTestApp";
    public boolean isLocked;
    public PrefsHelper(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        isLocked = getLocked();
    }

    public void setLocked(boolean value) {
        isLocked = value;
        sharedPreferences
                .edit()
                .putString(PREFS_KEY, new Gson().toJson(value))
                .apply();
    }

    private boolean getLocked() {
        isLocked = Boolean.parseBoolean(sharedPreferences.getString(PREFS_KEY, IS_LOCKED_KEY));
        return isLocked;
    }
}
