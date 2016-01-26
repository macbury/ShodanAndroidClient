package macbury.shodan.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import macbury.shodan.service.FindShodanService;

/**
 * This Receiver waits for connection to wifi. Then start {@link macbury.shodan.service.FindShodanService}
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
  private static final String TAG = "NetworkChangeReceiver";

  public NetworkChangeReceiver() {
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Intent shodanServiceIntent = new Intent(context, FindShodanService.class);
    if(intent.getAction().equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)){
      boolean connected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
      if(connected) {
        Log.d(TAG, "Connected to wifi");
        context.startService(shodanServiceIntent);
      } else {
        Log.d(TAG, "Disconnected from wifi");
        context.stopService(shodanServiceIntent);
      }
    } else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
      NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
      if(netInfo.isConnected()) {
        Log.d(TAG, "Connected to wifi");
        context.startService(shodanServiceIntent);
      } else {
        Log.d(TAG, "Disconnected from wifi");
        context.stopService(shodanServiceIntent);
      }
    }
  }
}
