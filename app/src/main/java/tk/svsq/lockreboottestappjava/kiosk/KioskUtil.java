package tk.svsq.lockreboottestappjava.kiosk;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.UserManager;

public class KioskUtil {

    private static final String TAG = "KioskUtil";

    private static final String[] KIOSK_USER_RESTRICTIONS = {
            UserManager.DISALLOW_SAFE_BOOT,
            UserManager.DISALLOW_FACTORY_RESET,
            UserManager.DISALLOW_ADD_USER,
            UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA };

    public static void setKioskPolicies(DevicePolicyManager policyManager,
                                        ComponentName adminComponentName, String pkgName, Boolean isActive) {
        for (String r : KIOSK_USER_RESTRICTIONS) {
            setUserRestriction(policyManager, adminComponentName, r, isActive);
        }

        policyManager.setLockTaskPackages(adminComponentName, new String[]{pkgName});
    }

    public static void setUserRestriction(DevicePolicyManager policyManager, ComponentName adminComponentName,
                                          String restriction, Boolean disallow) {
        if (disallow) {
            policyManager.addUserRestriction(adminComponentName, restriction);
        } else {
            policyManager.clearUserRestriction(adminComponentName, restriction);
        }
    }
}
