package com.knacky.events.presentation.fragments;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.knacky.events.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by knacky on 02.03.2018.
 */

public class SignInDialogFragment extends DialogFragment {
    @BindView(R.id.emailEditText)
    EditText signInEmail;

    @BindView(R.id.passwordEditText)
    EditText signInPassword;

    @BindView(R.id.sign_in_google_btn)
    Button signInGoogleBtn;

    @BindView(R.id.signUpNowLayout)
    LinearLayout signUpNowBtn;

    @BindView(R.id.logInBtn)
    Button logInBtn;

    @BindView(R.id.skipLayout)
    LinearLayout skipAuthBtn;

    @BindView(R.id.sign_in_FB_button)
    LoginButton signInFBbtn;

    SignUpDialogFragment signUpDialogFragment;
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;       //for facebook sign in
    private FirebaseUser user;

    private static final String TAG = "SignInDialogFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View signInFragmentview = inflater.inflate(R.layout.dialog_auth_sign_in_layout, null, false);
        ButterKnife.bind(this, signInFragmentview);

        signUpDialogFragment = new SignUpDialogFragment();
        signInFBbtn.setFragment(this);      //-----------------------REQUIRED LINE FOR FRAGMENT !!!
        // initAuthProviders();
//        configureFaceBookSignIn();
//        configureGoogleSignIn();
        mAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
        initButtons();
        return signInFragmentview;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }

    private void initButtons() {
        signInGoogleBtn.setOnClickListener(v -> {   //googleSignIn button
            signInWithGoogle();
//            configureGoogleSignIn();
        });    //call Sign in with Google
        signInFBbtn.setOnClickListener(v -> {       //FaceBookSignIn button
            Log.d(TAG, "facebook button");
            configureFaceBookSignIn();
        });
        skipAuthBtn.setOnClickListener(v -> dismiss());     //skip Authorization
        signUpNowBtn.setOnClickListener(v -> {      //call Registration form
            dismiss();
            signUpDialogFragment.show(getFragmentManager(), "signUpDialogFragment");
        });
        logInBtn.setOnClickListener(v ->    //Sign in with Email and Password
                signInWithEmailAndPass(signInEmail.getText().toString(), signInPassword.getText().toString()));
        this.setCancelable(true);
    }


    private void configureFaceBookSignIn() {
        // [START initialize_fblogin]
        // Initialize Facebook Login button
        signInFBbtn.setReadPermissions("email", "public_profile");
        Log.d(TAG, "SetReadPermisions");
        signInFBbtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);

                AccessToken token = loginResult.getAccessToken();
                Log.d(TAG, "handleFacebookAccessToken:" + token);

                AuthCredential facebookCredential = FacebookAuthProvider.getCredential(token.getToken());
                signInWithCredential(facebookCredential);           //AUTHENTIFICATION with FaceBook
            }

            @Override
            public void onCancel() {
                LoginManager.getInstance().logOut();
                Log.d(TAG, "facebook:onCancel");
//                updateUI(null);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
//                updateUI(null);
            }
        });
        // [END initialize_fblogin]
    }


    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    //----------------------------Main action------------------------//

    private void signInWithCredential(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = mAuth.getCurrentUser();
                            Log.d(TAG, "signInWithEmail:success, current user is: " + mAuth.getCurrentUser().getDisplayName());
                            Toast.makeText(getActivity(), "Authentication succeed," + mAuth.getCurrentUser().getDisplayName(),
                                    Toast.LENGTH_SHORT).show();

                            //                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Log.d(TAG, "failure, current user: " + mAuth.getCurrentUser());
                            Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                            //                            Snackbar.make(getView().findViewById(R.id.sign_in_dialog_fragment_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //                            updateUI(null);
                            LoginManager.getInstance().logOut();
                        }

                        // [START_EXCLUDE]
                        Toast.makeText(getContext(), "Process completed", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Process completed: " + mAuth.getCurrentUser());
//                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void signInWithEmailAndPass(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
//        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            user = mAuth.getCurrentUser();
                            Log.d(TAG, "signInWithEmail:success, current user is: " + mAuth.getCurrentUser().getDisplayName());
                            Toast.makeText(getActivity(), "Authentication succeed," + mAuth.getCurrentUser().getDisplayName(),
                                    Toast.LENGTH_SHORT).show();

//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure\nPlease,try later.", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }
//                        hideProgressDialog();

                    }
                });
        // [END sign_in_with_email]
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = signInEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            signInEmail.setError("Required.");
            valid = false;
        } else {
            signInEmail.setError(null);
        }

        String password = signInPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            signInPassword.setError("Required.");
            valid = false;
        } else {
            signInPassword.setError(null);
        }

        return valid;
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

                Toast.makeText(getContext(), "Starting process...", Toast.LENGTH_LONG).show();
                //        showProgressDialog();
                AuthCredential googleCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                signInWithCredential(googleCredential);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
//                updateUI(null);
            }
        }

        // Pass the activity result back to the Facebook SDK
//        if (mCallbackManager != null)
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }
    // [END onactivityresult]

    private void signOut() {
        Log.i(TAG, "Signing out... ");
        // Firebase sign out
        if (mAuth != null)
            mAuth.signOut();

        // Google sign out
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(getActivity(),
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.i(TAG, "Google signed out. ");
//                        updateUI(null);
                        }
                    });
        }
//        if (LoginManager.getInstance() != null)
            LoginManager.getInstance().logOut();  //fb sign out
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getDisplayName() != null)
            Log.i(TAG, "onDismiss, Current user is: " + mAuth.getCurrentUser().getDisplayName());
//        signOut();

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}
