package com.ut.apt.connexusmobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ut.apt.connexusmobile.R;


import org.apache.http.Header;
import org.w3c.dom.Text;

/**
 * Created by asim on 10/12/14.
 */
public class LoginFragment extends Fragment {

    private String TAG  = "LoginFragment";

    OnLoginButtonClickListener mLoginButtonClickListenerCallback;
    OnViewStreamButtonClickListener mViewStreamButtonClickListenerCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mLoginButtonClickListenerCallback = (OnLoginButtonClickListener) activity;
            mViewStreamButtonClickListenerCallback = (OnViewStreamButtonClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoginButtonClickListener/OnViewStreamButtonClickListener");
        }
    }

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View rootView = inflater.inflate(R.layout.login_layout_fragment, container, false);

        final Button loginButton = (Button) rootView.findViewById(R.id.loginBtnId);
        final EditText loginUserId = (EditText) rootView.findViewById(R.id.gmailLoginId);
        final EditText loginUserPwd = (EditText) rootView.findViewById(R.id.gmailLoginPwd);
        final Button viewStreamsButton = (Button) rootView.findViewById(R.id.viewStreamsBtnId);

        //Login Button Click Handler
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.e(TAG, "Inside the Click Handle for Login Button");
                Log.e(TAG, "Email Id Entered is: " + loginUserId.getText());
                Log.e(TAG, "Password Entered is: " + loginUserPwd.getText());

                mLoginButtonClickListenerCallback.onLoginButtonClick(loginUserId.getText().toString(), loginUserPwd.getText().toString());
            }
        });

        //View Stream Button Click Handler
        viewStreamsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e(TAG, "Inside the Click Handle for View All Streams Button");
                mViewStreamButtonClickListenerCallback.onViewAllStreamButtonClick();
            }
        });

        return rootView;
    }


    //Add Callback Handlers
    public interface OnLoginButtonClickListener{
        public void onLoginButtonClick(String loginUser, String loginPassword);
    }

    public interface OnViewStreamButtonClickListener{
        public void onViewAllStreamButtonClick();
    }


}
