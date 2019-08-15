package com.alfatih.socialmedialogin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import com.alfatih.socialmedialogin.databinding.ActivityMainBinding;
import com.alfatih.socialmedialogin.facebookUtils.FacebookUtils;
import com.alfatih.socialmedialogin.facebookUtils.IFBSignInListener;
import com.alfatih.socialmedialogin.googleUtils.GoogleUtils;
import com.alfatih.socialmedialogin.googleUtils.IGoogleSignInListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity  {

    public static final String TAG = "MainActivity";


    private ActivityMainBinding binding;
    private ProgressDialog mProgressDialog;

    private FacebookUtils mFacebookLoginManager;
    private GoogleUtils googleUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initFacebookClient();
        initGoogleClient();
        checkHash();

        binding.btnSignInGoogle.setSize(SignInButton.SIZE_STANDARD);
        binding.btnSignInGoogle.setOnClickListener(view -> googleUtils.signIn());
        binding.btnSignOutGoogle.setOnClickListener(view -> googleUtils.signOut());
        binding.btnRevokeAccessGoogle.setOnClickListener(view -> googleUtils.revokeAccess());

        binding.loginFacebookButton.setAuthType("rerequest");
        binding.loginFacebookButton.setOnClickListener(view -> {
            if (null != mFacebookLoginManager) {
                mFacebookLoginManager.logIn();
            }
        });
    }

    private void checkHash(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.alfatih.socialmedialogin",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void initGoogleClient(){
        googleUtils = new GoogleUtils(this, new IGoogleSignInListener() {
            @Override
            public void onShowProgressDialog() {
                showProgressDialog();
            }

            @Override
            public void onHideProgressDialog() {
                hideProgressDialog();
            }

            @Override
            public void onUpdateUI(boolean b) {
                updateUI(b);
            }

            @Override
            public void onGSignResult(GoogleSignInAccount account) {
                Log.e(TAG, "display name: " + account.getDisplayName());

                String personName = account.getDisplayName();
                String personPhotoUrl = account.getPhotoUrl().toString();
                String email = account.getEmail();
                Log.d(TAG, "handleGoogleSignInResult: "+account.getAccount().toString());
                Log.d(TAG, "handleGoogleSignInResult: ");

                Log.e(TAG, "Name: " + personName + ", email: " + email
                        + ", Image: " + personPhotoUrl);

                binding.txtName.setText(personName);
                binding.txtEmail.setText(email);
                Glide.with(getApplicationContext()).load(personPhotoUrl)
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.imgProfilePic);
            }
        });
    }

    private void initFacebookClient(){
        mFacebookLoginManager = new FacebookUtils(this, new IFBSignInListener() {
            @Override
            public void showProgressDialog() {
                //Show Custom Progress Bar
            }

            @Override
            public void hideProgressDialog() {
                //Hide Custom Progress Bar
            }

            @Override
            public void sendToServer(JSONObject object) {
                //Send JSON Response to your Server
                printUserDetails(object); // For Testing Purpose Only
            }

            @Override
            public void saveSharedPreference(JSONObject object) {
                //Save JSON Response to Shared Preference
            }

            @Override
            public void startActivity() {
                //Start new Activity
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleUtils.getCachedGoogleSignIn();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GoogleUtils.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            googleUtils.handleGoogleSignInResult(result);
        }
        if ((mFacebookLoginManager != null) && (null != mFacebookLoginManager.getmFacebookCallbackManager())) {
            mFacebookLoginManager.getmFacebookCallbackManager().onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            binding.btnSignInGoogle.setVisibility(View.GONE);
            binding.btnSignOutGoogle.setVisibility(View.VISIBLE);
            binding.btnRevokeAccessGoogle.setVisibility(View.VISIBLE);
            binding.llProfile.setVisibility(View.VISIBLE);
        } else {
            binding.btnSignInGoogle.setVisibility(View.VISIBLE);
            binding.btnSignOutGoogle.setVisibility(View.GONE);
            binding.btnRevokeAccessGoogle.setVisibility(View.GONE);
            binding.llProfile.setVisibility(View.GONE);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private static void printUserDetails(JSONObject object) {
        try {
            if (null != object) {
                Log.d(TAG, "ID : " + object.getLong("id"));

                if (null != object.getJSONObject("picture") && null != object.getJSONObject("picture").getJSONObject("data"))
                    Log.d(TAG, "PictureUrl : " + object.getJSONObject("picture").getJSONObject("data").getString("url"));

                Log.d(TAG, "Email : " + object.getString("email"));
                Log.d(TAG, "Name : " + object.getString("name"));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, "Error " + e.getLocalizedMessage());
        }
    }

}
