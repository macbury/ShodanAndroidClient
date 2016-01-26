package macbury.shodan.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.util.Log;

/**
 * This service uses zero conf to find service
 */
public class FindShodanService extends Service implements NsdManager.DiscoveryListener {
  private static final String TAG = "FindShodanService";
  private NsdManager mNsdManager;
  private final static String SERVICE_NAME = "Shodan";
  public int port; //TODO save it into application instance!!!!
  public String host;

  public FindShodanService() {
  }

  @Override
  public IBinder onBind(Intent intent) {
    // TODO: Return the communication channel to the service.
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "Created");
    this.mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
    mNsdManager.discoverServices("_http._tcp", NsdManager.PROTOCOL_DNS_SD, this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "Destroy!");
    mNsdManager.stopServiceDiscovery(this);
  }

  @Override
  public void onStartDiscoveryFailed(String serviceType, int errorCode) {
    Log.d(TAG, "Failed to start discovery: " + errorCode);
    stopSelf();
  }

  @Override
  public void onStopDiscoveryFailed(String serviceType, int errorCode) {
    Log.d(TAG, "Failed to stop discovery: " + errorCode);
    stopSelf();
  }

  @Override
  public void onDiscoveryStarted(String serviceType) {
    Log.d(TAG, "Searching service...");
  }

  @Override
  public void onDiscoveryStopped(String serviceType) {
    Log.d(TAG, "Stop search service...");
  }

  @Override
  public void onServiceFound(final NsdServiceInfo service) {
    if (SERVICE_NAME.equalsIgnoreCase(service.getServiceName())) {
      Log.d(TAG, "Service discovery success" + service + " starting resolving ip and port");

      mNsdManager.resolveService(service, new NsdManager.ResolveListener() {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
          Log.d(TAG, "Resolve failsed!");
          stopSelf();
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
          Log.d(TAG, "Found service host and port: " + serviceInfo);
          FindShodanService.this.host = serviceInfo.getHost().getHostAddress();
          FindShodanService.this.port = serviceInfo.getPort();
          Log.d(TAG, "Host: " + FindShodanService.this.host);
          Log.d(TAG, "Port: " + FindShodanService.this.port);
          stopSelf();
        }
      });
    } else {
      Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
    }

  }

  @Override
  public void onServiceLost(NsdServiceInfo serviceInfo) {
    Log.d(TAG, "Service lost: " + serviceInfo);
    stopSelf();
  }
}
