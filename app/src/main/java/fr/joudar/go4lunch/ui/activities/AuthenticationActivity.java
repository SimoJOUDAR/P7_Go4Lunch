package fr.joudar.go4lunch.ui.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.ActivityAuthenticationBinding;

public class AuthenticationActivity extends AppCompatActivity {

    ActivityAuthenticationBinding binding;

    //TODO : Create your own UserProvider interface implemented by FirebaseServices class and saved in the repo for less network requests and even offline work
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isAuth();
    }


    private void isAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            startHomepageActivity();
        }
        else {
            startAuth();
        }
    }

    private void startHomepageActivity() {
        final Intent homepageActivityIntent = new Intent(this, HomepageActivity.class);
        startActivity(homepageActivityIntent);
        finish();
    }

    private void startAuth() {

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(getAuthProviders())
                .setAuthMethodPickerLayout(getCustomAuthMethodPickerLayout())
                .setTheme(R.style.AuthTheme)
                .build();
        signInLauncher.launch(signInIntent);
    }

    // Provides the auth services (google, facebook)
    private List<AuthUI.IdpConfig> getAuthProviders() {
        return Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());
    }

    //applies our custom theme to the login section
    private AuthMethodPickerLayout getCustomAuthMethodPickerLayout() {
        return new AuthMethodPickerLayout.Builder(R.layout.auth_method_picker_layout)
                .setGoogleButtonId(R.id.google_login_btn)
                .setFacebookButtonId(R.id.fb_login_btn)
                .build();
    }

    // Manages login results (success/failure)
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        Log.i("LOGING", "____ON_RESULT_LOGIN______");
        if (result.getResultCode() == Activity.RESULT_OK) {
            startHomepageActivity();
        } else {
            binding.getRoot().setVisibility(View.VISIBLE);
            if (result.getResultCode() == Activity.RESULT_CANCELED) {
                //showLoginError(R.string.login_canceled);
            } else {
                //handleLoginFailure(result.getData());
                Toast.makeText(this, "No Auths", Toast.LENGTH_SHORT).show();
            }
        }
    }


}