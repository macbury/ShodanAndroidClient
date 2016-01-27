package macbury.shodan;

import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import macbury.shodan.events.ServerInfoEvent;
import macbury.shodan.models.ShodanServerInfo;

/**
 * Main shodan application center
 */
public class Shodan extends Application {
  private ShodanServerInfo currentShodanServer;
  /**
   * For in process comunnication
   */
  public final Bus bus = new Bus(ThreadEnforcer.ANY);

  /**
   * Resets information about current shodan server and broadcasts information about it using {@link ServerInfoEvent}
   */
  public void restInformationAboutShodanServer() {
    this.currentShodanServer = null;
    bus.post(new ServerInfoEvent());
  }

  /**
   * Sets shodan server information and broadcasts information about it using {@link ServerInfoEvent}
   * @param hostAddress
   * @param port
   */
  public void setShodanServerHostAndPort(String hostAddress, int port) {
    currentShodanServer = new ShodanServerInfo(hostAddress, port);
    bus.post(new ServerInfoEvent());
  }

  public ShodanServerInfo getCurrentShodanServer() {
    return currentShodanServer;
  }
}
