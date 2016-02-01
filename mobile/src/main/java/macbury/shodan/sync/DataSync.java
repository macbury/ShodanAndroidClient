package macbury.shodan.sync;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import macbury.shodan.api.StatsService;
import macbury.shodan.models.Humidifier;
import macbury.shodan.models.HumidifierState;
import macbury.shodan.models.Measurement;
import macbury.shodan.models.RefillResult;
import macbury.shodan.models.ShodanStats;
import okhttp3.HttpUrl;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class manages syncing data with server
 */
public class DataSync {
  private static final String TAG = "DataSync";
  private final NsdManager nsdManager;
  private static final String SERVICE_TYPE = "_http._tcp";
  private final static String SERVICE_NAME = "Shodan";
  private final Listener listener;
  private final Context context;
  private NsdServiceInfo serviceToResolve;
  private NsdServiceInfo currentServiceInfo;
  private StatsService api;
  private NsdManager.DiscoveryListener discoveryListener;
  private ShodanStats stats;
  private String deviceUid;
  private HumidifierState humidifierStatus;

  public HumidifierState getHumidifierStatus() {
    return humidifierStatus;
  }


  public enum State implements BasicState<DataSync> {
    PingSuccess {
      @Override
      public void enter(DataSync context) {

      }

      @Override
      public void exit(DataSync context) {

      }
    },

    Ping {
      @Override
      public void enter(DataSync context) {
        final DataSync dataSync = context;
        dataSync.api.ping(dataSync.deviceUid).enqueue(new Callback<HumidifierState>() {
          @Override
          public void onResponse(Response<HumidifierState> response) {
            dataSync.humidifierStatus = response.body();
            dataSync.changeState(PingSuccess);
          }

          @Override
          public void onFailure(Throwable t) {
            Log.e(TAG, "Error on ping!", t);
            dataSync.changeState(SyncError);
          }
        });
      }

      @Override
      public void exit(DataSync context) {

      }
    },

