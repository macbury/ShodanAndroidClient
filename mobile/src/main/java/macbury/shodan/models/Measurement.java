package macbury.shodan.models;

import java.util.Date;

/**
 * This is model with informations about current measurment from shodan
 */
public class Measurement {
  private float temperature;
  private float humidity;
  private int id;

  public int getTemperature() {
    return Math.round(temperature);
  }

  public int getHumidity() {
    return Math.round(humidity);
  }

  public int getId() {
    return id;
  }


  @Override
  public String toString() {
    return "Measurement{" +
        "temperature=" + temperature +
        ", humidity=" + humidity +
        ", id=" + id +
        '}';
  }
}
