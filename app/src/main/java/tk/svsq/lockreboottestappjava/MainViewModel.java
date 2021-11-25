package tk.svsq.lockreboottestappjava;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/*public class MainViewModel extends ViewModel {

    MutableLiveData<Boolean> wifiStatsLiveData = new MutableLiveData<>();

    public void subscribeForWifiStats(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        manager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                wifiStatsLiveData.postValue(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                wifiStatsLiveData.postValue(false);
            }
        });
    }

    public void removeObservers(LifecycleOwner owner) {
        wifiStatsLiveData.removeObservers(owner);
    }
}*/
