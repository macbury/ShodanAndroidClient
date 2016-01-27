package macbury.shodan.models;

import java.util.Date;

/**
 * This is model with informations about current measurment from shodan
 */
public class Measurement {
  private float temperature;
  private float humidity;
  private int id;

  public float getTemperature() {
    return temperature;
  }

  public float getHumidity() {
    return humidity;
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
