package com.example.tab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


// facebook SDK imports.
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


import com.example.tab.ui.main.SectionsPagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

public class MainActivity extends AppCompatActivity {
    public static Context context;
    private TextView myMessage;
    private Context mContext = MainActivity.this;
    public static String userId = null;
    private String url = "http://192.249.19.244:2280/";

    private int REQUEST_LOGIN = 2;

    /* DB info */
    public static ArrayList<Long> sendedContacts = new ArrayList<>();
    public static ArrayList<String> sendedImages = new ArrayList<>();

    private static final String TAG = "MyMessage";
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // facebook stuff
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        /* Login indent */
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);

        /* Save account info */
        saveToDatabase();

        //logger.logPurchase(BigDecimal.valueOf(4.32), Currency.getInstance("USD"));
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                userId = data.getStringExtra("userId");
                Toast.makeText(MainActivity.this, "Login success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveToDatabase() {
        /* Check login info */
        if (userId == null) {
            return;
        }

        /* Init retrofit */
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.valueOf(this.url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AccountService service = retrofit.create(AccountService.class);

        service.addAccount(userId).enqueue(new Callback<Unit>() {
            @Override
            public void onResponse(@NotNull Call<Unit> call, @NotNull Response<Unit> response) {
                Log.d("AccountService", "res:" + response);
            }

            @Override
            public void onFailure(@NotNull Call<Unit> call, @NotNull Throwable t) {
                Log.d("AccountService", "Failed API call with call: " + call
                        + ", exception:  " + t);
            }
        });
    }
}