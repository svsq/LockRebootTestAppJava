package tk.svsq.lockreboottestappjava;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Timer;
import java.util.TimerTask;

import tk.svsq.lockreboottestappjava.admin.AdminReceiver;
import tk.svsq.lockreboottestappjava.databinding.ActivityMainBinding;
import tk.svsq.lockreboottestappjava.ext.Output;
import tk.svsq.lockreboottestappjava.ext.PrefsHelper;
import tk.svsq.lockreboottestappjava.kiosk.KioskUtil;
import tk.svsq.lockreboottestappjava.root.ExecuteAsRootBase;
import tk.svsq.lockreboottestappjava.root.InvokeInterface;
import tk.svsq.lockreboottestappjava.root.RootCommands;

public class MainActivity extends AppCompatActivity {

    public static final Long ENABLE_LOCK_AFTER_OWNER_HAS_BEEN_SET = 5000L;
    public static final Long RELAUNCH_LOCK_MODE_MS = 2000L;

    private final String TAG = MainActivity.class.getSimpleName();
    private boolean mLockIsActive = false;
    private ComponentName admin;
    private PrefsHelper prefsHelper;

    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    public static MainActivity current;
    DevicePolicyManager mDevicePolicyManager;

    private final InvokeInterface mSetAdminListener = new InvokeInterface() {
        @Override
        public void successInvoke() {
            printText("ADMIN OK");
            Output.outputText = binding.tvOutput.getText().toString();
            binding.tvIsOwner.setText(getString(R.string.owner_mode_true));
        }

        @Override
        public void errInvoke() {
            printText("ADMIN NOT OK");
            binding.tvIsOwner.setText(getString(R.string.owner_mode_false));
        }
    };

    private final InvokeInterface mRebootDeviceListener = new InvokeInterface() {
        @Override
        public void successInvoke() {
            printText("Reboot - OK");
        }

        @Override
        public void errInvoke() {
            printText("Reboot - FAIL. Root NOT OK");
        }
    };

    public static IntentFilter getHomeIntentFilter() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        return filter;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        current = this;

        admin = new ComponentName(this, AdminReceiver.class);

        prefsHelper = new PrefsHelper(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

        binding.tvOutput.setText(Output.outputText);

        if (mLockIsActive) {
            binding.btnLockDevice.setText(R.string.title_lock_device);
        } else {
            binding.btnLockDevice.setText(R.string.title_unlock_device);
        }

        binding.btnLockDevice.setOnClickListener(view -> {
            if(mLockIsActive) {
                disableLockMode();
            } else {
                enableLockMode();
            }
        });

        binding.btnRebootDevice.setOnClickListener( view -> {
            try {
                RootCommands.rebootDevice(mRebootDeviceListener);
            } catch (Exception ex) {
                Log.e(TAG, "Reboot - FAIL. Root NOT OK", ex);
                printText("Reboot - FAIL. Root NOT OK");
            }
        });

        binding.btnRemoveOwner.setOnClickListener(view -> {
            if(!mLockIsActive) {
                if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                    mDevicePolicyManager.clearDeviceOwnerApp(getPackageName());
                    mDevicePolicyManager.removeActiveAdmin(admin);
                    printText("Remove admin - OK");
                    Output.outputText = binding.tvOutput.getText().toString();
                    recreate();
                } else {
                    printText("ADMIN ALREADY OFF");
                }
            } else {
                printText("Need UNLOCK first!");
            }
        });

        binding.btnCheckRoot.setOnClickListener(view -> {
            if(ExecuteAsRootBase.canRunRootCommands()) {
                printText("ROOT - OK");
            } else {
                printText("ROOT - NOT OK");
            }
        });

        if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            binding.tvIsOwner.setText(getString(R.string.owner_mode_true));
            binding.btnLockDevice.setText(getString(R.string.title_lock_device));
        } else {
            binding.tvIsOwner.setText(getString(R.string.owner_mode_false));
            binding.btnLockDevice.setText(getString(R.string.title_set_admin));
        }

        viewModel.wifiStatsLiveData.observe(this, data -> {
            if (data) {
                binding.tvWifiStats.setText(getString(R.string.wifi_stats_on));
                binding.tvWifiStats.setTextColor(Color.GREEN);
            } else {
                binding.tvWifiStats.setText(getString(R.string.wifi_stats_off));
                binding.tvWifiStats.setTextColor(Color.RED);
            }
        });

        if (prefsHelper.isLocked) {
            enableLockMode();
        }

        viewModel.subscribeForWifiStats(this);
    }

    Handler handler = new Handler();

    private void enableLockMode() {
        if (!mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            if (ExecuteAsRootBase.canRunRootCommands()) {
                printText("ROOT OK");
                RootCommands.setDeviceOwner(mSetAdminListener);
                handler.postDelayed(() -> {
                    if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                        printText("RETRY");
                        enableLockMode();
                    } else {
                        printText("LOCK FAILED");
                    }
                }, ENABLE_LOCK_AFTER_OWNER_HAS_BEEN_SET);
            } else {
                printText("ROOT NOT OK");
            }
        } else {
            if (!mLockIsActive) {
                mLockIsActive = true;

                final ComponentName customLauncher =
                        new ComponentName(this, MainActivity.class);

                // enable custom launcher
                getPackageManager().setComponentEnabledSetting(
                        customLauncher,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);

                // set custom launcher as default home activity
                mDevicePolicyManager.addPersistentPreferredActivity(admin,
                        getHomeIntentFilter(), customLauncher);

                KioskUtil.setKioskPolicies(mDevicePolicyManager, admin, getPackageName(), true);

                // start lock task mode if it's not already active
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

                if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
                    startLock();
                }

                binding.btnLockDevice.setText(getString(R.string.title_unlock_device));
                prefsHelper.setLocked(true);
            }
        }
    }

    private void disableLockMode() {
        runOnUiThread(() -> {
            printText("UNLOCK");

            if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                if (mLockIsActive) {
                    mLockIsActive = false;
                    stopLockTask();

                    KioskUtil.setKioskPolicies(mDevicePolicyManager, admin, getPackageName(), false);
                    mDevicePolicyManager.clearPackagePersistentPreferredActivities(admin, getPackageName());
                    getPackageManager().setComponentEnabledSetting(
                            new ComponentName(getPackageName(), getClass().getName()),
                            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                            PackageManager.DONT_KILL_APP);

                    binding.btnLockDevice.setText(R.string.title_lock_device);
                    printText("UNLOCK - OK");

                } else {
                    printText("UNLOCK ALREADY OK");
                }

                prefsHelper.setLocked(false);
            }
        });
    }

    private Timer lockTimer = null;

    private void startLock() {
        try {
            startLockTask();
        } catch (IllegalArgumentException e) {
            printText("Cannot start lock, app is not in foreground. Retry in " + RELAUNCH_LOCK_MODE_MS / 1000 + " seconds...");
            lockTimer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    lockTimer.cancel();
                    startLock();
                }
            };
            lockTimer.schedule(timerTask, RELAUNCH_LOCK_MODE_MS, RELAUNCH_LOCK_MODE_MS);
        }
    }

    private void printText(String msg) {
        Log.i(TAG, msg);
        final String out = binding.tvOutput.getText().toString();
        binding.tvOutput.setText(out + "\n" + msg);
        binding.scrollView.scrollTo(0, binding.tvOutput.getHeight());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        current = null;
        viewModel.removeObservers(this);
    }
}
