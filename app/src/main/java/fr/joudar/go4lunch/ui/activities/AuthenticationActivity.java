package fr.joudar.go4lunch.ui.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

    private final String TAG = "AuthenticationActivity";
    ActivityAuthenticationBinding binding;
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
        Log.d(TAG, "onCreate");
        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        isAuth();
    }


    private void isAuth() {
        Log.d(TAG, "isAuth");
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
        Log.d(TAG, "startHomepageActivity");
        final Intent homepageActivityIntent = new Intent(this, HomepageActivity.class);
        startActivity(homepageActivityIntent);
        finish();
    }

    // Create and launch sign-in intent
    private void startAuth() {
        Log.d(TAG, "startAuth");
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(getAuthProviders())
                .setIsSmartLockEnabled(false)
                .setAuthMethodPickerLayout(getCustomAuthMethodPickerLayout())
                .setTheme(R.style.AuthTheme)
                .build();
        signInLauncher.launch(signInIntent);
    }

    // Provides the auth services (google, facebook)
    private List<AuthUI.IdpConfig> getAuthProviders() {
        Log.d(TAG, "getAuthProviders");
        return Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build() //TODO - Just a place filler for facebook Auth below
                //new AuthUI.IdpConfig.FacebookBuilder().build()
        );
    }

    //applies our custom theme to the login section
    private AuthMethodPickerLayout getCustomAuthMethodPickerLayout() {
        Log.d(TAG, "getCustomAuthMethodPickerLayout");
        return new AuthMethodPickerLayout.Builder(R.layout.auth_method_picker_layout)
                .setGoogleButtonId(R.id.google_login_btn)
                .setEmailButtonId(R.id.fb_login_btn) //TODO - Just a place filler for facebook auth below
                //.setFacebookButtonId(R.id.fb_login_btn)
                .build();
    }

    // Handles login results (success/failure)
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        Log.d(TAG, "onSignInResult");
        if (result.getResultCode() == Activity.RESULT_OK) {
            startHomepageActivity();
        } else {
            binding.getRoot().setVisibility(View.VISIBLE);
            binding.retryLogin.setOnClickListener(arg -> startAuth());
        }
    }


}