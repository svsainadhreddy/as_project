package com.example.myapplicationpopc.utils;

import android.content.Context;
import android.content.SharedPreferences;
public class SharedPrefManager {

        private static final String SHARED_PREF_NAME = "popc_prefs";
        private static final String KEY_TOKEN = "key_token";
        private static final String KEY_DOCTOR_ID = "key_doctor_id";
        private static final String KEY_USERNAME = "key_username";


        private static SharedPrefManager mInstance;
        private static Context mCtx;

        private SharedPrefManager(Context context) {
            mCtx = context.getApplicationContext();
        }

        public static synchronized SharedPrefManager getInstance(Context context) {
            if (mInstance == null) {
                mInstance = new SharedPrefManager(context);
            }
            return mInstance;
        }

        // Save login details
        public void saveLoginData(String token, String doctorId, String username) {
            SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_TOKEN, token);
            editor.putString(KEY_DOCTOR_ID, doctorId);
            editor.putString(KEY_USERNAME, username);
            editor.apply();
        }

        // Getters
        public String getToken() {
            SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(KEY_TOKEN, null);
        }

        public String getDoctorId() {
            SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(KEY_DOCTOR_ID, null);
        }

        public String getUsername() {
            SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(KEY_USERNAME, null);
        }

        // Clear data on logout
        public void logout() {
            SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }


    }


