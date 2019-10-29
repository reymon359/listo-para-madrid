package es.upm.miw.firebaselogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

// Firebase
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import es.upm.miw.firebaselogin.models.Forecast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String API_BASE_URL = "http://api.openweathermap.org";

    private static final String LOG_TAG = "MiW";

    private TextView tvRespuesta;

    private ICountryRESTAPIService apiService;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private static final int RC_SIGN_IN = 2018;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvRespuesta = (TextView) findViewById(R.id.tvRespuesta);

        findViewById(R.id.logoutButton).setOnClickListener(this);


        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user is signed in
                    CharSequence username = user.getDisplayName();
                    Toast.makeText(MainActivity.this, getString(R.string.firebase_user_fmt, username), Toast.LENGTH_LONG).show();
                    Log.i(LOG_TAG, "onAuthStateChanged() " + getString(R.string.firebase_user_fmt, username));
                    ((TextView) findViewById(R.id.textView)).setText("Bienvenido: "+ username);
                    getForecast();
                } else {
                    // user is signed out
                    startActivityForResult(
                            // Get an instance of AuthUI based on the default app
                            AuthUI.getInstance().
                                    createSignInIntentBuilder().
                                    setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()
                                    )).
                                    setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */, true /* hints */).
                                    build(),
                            RC_SIGN_IN
                    );
                }
            }
        };
    }


    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.signed_in, Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "onActivityResult " + getString(R.string.signed_in));
                getForecast();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.signed_cancelled, Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "onActivityResult " + getString(R.string.signed_cancelled));
                finish();
            }
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        mFirebaseAuth.signOut();
        Log.i(LOG_TAG, getString(R.string.signed_out));
    }

    public void getForecast() {
        // btb added for retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ICountryRESTAPIService.class);
        Log.i(LOG_TAG, "gettingForecast ");
        obtenerInfoPais();
    }

    public void obtenerInfoPais() {

        // Realiza la llamada
        Call<Forecast> call_async = apiService.getForecast();

        // Asíncrona
        call_async.enqueue(new Callback<Forecast>() {

            @Override
            public void onResponse(Call<Forecast> call, Response<Forecast> response) {
                Log.i(LOG_TAG, "response => respuesta=" + response.body());

                Forecast forecast = response.body();
                tvRespuesta.setText("");
                List<es.upm.miw.firebaselogin.models.List> hoursList;
                if (null != forecast) {
                    hoursList = forecast.getList();


                    for (int i = 0; i < 9; i++) {

                        // kelvin to celsius
                        double dTemp = hoursList.get(i).getMain().getTemp() - 273.15;
                        double dTempRoundOff = Math.round(dTemp * 100) / 100;
                        // % humidity
                        Integer oiHumidPerc = hoursList.get(i).getMain().getHumidity();
                        tvRespuesta.append(hoursList.get(i).getDtTxt() + " " + dTempRoundOff + "ºC Lluvia:" + oiHumidPerc + "% \n\n");
                    }

                    Log.i(LOG_TAG, "obtenerInfoPais => respuesta=" + forecast);
                } else {
                    tvRespuesta.setText("error Al recoger el tiempo");
                    Log.i(LOG_TAG, "error Al recoger el tiempo");
                }
            }


            @Override
            public void onFailure(Call<Forecast> call, Throwable t) {
                Toast.makeText(
                        getApplicationContext(),
                        "ERROR: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                Log.e(LOG_TAG, t.getMessage());
            }
        });

    }

}