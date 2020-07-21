package com.example.tab;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.facebook.internal.ImageResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import  android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Objects;

import static android.content.Context.CAMERA_SERVICE;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import kotlin.Unit;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;
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
    static ArrayList<String> items;


    //widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;
    private Button deleteButton;
    private KonfettiView konfettiView;
    public static IconRoundCornerProgressBar progressBar;


    //vars
    private ArrayList<String> directories = new ArrayList<String>(); ;
    private String mAppend = "file:/";
    private String pathToFile;
    public static int foundItems = 0;


    //pokemon
    Toolbar toolbar;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment4_layout, container, false);
        items = new ArrayList<>(Arrays.asList(
                "Computer", "Mobile Phone",
                "Clock", "Chair", "Vehicle",
                "Umbrella", "Stairs", "Sky",
                "Plant", "Pattern", "Car", "Asphalt",
                "Building"));
        konfettiView = view.findViewById(R.id.konfettiView);
        konfettiView.bringToFront();
        konfettiView.setElevation(3000);

        Log.d(TAG, "onCreateView: started.");
        Log.d(TAG, "items initialized: " +items.toString());

        /* Show loading */
        SweetAlertDialog pDialog = new SweetAlertDialog(Objects.requireNonNull(getContext()), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
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
        pDialog.dismissWithAnimation();

        return view;
    }


    interface LabelsResponse {
        void onResponseReceived(ArrayList<String> res);
    }
    public interface ImageResponse {
        void onResponseReceived(Bitmap res);
    }
    interface DatabaseResponse {
        void onResponseReceived(String item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                /* Show loading */
                SweetAlertDialog pDialog = new SweetAlertDialog(Objects.requireNonNull(getContext()), SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                pDialog.setTitleText("Loading");
                pDialog.setCancelable(false);
                pDialog.show();
                /* Process image */
                getLabelOfImage(new LabelsResponse() {
                    @Override
                    public void onResponseReceived(ArrayList<String> res) {
                        ArrayList<String> labels = res;
                        Log.d(TAG, "onActivityResult: labels = " + labels.toString());
                        Pair<String, String> pair = matchLabels(pathToFile, labels);
                        Toast.makeText(getActivity(), labels.toString(), Toast.LENGTH_LONG).show();
                        String path = pair.first;
                        String match = pair.second;

                        if (pair.first != null) {
                            File file = new File(path);
                            saveToDatabase(file, match, new DatabaseResponse() {
                                @Override
                                public void onResponseReceived(String item) {
                                    /* Success message */
                                    konfettiView.build()
                                            .addColors(Color.BLACK, Color.BLUE, Color.GRAY)
                                            .setDirection(0.0, 359.0)
                                            .setSpeed(1f, 5f)
                                            .setFadeOutEnabled(true)
                                            .setTimeToLive(2000L)
                                            .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                                            .addSizes(new Size(12, 5f))
                                            .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                                            .streamFor(300, 5000L);

                                    pDialog.dismissWithAnimation();
                                    new SweetAlertDialog(Objects.requireNonNull(getContext()), SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success!")
                                            .setContentText("You found "+ item.toLowerCase() + ". " + "Good job:)")
                                            .setConfirmText("Okay")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sDialog) {
                                                    sDialog.dismissWithAnimation();
                                                }
                                            })
                                            .show();
                                    FragmentTransaction tr = getFragmentManager().beginTransaction();
                                    tr.replace(R.id.fragment4_layout, new Fragment4() );
                                    tr.commit();
                                }
                            });
                            Log.d(TAG, "uploaded image with label " + match);

                        } else {
                            /* Fail message */
                            pDialog.dismissWithAnimation();
                            new SweetAlertDialog(Objects.requireNonNull(getContext()), SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Failed..")
                                    .setContentText("Try another image!")
                                    .setConfirmText("Okay, I will:)")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                        }
                                    })
                                    .show();
                        }
                    }
                });
            } else {

                Log.d(TAG, "onActivityResults: camera aborted. Deleting temporary .jpg file");
                File file = new File(pathToFile);
                boolean deleted = file.delete();
                if (deleted){
                    Log.d(TAG, pathToFile + " deleted successfully.");
                } else {
                    Log.d(TAG, "failed to delete " + pathToFile);
                }

                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                // delete undefined .jpg file?
            }

        }
    }

    // matches dogam labels with firebase image labels.
    private Pair<String, String> matchLabels(String pathToFile, ArrayList<String> labels) {

        Log.d(TAG, "matchLabels: started with labels " + labels.toString());
        ArrayList<String> matches = new ArrayList();

        for ( int i = 0; i < labels.size(); i ++) {
            for (int j = 0; j < items.size(); j++) {
                if (items.get(j).toLowerCase().contains(labels.get(i).toLowerCase())) {
                    matches.add(items.get(j));
                    break;
                }
            }

        }
        Pair<String, String> pair;
        if (matches.size() == 0){
            pair = new Pair<>(null, matches.get(0));
        } else {
            pair = new Pair<>(pathToFile, matches.get(0));

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
//        try {
//            testImage = FirebaseVisionImage.fromFilePath(Objects.requireNonNull(getContext()), uri);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }

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

        /* Get image */
        Bitmap bitmap = BitmapFactory.decodeFile(testFile.toString());
        Bitmap rotatedBitmap = rotateImage(bitmap, rotation);
        testImage = FirebaseVisionImage.fromBitmap(rotatedBitmap);

        /* Save image to pathToFile */
        OutputStream out = null;
        try {
            File file = new File(pathToFile);
            out = new FileOutputStream(file);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        LabelOfImage.analyze(testImage, rotation, labelsResponse);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

     public static void getFromDatabase(String requiredImage, ImageResponse imageResponse) {
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

        service.downloadImage(userId, "dogam", requiredImage).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                Log.d("ImageService", "res:" + response.body());
                if (response.body() == null){
                    imageResponse.onResponseReceived(null);
                    return;

                }
                InputStream stream = response.body().byteStream();
                Bitmap image = BitmapFactory.decodeStream(stream);
                imageResponse.onResponseReceived(image);
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Log.d("ImageService", "Failed API call with call: " + call
                        + ", exception:  " + t);
                imageResponse.onResponseReceived(null);
            }
        });
    }

    private void saveToDatabase(File image, String answer, DatabaseResponse databaseResponse) {
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
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", answer + ".jpg", requestFile);

        /* Send image to server */
        service.uploadImage(userId, "dogam", body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                Log.d("ImageService", "res:" + response);
                databaseResponse.onResponseReceived(answer);
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Log.d("ImageService", "Failed API call with call: " + call
                        + ", exception:  " + t);
            }
        });
    }








}