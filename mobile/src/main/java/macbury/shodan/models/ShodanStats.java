package macbury.shodan.models;

import java.util.ArrayList;

/**
 * Created by macbury on 28.01.16.
 */
public class ShodanStats {
  private Measurement current;
  private ArrayList<ShodanDataPoint> last_24;
  private ArrayList<ShodanDataPoint> last_week;
  private Humidifier humidifier;

  public Measurement getCurrent() {
    return current;
  }

  public ArrayList<ShodanDataPoint> getLast24() {
    return last_24;
  }

  public ArrayList<ShodanDataPoint> getLastWeek() {
    return last_week;
  }

  public Humidifier getHumidifier() {
    return humidifier;
  }
}
