package macbury.shodan.models;

/**
 * Wrapper around states of humidifier
 */
public class HumidifierState {
  private String state;
  private final static String STATE_OUT_OF_WATER = "out_of_water";

  public boolean isOutOfWater() {
    return STATE_OUT_OF_WATER.equalsIgnoreCase(state);
  }
}
