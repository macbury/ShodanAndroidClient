package macbury.shodan.api;

import java.lang.ref.Reference;

import macbury.shodan.models.HumidifierState;
import macbury.shodan.models.Measurement;
import macbury.shodan.models.RefillResult;
import macbury.shodan.models.ShodanStats;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by macbury on 27.01.16.
 */
public interface StatsService {
  @GET("/")
  Call<Measurement> current();
  @GET("/stats")
  Call<ShodanStats> all();
  @GET("/humidifier/{id}/refill")
  Call<RefillResult> refill(@Path("id") int humidifierId);

  @GET("/ping/{uid}")
  Call<HumidifierState> ping(@Path("uid") String deviceUid);
}
