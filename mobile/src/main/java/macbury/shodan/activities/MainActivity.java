package macbury.shodan.activities;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import macbury.shodan.R;
import macbury.shodan.sync.DataSync;

public class MainActivity extends AppCompatActivity implements DataSync.Listener {
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

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    this.dataSync = new DataSync(this.getBaseContext(), this);

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

        humidityTextView.setText(dataSync.getMeasurment().getHumidity() + " %");
        temperatureTextView.setText(dataSync.getMeasurment().getTemperature() + " C");
        break;
    }
  }
}
