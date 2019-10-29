package es.upm.miw.firebaselogin;

import java.util.List;

import es.upm.miw.firebaselogin.models.Forecast;
import retrofit2.Call;
import retrofit2.http.GET;


@SuppressWarnings("Unused")
interface ICountryRESTAPIService {

    // Request method and URL specified in the annotation
    // Callback for the parsed response is the last parameter

    @GET("/data/2.5/forecast?id=3117735&appid=cdd91ddbee758a85b64b6e06c319447a")
    Call<List<Forecast>> getAllForecast();
    // https://restcountries.eu/rest/v2/alpha/ES

    @GET("/data/2.5/forecast?id=3117735&appid=cdd91ddbee758a85b64b6e06c319447a")
    Call<Forecast> getForecast();

}
