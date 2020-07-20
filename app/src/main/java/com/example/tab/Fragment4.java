package com.example.tab;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import  android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.util.Arrays;
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
    private static final String TAG = "DogamFragment";


    //constants
    private static final int NUM_GRID_COLUMNS = 3;
    private final int REQUEST_CAMERA = 1;
    private static ArrayList<String> items;


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
        View view = inflater.inflate(R.layout.fragment4_layout, container, false);
        items = new ArrayList<>(Arrays.asList("Computer", "Phone", "Clock", "Chair"));

        Log.d(TAG, "onCreateView: started.");
        Log.d(TAG, "items initialized: " +items.toString());

        /*
        toolbar = (Toolbar) view.findViewById(R.id.toolbar2);
        toolbar.setTitle("POKEMON LIST");
        setSupportActionBar(toolbar);

         */
        /* Prepare camer button */
        FloatingActionButton btnLaunchCamera = view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: launching camera.");
                dispatchPictureTakerAction();
            }

            private void dispatchPictureTakerAction(){
                Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = null;

                //fix later

                photoFile = createPhotoFile();
                if (photoFile != null) {
                    pathToFile = photoFile.getAbsolutePath();
                    Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.thecodecity.cameraandroid.fileprovider", photoFile);
                    takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePic, REQUEST_CAMERA);
                }
            }

            private File createPhotoFile(){
                String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                //File appStorageDir = Context.getFilesDir();
                File image = null;
                try {
                    image = File.createTempFile(name, ".jpg", storageDir);
                } catch (IOException e) {
                    Log.d(TAG, "Excep : " +e.toString());
                }
                return image;
            }

        });

        return view;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {


                ArrayList<String> labels = getLabelOfImage();
                Log.d(TAG, "onActivityResult: labels = " + labels.toString());
                matchLabels(pathToFile, labels);
            } else {
                // delete empty .jpg file.

            }
        }
    }


    private Pair<String, ArrayList<String>> matchLabels(String pathToFile, ArrayList<String> labels) {

        Log.d(TAG, "matchLabels: started with labels " + labels.toString());
        ArrayList<String> matches = new ArrayList();

        for ( int i = 0; i < labels.size(); i ++) {
            for (int j = 0; j < items.size(); j++) {
                if (items.get(j).toLowerCase().contains(labels.get(i).toLowerCase())) {
                    matches.add(items.get(j));
                }
            }

        }
        Pair<String, ArrayList<String>> pair;
        if (matches.size() == 0){
            pair = new Pair<>(null, matches);
        } else {
            pair = new Pair<>(pathToFile, matches);

        }

        Log.d(TAG, "matchLabels: pair " + pair.toString());
        return pair;




    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private ArrayList<String> getLabelOfImage() {
//        File testFile = new File("/storage/emulated/0/Download/322868_1100-800x825.jpg");
        if (pathToFile == null) {
            return new ArrayList<String>();
        }
        File testFile = new File(pathToFile);
        Uri uri = FileProvider.getUriForFile(getActivity(), "com.thecodecity.cameraandroid.fileprovider", testFile);
        /* Get image */
        FirebaseVisionImage testImage;
        try {
            testImage = FirebaseVisionImage.fromFilePath(Objects.requireNonNull(getContext()), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<String>();
        }

        /* Get rotation */
        String cameraId = null;
        int rotation = 0;
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

        matchLabels(pathToFile, LabelOfImage.analyze(testImage, rotation)  );

        return LabelOfImage.analyze(testImage, rotation);
    }








}