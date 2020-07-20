package com.example.tab;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
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
import android.net.Uri;

import java.util.Arrays;
import java.util.Objects;

import static android.content.Context.CAMERA_SERVICE;

import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static com.example.tab.MainActivity.url;
import static com.example.tab.MainActivity.userId;


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


    interface LabelsResponse {
        void onResponseReceived(ArrayList<String> res);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                getLabelOfImage(new LabelsResponse() {
                    @Override
                    public void onResponseReceived(ArrayList<String> res) {
                        ArrayList<String> labels = res;
                        Log.d(TAG, "onActivityResult: labels = " + labels.toString());
                        matchLabels(pathToFile, labels);
                    }
                });
            } else {

                // delete undefined .jpg file?
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
    private void getLabelOfImage(LabelsResponse labelsResponse) {
//        File testFile = new File("/storage/emulated/0/Download/322868_1100-800x825.jpg");
        if (pathToFile == null) {
            return;
        }
        File testFile = new File(pathToFile);
        Uri uri = FileProvider.getUriForFile(getActivity(), "com.thecodecity.cameraandroid.fileprovider", testFile);
        /* Get image */
        FirebaseVisionImage testImage;
        try {
            testImage = FirebaseVisionImage.fromFilePath(Objects.requireNonNull(getContext()), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return;
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
        LabelOfImage.analyze(testImage, rotation, labelsResponse);
    }

    private void getFromDatabase(String answer) {
        /* Check login info */
        if (userId == null) {
            return;
        }
        /* Init retrofit */
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.valueOf(url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ImageService service = retrofit.create(ImageService.class);

    }

    private void saveToDatabase(File image, String answer) {
        /* Check login info */
        if (userId == null) {
            return;
        }
        /* Init retrofit */
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.valueOf(url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ImageService service = retrofit.create(ImageService.class);


        int pos = image.toString().lastIndexOf( "." );
        String ext = image.toString().substring( pos + 1 );
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/" + ext), image);
        // MultipartBody.Part is used to send also the actual filename
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", image.getName(), requestFile);

        /* Send image to server */
        service.uploadImage(userId, answer, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                Log.d("ImageService", "res:" + response);
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Log.d("ImageService", "Failed API call with call: " + call
                        + ", exception:  " + t);
            }
        });
    }








}