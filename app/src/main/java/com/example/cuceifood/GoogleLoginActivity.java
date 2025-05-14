package com.example.cuceifood;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class GoogleLoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login);

        // 1. Inicialización FORZADA de Backendless
        Backendless.setUrl("https://api.backendless.com");
        Backendless.initApp(this, "18CFE9E5-DC47-458C-AD0A-D62AEF29AEDF", "63A6C9A3-C7B7-49ED-82D9-C47C6D349597");
        //Backendless.UserService.setStayLoggedIn(true);

        // 2. Configuración AGREGADA para debug
        printDebugInfo();

        // 3. Configuración MEJORADA de Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            Log.d("DEBUG", "Iniciando flujo de Google Sign-In...");
            signIn();
        });
    }

    private void printDebugInfo() {
        // Verificación EXTRA de configuración
        Log.d("DEBUG", "Package name: " + getPackageName());
        Log.d("DEBUG", "SHA-1 debug: " + getSha1Debug());
        Log.d("DEBUG", "Client ID: " + getString(R.string.server_client_id));
    }

    private String getSha1Debug() {
        try {
            Process process = Runtime.getRuntime().exec(
                    "keytool -list -v -keystore " +
                            System.getProperty("user.home") + "/.android/debug.keystore " +
                            "-alias androiddebugkey -storepass android -keypass android");

            process.waitFor();
            java.util.Scanner s = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";

            int sha1Index = result.indexOf("SHA1:");
            if (sha1Index != -1) {
                return result.substring(sha1Index, result.indexOf("\n", sha1Index)).trim();
            }
            return "No encontrado";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                Log.d("DEBUG", "Token recibido (inicio): " + account.getIdToken().substring(0, 20) + "...");
                loginWithBackendless(account.getIdToken());
            }
        } catch (ApiException e) {
            Log.e("ERROR", "Código: " + e.getStatusCode(), e);
            showErrorDialog(e.getStatusCode());
        }
    }

    private void loginWithBackendless(String idToken) {
        Backendless.UserService.loginWithOAuth2(
                "googleplus",
                idToken,
                null,
                new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser user) {
                        Log.d("SUCCESS", "Login exitoso: " + user.getUserId());
                        startActivity(new Intent(GoogleLoginActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("ERROR", "Backendless error: " + fault.getMessage());
                        showToast("Error en servidor: " + fault.getMessage());
                    }
                },
                true
        );
    }

    private void showErrorDialog(int errorCode) {
        String message;
        switch (errorCode) {
            case 10:
                message = "SOLUCIÓN DEFINITIVA:\n\n" +
                        "1. Ve a Google Cloud Console > Credenciales\n" +
                        "2. ELIMINA las credenciales existentes\n" +
                        "3. Crea NUEVAS credenciales para Android\n" +
                        "4. ESPERA 15 minutos\n" +
                        "5. Reinstala la app COMPLETAMENTE";
                break;
            default:
                message = "Error code: " + errorCode;
        }

        new AlertDialog.Builder(this)
                .setTitle("Error en Google Sign-In")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }
}
