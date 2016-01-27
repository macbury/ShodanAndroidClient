package macbury.shodan.models;

import macbury.shodan.api.StatsService;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

/**
 * Simple model containing all information for connection
 */
public class ShodanServerInfo {
  private final StatsService service;
  private String host;
  private int port;

  public ShodanServerInfo(String host, int port){
    this.host = host;
    this.port = port;

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://" + host + ":" + port)
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    this.service = retrofit.create(StatsService.class);
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public StatsService getService() {
    return service;
  }
}
