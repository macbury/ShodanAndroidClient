package macbury.shodan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import macbury.shodan.services.PingShodanService;

public class AlarmPingReceiver extends BroadcastReceiver {
  private static final String TAG = "AlarmPingReceiver";

  public AlarmPingReceiver() {
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "Recived!");
    Intent shodanServiceIntent = new Intent(context, PingShodanService.class);
    context.startService(shodanServiceIntent);
  }
}
