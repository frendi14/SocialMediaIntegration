package com.alfatih.socialmedialogin.googleUtils;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;

public class GoogleUtils implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GoogleUtils";
    public static final int RC_SIGN_IN = 007;

    private final IGoogleSignInListener iGoogleSignInListener;
    private final GoogleApiClient mGoogleApiClient;
    private final FragmentActivity fragmentActivity;

    public GoogleUtils (FragmentActivity fragmentActivity, IGoogleSignInListener iGoogleSignInListener){
        this.fragmentActivity = fragmentActivity;
        this.iGoogleSignInListener = iGoogleSignInListener;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(fragmentActivity)
                .enableAutoManage(fragmentActivity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    public void getCachedGoogleSignIn(){
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleGoogleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            iGoogleSignInListener.onShowProgressDialog();
            opr.setResultCallback(googleSignInResult -> {
                iGoogleSignInListener.onHideProgressDialog();
                handleGoogleSignInResult(googleSignInResult);
            });
        }
    }

    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        fragmentActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                status -> iGoogleSignInListener.onUpdateUI(false));
    }

    public void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                status -> iGoogleSignInListener.onUpdateUI(false));
    }

    public void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleGoogleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            iGoogleSignInListener.onGSignResult(acct);
            iGoogleSignInListener.onUpdateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            iGoogleSignInListener.onUpdateUI(false);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


}
