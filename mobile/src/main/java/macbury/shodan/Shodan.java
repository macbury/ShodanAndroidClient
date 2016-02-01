package macbury.shodan;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import macbury.shodan.receivers.AlarmPingReceiver;
import macbury.shodan.services.PingShodanService;


/**
 * Main shodan application center
 */
public class Shodan extends Application {
  private static final String TAG = "Shodan";
  private AlarmManager alarmManager;

  @Override
  public void onCreate() {
    super.onCreate();
    configureAlarms();
  }

  private void configureAlarms() {
    Log.i(TAG, "Setup alarms");

    Intent intent = new Intent(this, AlarmPingReceiver.class);
    PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

    this.alarmManager     = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        0,
        AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
  }
}
