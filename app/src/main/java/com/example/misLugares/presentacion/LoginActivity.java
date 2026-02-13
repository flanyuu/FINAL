package com.example.misLugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.misLugares.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // IMPORTANTE: Reemplaza este client ID con el correcto de tu google-services.json
    // Búscalo en el objeto "oauth_client" donde "client_type": 3
    private static final String WEB_CLIENT_ID = "165553765880-qli6evqbigiookjc2dal04v65dr084es.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button btnSignIn = findViewById(R.id.btnGoogleSignIn);
        btnSignIn.setOnClickListener(v -> signIn());

        // Botón de bypass
        Button btnBypass = findViewById(R.id.btnBypass);
        btnBypass.setOnClickListener(v -> bypassLogin());
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Usuario ya autenticado: " + currentUser.getEmail());
            irAPantallaPrincipal();
        }
    }

    private void signIn() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google Sign-In exitoso: " + account.getEmail());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed. Status code: " + e.getStatusCode(), e);
                String mensaje = mensajeParaCodigo(e.getStatusCode());
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(this, "Bienvenido " + user.getDisplayName(),
                                    Toast.LENGTH_SHORT).show();
                            irAPantallaPrincipal();
                        }
                    } else {
                        Log.e(TAG, "signInWithCredential:failure", task.getException());
                        String errorMsg = "Autenticación fallida";
                        if (task.getException() != null) {
                            errorMsg += ": " + task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void irAPantallaPrincipal() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Mensaje claro según código de error. Código 10 = DEVELOPER_ERROR (SHA-1, package name o OAuth).
     */
    private String mensajeParaCodigo(int statusCode) {
        if (statusCode == 10) {
            return "Error de configuración (código 10). Añade la huella SHA-1 de tu keystore en Firebase/Google Cloud Console " +
                    "(Project settings → Your apps) y asegúrate de que el package name coincida. " +
                    "Mientras tanto puedes usar \"Entrar sin cuenta\".";
        }
        return "Error al iniciar sesión: " + statusCode + ". Verifica la configuración de Firebase.";
    }

    /**
     * Bypass para pruebas sin Google Sign-In.
     */
    private void bypassLogin() {
        Log.d(TAG, "BYPASS ACTIVADO");
        irAPantallaPrincipal();
    }
}