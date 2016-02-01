package macbury.shodan.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dd.CircularProgressButton;

import lecho.lib.hellocharts.view.LineChartView;
import macbury.shodan.R;
import macbury.shodan.models.Humidifier;
import macbury.shodan.models.Measurement;
import macbury.shodan.services.PingShodanService;
import macbury.shodan.sync.DataSync;

public class MainActivity extends AppCompatActivity implements DataSync.Listener, View.OnClickListener {
  private static final String TAG = "MainActivity";
  private DataSync dataSync;
  private Toolbar toolbar;
  private NestedScrollView scrollView;
  private View loadingView;
  private FloatingActionButton fab;
  private View errorView;
  private TextView errorTextView;
  private TextView temperatureTextView;
  private TextView humidityTextView;
  private Measurement measurment;
  private LineChartView last24HoursChart;
  private CircularProgressButton refillButton;
  private TextView minimalHumiditiTextView;
  private TextView humdifierNextTickTextView;
  private TextView humidifierLeftFromTotalTextView;
  private TextView humidifierStateTextView;
  private CountDownTimer nextTickCountDown;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    this.scrollView     = (NestedScrollView) findViewById(R.id.scroll_view);
    this.loadingView    = findViewById(R.id.loading_view);
    this.errorView      = findViewById(R.id.error_view);
    this.errorTextView  = (TextView)findViewById(R.id.error_text_view);
    this.fab = (FloatingActionButton) findViewById(R.id.fab);

    this.temperatureTextView  = (TextView)findViewById(R.id.temperature_text_view);
    this.humidityTextView     = (TextView)findViewById(R.id.humidity_text_view);
    this.refillButton         = (CircularProgressButton)findViewById(R.id.button_refill);
    this.minimalHumiditiTextView = (TextView)findViewById(R.id.humidifier_minimal_humidity);
    this.humdifierNextTickTextView       = (TextView)findViewById(R.id.humidifier_next_tick);
    this.humidifierLeftFromTotalTextView = (TextView)findViewById(R.id.humidifier_left_from_total);
    this.humidifierStateTextView         = (TextView)findViewById(R.id.humidifier_state);
    //this.last24HoursChart     = (LineChartView)findViewById(R.id.last_24_hours_chart);
    fab.setOnClickListener(this);

    refillButton.setIndeterminateProgressMode(true);
    refillButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dataSync.refill();
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    this.dataSync = new DataSync(this.getBaseContext(), this);

    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.cancel(PingShodanService.OUT_OF_WATER_NOTIFICATION_ID);

  }

  @Override
  protected void onPause() {
    super.onPause();
    dataSync.stop();
  }

  @Override
  public void onChangeState(DataSync dataSync, DataSync.State currentState) {
    Log.i(TAG, "Current state: " + currentState);
    ActionBar actionBar = getSupportActionBar();
    switch (currentState) {
      case Refill:
        refillButton.setProgress(50);
        fab.setVisibility(View.GONE);

        break;
      case SearchingService:
      case ResolvingIp:
      case SyncData:
        loadingView.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        break;
      case ServiceSearchError:
      case ResolveError:
      case SyncError:
        loadingView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.VISIBLE);

        switch (currentState) {
          case ServiceSearchError:
            errorTextView.setText("Nie znaleziono usługi");
            break;
          case ResolveError:
            errorTextView.setText("Nie uzyskano ip usługi");
            break;
          case SyncError:
            errorTextView.setText("Błąd podczas pobierania danych");
            break;
          default:
            errorTextView.setText("Jakiś nieznany błąd :(");
            break;
        }

        break;
      case Ready:
        loadingView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
        scrollView.scrollTo(0, 0);

        setMeasurment(dataSync.getMeasurment());
        refillButton.setProgress(0);
        setHumidifier(dataSync.getHumidifier());
        break;
    }
  }

  private void setHumidifier(Humidifier humidifier) {
    minimalHumiditiTextView.setText(Math.round(humidifier.getMinHumidity()) + "%");
    humidifierLeftFromTotalTextView.setText(
        humidifier.getLeft() + "/" + humidifier.getTotal()
    );
    humidifierStateTextView.setText(humidifier.getState());

    if (nextTickCountDown != null)
      nextTickCountDown.cancel();

    this.nextTickCountDown = new CountDownTimer(humidifier.getNextUpdateIn() * 1000, 1000) {

      public void onTick(long millisUntilFinished) {
        int duration = (int)(millisUntilFinished / 1000);
        int hours = duration/3600;
        int minutes = (duration%3600)/60;
        int seconds = duration%60;
        humdifierNextTickTextView.setText("("+ hours + ":" + minutes + ":" + seconds +")");
      }

      public void onFinish() {
        humdifierNextTickTextView.setText("");
      }
    };

    nextTickCountDown.start();
  }

  public void setMeasurment(Measurement measurment) {
    this.measurment = measurment;
    humidityTextView.setText(measurment.getHumidity() + " %");
    temperatureTextView.setText(measurment.getTemperature() + " C");
  }

  @Override
  public void onClick(View v) {
    dataSync.refresh();
  }
}
