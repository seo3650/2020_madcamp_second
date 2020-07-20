package com.example.tab;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import  android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import java.util.Objects;

import static android.content.Context.CAMERA_SERVICE;
import java.util.Objects;

import static android.content.Context.CAMERA_SERVICE;
import android.os.Build;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import java.nio.file.Files;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.getExternalStoragePublicDirectory;


public class Fragment4 extends Fragment {
    private static final String TAG = "FacebookFragment";


    //constants
    private static final int NUM_GRID_COLUMNS = 3;

    //widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;
    private Button deleteButton;


    //vars
    private ArrayList<String> directories = new ArrayList<String>(); ;
    private String mAppend = "file:/";
    private String pathToFile;


    //pokemon
    Toolbar toolbar;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        try {
            getLabelOfImage(null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        View view = inflater.inflate(R.layout.fragment4_layout, container, false);

        Log.d(TAG, "onCreateView: started.");


        /*
        toolbar = (Toolbar) view.findViewById(R.id.toolbar2);
        toolbar.setTitle("POKEMON LIST");
        setSupportActionBar(toolbar);

         */




        /*
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });



        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        // If using in a fragment
        loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

         */


        return view;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private ArrayList<String> getLabelOfImage(Image image) throws CameraAccessException {
        File testFile = new File("/storage/emulated/0/Download/322868_1100-800x825.jpg");
        Uri uri = FileProvider.getUriForFile(getActivity(), "com.thecodecity.cameraandroid.fileprovider", testFile);

        /* Get image */
        FirebaseVisionImage testImage;
        try {
            testImage = FirebaseVisionImage.fromFilePath(Objects.requireNonNull(getContext()), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        /* Get rotation */
        String cameraId = null;
        int rotation;
        try {
            CameraManager cameraManager = (CameraManager) getContext().getSystemService(CAMERA_SERVICE);
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String candidateId: cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(candidateId);
                Integer camerInfo = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (camerInfo != null && camerInfo == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = candidateId;
                }
            }
            rotation = LabelOfImage.getRotationCompensation(cameraId, getActivity(), getContext());
        } catch (CameraAccessException e) {
            rotation = 0;
        }


        return LabelOfImage.analyze(testImage, rotation);
    }
}