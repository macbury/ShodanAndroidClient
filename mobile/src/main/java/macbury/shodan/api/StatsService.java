package macbury.shodan.api;

import macbury.shodan.models.Measurement;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by macbury on 27.01.16.
 */
public interface StatsService {
  @GET("/")
  Call<Measurement> current();
}
