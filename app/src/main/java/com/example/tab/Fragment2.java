package com.example.tab;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import  android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import kotlin.Unit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static com.example.tab.MainActivity.sendedImages;
import static com.example.tab.MainActivity.userId;



public class Fragment2 extends Fragment {
    private static final String TAG = "GalleryFragment";


    //constants
    private static final int NUM_GRID_COLUMNS = 3;
    private static final int REQUET_VIDEO_CODE = 1000;

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
    private String deletePath;

    //facebook
    private LoginButton loginButton;
    private Button btnSharePhoto;
    private ShareDialog shareDialog;
    private CallbackManager callbackManagerShare;
    private ShareButton shareButton;


    private final String url = "http://192.249.19.244:2280/";




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        saveToDatabase();
        //callbackManager = CallbackManager.Factory.create();
        View view = inflater.inflate(R.layout.fragment2_layout, container, false);
        galleryImage = (ImageView) view.findViewById(R.id.galleryImageView);
        gridView = (GridView) view.findViewById(R.id.gridView);
        directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectory);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        directories = new ArrayList<String>();
        //deleteButton = (Button) view.findViewById(R.id.deleteButton); // instance of button class
        Log.d(TAG, "onCreateView: started.");

        //ImageView shareClose
        init();
        //facebook stuff
        shareButton = (ShareButton) view.findViewById(R.id.fb_share_button);
        shareDialog = new ShareDialog(getActivity());

        // launch camera
        FloatingActionButton btnLaunchCamera = view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*
                Snackbar.make(view, "It's a trap :D", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                 */
                Log.d(TAG, "onClick: launching camera.");

                /*
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

                 */
                dispatchPictureTakerAction();
            }

            private void dispatchPictureTakerAction(){
                Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = null;

                photoFile = createPhotoFile();
                if (photoFile != null) {
                    pathToFile = photoFile.getAbsolutePath();
                    Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.thecodecity.cameraandroid.fileprovider", photoFile);
                    takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePic, 1);
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

        //registerForContextMenu(null);



        registerForContextMenu(galleryImage);
        return view;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getActivity().getMenuInflater().inflate(R.menu.gallery_menu, menu);


    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.share_image:
                shareButton.performClick();
                return true;
            case R.id.delete_image:


                Log.d(TAG, "onContextItemSelected: delete selected. Deleting file.");
                if (pathToFile == null) {return false;}
                File file = new File(pathToFile);
                boolean deleted = file.delete();
                if (deleted){
                    Log.d(TAG, pathToFile + " deleted successfully.");
                    Toast.makeText(getActivity(), "Deleted selected image", Toast.LENGTH_SHORT);
                } else {
                    Log.d(TAG, "failed to delete " + pathToFile);
                }

                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                init();
                return true;
            default:
                return super.onContextItemSelected(item);
        }



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == 1){
                Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);

            }


            Log.d(TAG, "onActivityResult: done taking a photo.");
            Log.d(TAG, "onActivityResult: attempting to return to gallery.");

            //navigate back to our gallery.

            init();
        } else {
            // delete undefined .jpg file
            Log.d(TAG, "onActivityResults: camera aborted. Deleting temporary .jpg file");
            File file = new File(pathToFile);
            boolean deleted = file.delete();
            if (deleted){
                Log.d(TAG, pathToFile + " deleted successfully.");
            } else {
                Log.d(TAG, "failed to delete " + pathToFile);
            }

            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));



        }
    }

    private void init(){
        FilePaths filePaths = new FilePaths();

        if (FileSearch.getDirectoryPaths(filePaths.PICTURES) != null){
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }
        if (FileSearch.getDirectoryPaths(filePaths.PICTURES) != null){
            directories.add(filePaths.PICTURES);
        }




        if (FileSearch.getDirectoryPaths(filePaths.DOWNLOAD) != null){
            directories.add(filePaths.DOWNLOAD);
        }

        directories.add(filePaths.CAMERA);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item, directories);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: selected: " + directories.get(i));

                //setup our image grid for the directory chosen
                setupGridView(directories.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //check for other folders inside "/storage/emulated/0/pictures"


    }


    private void setupGridView(String selectedDirectory){
        Log.d((TAG), "setupGridView: directory chosen: " + selectedDirectory);
        final ArrayList<String> imgURLs = FileSearch.getFilePaths(selectedDirectory);

        //set the grid column width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);




        //use the grid adapter to adapter the images to gridview file ://
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, mAppend, imgURLs);
        gridView.setAdapter(adapter);


        //set the first image to be displayed when the activity fragment view is displayed



        if (imgURLs.size() > 0 ){
            setImage(imgURLs.get(0), galleryImage, mAppend);
            setShareButton(imgURLs.get(0));





        } else {
            Log.d(TAG, "empty directory. Share button disabled.");
            galleryImage.setImageResource( R.drawable.empty );
            setShareButton(null);
            pathToFile = "";


        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: selected an image: " + imgURLs.get(i));

                setImage(imgURLs.get(i), galleryImage, mAppend);
                setShareButton(imgURLs.get(i));

            }
        });

    }

    private void setShareButton(String imgURL  ){

        if (imgURL == null){
            shareButton.setShareContent(null);
            return;
        }

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),  Uri.parse("file://" + imgURL));
        } catch (FileNotFoundException e)
        { // TODO Auto-generated catch block e.printStackTrace();
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block e.printStackTrace();
            e.printStackTrace();
        }


        //BitmapDrawable bitmapDrawable = ((BitmapDrawable)getGalleryImage().getDrawable());

        // Bitmap image = bitmapDrawable.getBitmap();
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();


        shareButton.setShareContent(content);

        Log.d(TAG, "facebook share button set to " + imgURL);
    }

    private ImageView getGalleryImage(){
        return galleryImage;
    }

    private void setImage(final String imgURL, ImageView imageView,final String append){
        Log.d(TAG, "setImage: setting Image");
        Log.d(TAG, "setImage: imgURL" + imgURL);
        Log.d(TAG, "setImage: append" + append);


        if (imageView == galleryImage){
            pathToFile =  imgURL;
        }


        DisplayImageOptions GALLERY = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .considerExifParams(true)
                .build();

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));

        imageLoader.displayImage(append + imgURL, imageView,GALLERY, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        Log.d(TAG, "Loaded image " + append + imgURL);





        //ShareDialog.show(this, content);




        /* maybe: deleteButton?
        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                this.deleteImage( imgURL,  append);
            }

            private void deleteImage(String imgURL, String append){
                String file_dj_path = imgURL;
                File fDelete = new File(imgURL);
                if (fDelete.exists()){
                    if (fDelete.delete()){
                        Log.d(TAG, "file deleted: " + file_dj_path);
                        //callBroadCast();
                        init();
                    } else {
                        Log.d(TAG, "file not deleted: " + file_dj_path);
                    }
                } else {
                    Log.d(TAG, "no such file: " + file_dj_path);
                }

            }

         */


            /*
            private void callBroadCast(){
                if (Build.VERSION.SDK_INT >= 15){
                    Log.e("-->", " >= 15");
                    MediaScannerConnection.scanFile(MainActivity.this, new String[]{Environment.getExternalStorageDirectory().toString()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.e("ExternalStorage", "Scanned " + path + ":");
                                    Log.e("ExternalStorage", "-> uri=" + uri);
                                }
                            });
                } else{
                    Log.e("-->", " < 14");

                }

            }


        });


        Log.d(TAG, "setImage: button set");

             */


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
        ImageService service = retrofit.create(ImageService.class);

        /* Get storage */
        // TODO: Upload serveral images at a time
        File[] storageDirs = {new File(FilePaths.CAMERA), new File(FilePaths.DOWNLOAD), new File (FilePaths.PICTURES)};
        for (File storageDir : storageDirs) {
            /* Get image */
            File[] images = storageDir.listFiles();
            if (images == null) {
                continue;
            }
            for (File image : images) {
                /* Check duplicate sending */
                if (sendedImages.contains(image.toString())) {
                    continue;
                }
                sendedImages.add(image.toString());

                int pos = image.toString().lastIndexOf( "." );
                String ext = image.toString().substring( pos + 1 );
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/" + ext), image);
                // MultipartBody.Part is used to send also the actual filename
                MultipartBody.Part body = MultipartBody.Part.createFormData("image", image.getName(), requestFile);

                /* Send image to server */
                service.uploadImage(userId, "default", body).enqueue(new Callback<ResponseBody>() {
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
    }



/*
    private static final int REQUEST_PERMISSIONS = 1234;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };



    private  static final int PERMISSIONS_COUNT = 2;

    private boolean arePermissionDenied(){
        for (int i = 0; i < PERMISSIONS_COUNT; i++){
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

        }
    }
     */
}
