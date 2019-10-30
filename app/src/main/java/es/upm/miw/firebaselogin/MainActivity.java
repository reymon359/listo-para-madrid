package es.upm.miw.firebaselogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// Firebase
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import es.upm.miw.firebaselogin.models.Forecast;
import es.upm.miw.noisereporter.NoiseAlertActivity;
import es.upm.miw.noisereporter.fcube.commands.FCColor;
import es.upm.miw.noisereporter.fcube.commands.FCOff;
import es.upm.miw.noisereporter.fcube.commands.FCOn;
import es.upm.miw.noisereporter.fcube.config.FeedbackCubeConfig;
import es.upm.miw.noisereporter.fcube.config.FeedbackCubeManager;
import es.upm.miw.noisereporter.feeback.FeedbackColor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity implements View.OnClickListener {
    static String sIp = "192.168.0.100";
    private static final String API_BASE_URL = "http://api.openweathermap.org";

    private static final String LOG_TAG = "MiW";

    private TextView tvRespuesta, tvTemperatura, tvAbrigo, tvNubes, tvParaguas, tvBaseDatos;

    private ICountryRESTAPIService apiService;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Button cubeButton, buttonActualizar;

    private static final int RC_SIGN_IN = 2018;

    private Double mediaTemperatura = 0.0;
    private Integer mediaNubes = 0;

    //Database
    // btb Firebase database variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference tiemposDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Elementos
        tvRespuesta = findViewById(R.id.tvRespuesta);
        tvTemperatura = findViewById(R.id.tvTemperatura);
        tvAbrigo = findViewById(R.id.tvAbrigo);
        tvNubes = findViewById(R.id.tvNubes);
        tvParaguas = findViewById(R.id.tvParaguas);
        tvBaseDatos = findViewById(R.id.tvBaseDatos);
        cubeButton = findViewById(R.id.cubeButton);
        buttonActualizar = findViewById(R.id.buttonActualizar);
//        buttonBaseDAtos = findViewById(R.id.buttonBaseDatos);

        // Botones
        findViewById(R.id.logoutButton).setOnClickListener(this);
        cubeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setCubeLight();
            }
        });
        buttonActualizar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getForecast();
            }
        });

        // btb Get instance of Firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        tiemposDatabaseReference = mFirebaseDatabase.getReference().child("tiempos");

        // btb Listener will be called when changes were performed in DB
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                // Deserialize data from DB into our FriendlyMessage object
//                FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
//                mMessageAdapter.add(friendlyMessage);
                tvBaseDatos.setText("");
                tvBaseDatos.setText(dataSnapshot.getValue(Forecast.class).getList().get(0).getDtTxt());

           }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        tiemposDatabaseReference.addChildEventListener(mChildEventListener);


        // Auth
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
                    ((TextView) findViewById(R.id.textView)).setText("Bienvenido: " + username);
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

    public void guardarDatosDatabase(Forecast forecast) {
        tiemposDatabaseReference.push().setValue(forecast);
    }
    public void setCubeLight() {
        // First encender cubo
        encenderCubo();
        // Si hace frio Cubo azul
        if (mediaTemperatura < 20) {
            sendColor(new FeedbackColor(0, 0, 255));
        } else {
            // Si hace calor cubo rojo
            sendColor(new FeedbackColor(255, 0, 0));
        }

    }
    public void sendColor(FeedbackColor color) {
        FCColor fcc = new FCColor(sIp, "" + color.getR(), ""
                + color.getG(), "" + color.getB());
        new FeedbackCubeManager().execute(fcc);
    }
    public void encenderCubo() {
        FCOn f = new FCOn(sIp);
        new FeedbackCubeManager().execute(f);
    }
    public void apagarCubo() {
        FCOff f = new FCOff(sIp);
        new FeedbackCubeManager().execute(f);
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
        Toast.makeText(this, "Tiempo recogido y actualizado en la base de datos", Toast.LENGTH_LONG).show();
    }

    public void obtenerInfoPais() {
        Call<Forecast> call_async = apiService.getForecast();
        call_async.enqueue(new Callback<Forecast>() {
            @Override
            public void onResponse(Call<Forecast> call, Response<Forecast> response) {
                Log.i(LOG_TAG, "response => respuesta=" + response.body());

                Forecast forecast = response.body();
                tvRespuesta.setText("");
                List<es.upm.miw.firebaselogin.models.List> hoursList;
                if (null != forecast) {
                    hoursList = forecast.getList();
                    guardarDatosDatabase(forecast);// guardando en base de datos
                    for (int i = 0; i < 9; i++) {
                        // kelvin to celsius
                        double dTemp = hoursList.get(i).getMain().getTemp() - 273.15;
                        double dTempRoundOff = Math.round(dTemp * 100) / 100;
                        mediaTemperatura += dTempRoundOff;
                        // % humidity
                        Integer oiHumidPerc = hoursList.get(i).getMain().getHumidity();
                        mediaNubes += oiHumidPerc;
                        tvRespuesta.append(hoursList.get(i).getDtTxt() + " " + dTempRoundOff + "ºC Nubes:" + oiHumidPerc + "% \n\n");
                    }
                    mediaTemperatura = mediaTemperatura / 9;
                    mediaNubes = mediaNubes / 9;
                    temperaturaNubes(mediaTemperatura, mediaNubes);

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

    public void temperaturaNubes(Double mediaTemperatura, Integer mediaNubes) {
        DecimalFormat numberFormat = new DecimalFormat("#.00");
        tvTemperatura.setText("Temperatura media: " + numberFormat.format(mediaTemperatura) + "ºC");
        tvNubes.setText("Cielo nublado: " + mediaNubes + "%");

        String textoAbrigo;
        String textoParaguas;

        if (mediaTemperatura < 18.0) {
            textoAbrigo = "❄ Abrigate bien que hace frio!";
            tvAbrigo.setTextColor(getResources().getColor(R.color.red));
        } else if (mediaTemperatura >= 18.0 && mediaTemperatura <= 22.0) {
            textoAbrigo = "🧣 Hace fresquete, deberías abrigarte.";
            tvAbrigo.setTextColor(getResources().getColor(R.color.yellow));
        } else {
            textoAbrigo = "☀ No hace falta que te abrigues.";
            tvAbrigo.setTextColor(getResources().getColor(R.color.green));
        }

        if (mediaNubes < 33) {
            textoParaguas = "☀ No hace falta que cojas el paraguas";
            tvParaguas.setTextColor(getResources().getColor(R.color.green));
        } else if (mediaNubes >= 33 && mediaNubes <= 66) {
            textoParaguas = "🌂 Deberías coger el paraguas por si acaso";
            tvParaguas.setTextColor(getResources().getColor(R.color.yellow));
        } else {
            textoParaguas = "☔ Coge el paraguas porque lo vas a necesitar.";
            tvParaguas.setTextColor(getResources().getColor(R.color.red));
        }

        tvAbrigo.setText(textoAbrigo);
        tvParaguas.setText(textoParaguas);
    }

}

