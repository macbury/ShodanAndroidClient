package macbury.shodan.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import macbury.shodan.R;
import macbury.shodan.activities.MainActivity;
import macbury.shodan.sync.DataSync;
import macbury.shodan.utils.Installation;

/**
 * This service pings current device uid, and gets status for humidifier. If state of humidifier is out of water then displays notification about it!
 */
public class PingShodanService extends Service implements DataSync.Listener {
  private static final String TAG           = "PingShodanService";
  private static final String WAKE_LOCK_TAG = "PingShodanService";
  public static final int OUT_OF_WATER_NOTIFICATION_ID = 666;
  private DataSync dataSync;
  private PowerManager.WakeLock wakeLock;
  private PowerManager powerManager;

  public PingShodanService() {
  }

  @Override
  public IBinder onBind(Intent intent) {
    // TODO: Return the communication channel to the service.
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    if (mWifi != null && mWifi.isConnected()) {
      Log.i(TAG, "Wifi is connected, doing rest");
      this.powerManager = (PowerManager) getSystemService(POWER_SERVICE);
      this.wakeLock     = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
      this.wakeLock.acquire();
      this.dataSync = new DataSync(this.getBaseContext(), this);
    } else {
      Log.i(TAG, "No wifi..., stopping service");
      stopSelf();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    wakeLock.release();
    dataSync.stop();
    Log.i(TAG, "Service destroyed");
  }

  private String getDeviceUid() {
    return Installation.id(getBaseContext());
  }

  @Override
  public void onChangeState(DataSync dataSync, DataSync.State currentState) {
    switch (currentState) {
      case ServiceSearchError:
      case ResolveError:
      case SyncError:
        Log.e(TAG, "Got sync network! Maybe there is no shodan here?");
        stopSelf();

        break;
      case Ready:
        dataSync.ping(getDeviceUid());
        break;

      case PingSuccess:
        Log.i(TAG, "Got all information!");
        if (dataSync.getHumidifierStatus().isOutOfWater()) {
          notifyUserAboutNoWater();
        }
        stopSelf();
        break;
    }
  }

  private void notifyUserAboutNoWater() {
    NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
    PendingIntent pendingIntent =  PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_refill)
        .setContentTitle("Humidifier")
        .setContentText("Refill water!!!!");
    builder.setContentIntent(pendingIntent);
    builder.setAutoCancel(true);
    builder.setOnlyAlertOnce(true);
    builder.setPriority(NotificationCompat.PRIORITY_MAX);
    builder.setVibrate(new long[]{2000});
    builder.setLights(Color.BLUE, 3000, 500);

    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    builder.setSound(alarmSound);

    manager.notify(OUT_OF_WATER_NOTIFICATION_ID, builder.build());
  }
}
