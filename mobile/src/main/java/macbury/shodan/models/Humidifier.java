package macbury.shodan.models;

/**
 * Created by macbury on 29.01.16.
 */
public class Humidifier {
  private int id;
  private int left;
  private String state;
  private int min_humidity;
  private int next_update_in;
  private int total;

  public int getId() {
    return id;
  }

  public int getLeft() {
    return left;
  }

  public String getState() {
    return state;
  }

  public int getMinHumidity() {
    return min_humidity;
  }

  public int getNextUpdateIn() {
    return next_update_in;
  }

  public int getTotal() {
    return total;
  }
}
