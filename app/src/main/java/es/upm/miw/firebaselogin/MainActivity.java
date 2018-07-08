package es.upm.miw.firebaselogin;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

// Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends Activity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String username = (currentUser != null) ? currentUser.getDisplayName() : null;
        Toast.makeText(this, "User: " + username, Toast.LENGTH_SHORT).show();
    }
}
