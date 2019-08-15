package com.alfatih.socialmedialogin.facebookUtils;

import org.json.JSONObject;

public interface IFBSignInListener {

    void showProgressDialog();

    void hideProgressDialog();

    void sendToServer(JSONObject object);

    void saveSharedPreference(JSONObject object);

    void startActivity();
}
