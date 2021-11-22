package tk.svsq.lockreboottestappjava.root;

import android.util.Log;

public class RootCommands {

    public static final String TAG = RootCommands.class.getSimpleName();

    private static InvokeInterface invokeInterface = null;

    private static final String[] ROOT_COMMAND_REBOOT_DEVICE = {"su", "0", "reboot" };
    private static final String[] ROOT_SET_DEVICE_OWNER = {"su", "0", "dpm", "set-device-owner", "tk.svsq.lockreboottestappjava/.admin.AdminReceiver"};

    private static void runRootCommand(
            String[] cmds,
            String okMsg,
            String errMsg)
    {
        if(ExecuteAsRootBase.canRunRootCommands()) {
            try {
                Process process = Runtime.getRuntime().exec(cmds);
                process.waitFor();
                Log.i(TAG, okMsg);
                invokeInterface.successInvoke();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, errMsg);
                invokeInterface.errInvoke();
            }
        } else {
            Log.e(TAG, "Root - NOT OK");
            invokeInterface.errInvoke();
        }
    }

    public static void setDeviceOwner(InvokeInterface listener) {
        invokeInterface = listener;
        runRootCommand(ROOT_SET_DEVICE_OWNER, "Admin - OK", "Admin - NOT OK");
    }

    public static void rebootDevice(InvokeInterface listener) {
        invokeInterface = listener;
        runRootCommand(ROOT_COMMAND_REBOOT_DEVICE, "Reboot - OK", "Reboot - NOT OK");
    }
}
