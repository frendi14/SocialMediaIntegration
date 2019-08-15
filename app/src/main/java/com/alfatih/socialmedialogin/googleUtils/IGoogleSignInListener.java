package com.alfatih.socialmedialogin.googleUtils;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public interface IGoogleSignInListener {

    void onShowProgressDialog();

    void onHideProgressDialog();

    void onUpdateUI(boolean b);

    void onGSignResult(GoogleSignInAccount account);
}