    Refill {
      @Override
      public void enter(DataSync dataSync) {
        final DataSync context = dataSync;
        context.api.refill(context.stats.getHumidifier().getId()).enqueue(new Callback<RefillResult>() {
          @Override
          public void onResponse(Response<RefillResult> response) {
            context.changeState(SyncData);
          }

          @Override
          public void onFailure(Throwable t) {
            Log.e(TAG, "Error on refill", t);
            context.changeState(SyncError);
          }
        });
      }

      @Override
      public void exit(DataSync context) {

      }
    },
    Stopped {
      @Override
      public void enter(DataSync context) {

      }

      @Override
      public void exit(DataSync context) {

      }
    },
    SearchingService {
      @Override
      public void enter(DataSync context) {
        final DataSync dataSync = context;
        context.discoveryListener = new NsdManager.DiscoveryListener() {

          @Override
          public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            dataSync.changeState(State.ServiceSearchError);
          }

          @Override
          public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            dataSync.changeState(State.ServiceSearchError);
          }

          @Override
          public void onDiscoveryStarted(String serviceType) {

          }

          @Override
          public void onDiscoveryStopped(String serviceType) {

          }

          @Override
          public void onServiceFound(NsdServiceInfo serviceInfo) {
            if (SERVICE_NAME.equalsIgnoreCase(serviceInfo.getServiceName())) {
              Log.d(TAG, "Service discovery success" + serviceInfo + " starting resolving ip and port");
              dataSync.serviceToResolve = serviceInfo;
              dataSync.changeState(State.ResolvingIp);
            } else {
              Log.d(TAG, "Unknown Service Type: " + serviceInfo.getServiceType());
            }
          }

          @Override
          public void onServiceLost(NsdServiceInfo serviceInfo) {
            dataSync.changeState(State.ServiceSearchError);
          }
        };
        context.nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, context.discoveryListener);
      }

      @Override
      public void exit(DataSync context) {
        if (context.discoveryListener != null)
          context.nsdManager.stopServiceDiscovery(context.discoveryListener);
        context.discoveryListener = null;
      }
    },
    ServiceSearchError {
      @Override
      public void enter(DataSync context) {

      }

      @Override
      public void exit(DataSync context) {

      }
    },
    ResolvingIp {
      @Override
      public void enter(final DataSync context) {
        context.nsdManager.resolveService(context.serviceToResolve, new NsdManager.ResolveListener() {

          @Override
          public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            context.changeState(ResolveError);
          }

          @Override
          public void onServiceResolved(NsdServiceInfo serviceInfo) {
            context.setCurrentServiceInfo(serviceInfo);

          }
        });
      }

      @Override
      public void exit(DataSync context) {

      }
    },
    ResolveError {
      @Override
      public void enter(DataSync context) {

      }

      @Override
      public void exit(DataSync context) {

      }
    },
    Ready {
      @Override
      public void enter(DataSync context) {

      }

      @Override
      public void exit(DataSync context) {

      }
    },
    SyncData {
      @Override
      public void enter(final DataSync context) {
        context.api.all().enqueue(new Callback<ShodanStats>() {
          @Override
          public void onResponse(Response<ShodanStats> response) {
            context.stats = response.body();
            context.changeState(Ready);
          }

          @Override
          public void onFailure(Throwable t) {
            Log.e(TAG, "Error on sync", t);
            context.changeState(SyncError);
          }
        });
      }

      @Override
      public void exit(DataSync context) {

      }
    },

    SyncError {
      @Override
      public void enter(DataSync context) {

      }

      @Override
      public void exit(DataSync context) {

      }
    };
  }

  public interface Listener {
    public void onChangeState(DataSync dataSync, State currentState);
  }

  private State currentState;

  public DataSync(Context context, Listener listener) {
    this.context    = context;
    this.nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    this.listener   = listener;
    this.changeState(State.SearchingService);
  }

  /**
   * Change state and trigger exit and enter events
   * @param nextState
   */
  private synchronized void changeState(final State nextState) {
    if (currentState != null) {
      Log.i(TAG, "Exiting: " + currentState.toString());
      currentState.exit(this);
    }

    currentState = nextState;
    Log.i(TAG, "Entering: " + currentState.toString());
    nextState.enter(this);


    /**
     * Run listener on main thread
     */
    Handler uiHandler = new Handler(Looper.getMainLooper());
    uiHandler.post(new Runnable() {
      @Override
      public void run() {
        listener.onChangeState(DataSync.this, nextState);
      }
    });
  }

  public Measurement getMeasurment() {
    return stats.getCurrent();
  }

  public void stop() {
    changeState(State.Stopped);
  }

  /**
   * Sets current service information and builds api accessor
   * @param currentServiceInfo
   */
  public void setCurrentServiceInfo(NsdServiceInfo currentServiceInfo) {
    this.currentServiceInfo = currentServiceInfo;
    HttpUrl httpUrl;
    try {
      httpUrl = new HttpUrl.Builder().scheme("http").host(currentServiceInfo.getHost().getHostAddress()).port(currentServiceInfo.getPort()).build();
    } catch (Exception e) {
      changeState(State.SyncError);
      return;
    }

    Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create();

    Log.i(TAG, "Shodan url: " + httpUrl.toString());
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(httpUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();

    this.api = retrofit.create(StatsService.class);

    changeState(State.SyncData);
  }


  public State getCurrentState() {
    return currentState;
  }

  public void refresh() {
    if (currentState == DataSync.State.Ready) {
      changeState(State.SyncData);
    } else {
      changeState(State.SearchingService);
    }
  }

  public Humidifier getHumidifier() {
    return this.stats.getHumidifier();
  }

  public void refill() {
    if (currentState == DataSync.State.Ready) {
      changeState(State.Refill);
    } else {
      changeState(State.SearchingService);
    }
  }

  /**
   * Sends ping with device uid.
   * @param deviceUid
   */
  public void ping(String deviceUid) {
    this.deviceUid = deviceUid;
    changeState(State.Ping);
  }
}
