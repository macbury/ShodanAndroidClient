package macbury.shodan.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import macbury.shodan.R;
import macbury.shodan.Shodan;
import macbury.shodan.events.ServerInfoEvent;
import macbury.shodan.models.Measurement;
import macbury.shodan.service.FindShodanService;
import retrofit2.Callback;
import retrofit2.Response;

public class StatsMainActivity extends AppCompatActivity implements Callback<Measurement> {
  private static final String TAG = "StatsMainActivity";
  private Shodan shodan;
  private TextView temperatureTextView;
  private TextView humidityTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_stats_main);
    this.shodan     = (Shodan)getApplication();

    this.temperatureTextView = (TextView)findViewById(R.id.temperatureTextView);
    this.humidityTextView = (TextView)findViewById(R.id.humidity_text_view);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });
  }

  /**
   * Show information that data is pulled
   */
  private void startLoading() {
    this.humidityTextView.setText("Wczytywanie...");
    this.temperatureTextView.setText("Wczytywanie...");
  }

  private void fetchData() {
    Log.i(TAG, "Fetching data...");
    shodan.getCurrentShodanServer().getService().current().enqueue(this);
  }

  @Subscribe
  public void onShodanServerInformationUpdate(ServerInfoEvent event) {
    if (shodan.getCurrentShodanServer() == null) {
      startLoading();
    } else {
      fetchData();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    shodan.bus.register(this);
    if (shodan.getCurrentShodanServer() == null) {
      startService(new Intent(this, FindShodanService.class));
      startLoading();
    } else {
      fetchData();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    shodan.bus.unregister(this);
    stopService(new Intent(this, FindShodanService.class));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_stats_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onResponse(Response<Measurement> response) {
    Measurement measurement = response.body();
    Log.i(TAG, "Got response: " + measurement.toString());

    this.humidityTextView.setText(measurement.getHumidity() + " %");
    this.temperatureTextView.setText(measurement.getTemperature() + " C");
  }

  @Override
  public void onFailure(Throwable t) {
    Log.e(TAG, "Error while fetching: ", t);
    //TODO show here some error
    //Snackbar.make(getCoi, "Replace with your own action", Snackbar.LENGTH_LONG)
     //   .setAction("Action", null).show();
  }
}
