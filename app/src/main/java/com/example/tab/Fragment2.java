package com.example.tab;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import  android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import java.nio.file.Files;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.getExternalStoragePublicDirectory;


import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


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

    //facebook
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private Button btnSharePhoto;
    private ShareDialog shareDialog;
    private CallbackManager callbackManagerShare;
    private ShareButton shareButton;


    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            SharePhoto sharePhoto = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (shareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(sharePhoto)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

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
        callbackManager = CallbackManager.Factory.create();
        callbackManagerShare = CallbackManager.Factory.create();
        shareButton = (ShareButton) view.findViewById(R.id.fb_share_button);
        shareDialog = new ShareDialog(getActivity());




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


        //FacebookSdk.sdkInitialize(getActivity());


        /*
        btnSharePhoto = (Button) view.findViewById(R.id.btnSharePhoto) ;


        btnSharePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                shareDialog.registerCallback(callbackManagerShare, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Toast.makeText(getActivity(), "Share successful!", Toast.LENGTH_SHORT ).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getActivity(), "Share cancelled!", Toast.LENGTH_SHORT ).show();

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG ).show();

                    }
                });





                //Log.d(TAG, "callBack setup complete.");
                //We will fetch photo from link and convert to bitmap


                Picasso.with(getContext()) // not so sure.
                        .load("https://commons.wikimedia.org/wiki/File:The_Dark_Knight_Batman.jpg")
                        .into(target);




        });

         */







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

        return view;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == 1){
                Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);

            } else if (requestCode == REQUET_VIDEO_CODE) {
                Uri selectedVideo = data.getData();

                ShareVideo video = new ShareVideo.Builder()
                        .setLocalUrl(selectedVideo)
                        .build();

                ShareVideoContent videoContent = new ShareVideoContent.Builder()
                        .setContentTitle("This is a useful video")
                        .setContentDescription("Funny video from EDMT Dev download from YouTube")
                        .setVideo(video)
                        .build();

                if (shareDialog.canShow(ShareVideoContent.class))
                    shareDialog.show(videoContent);
            }

            Log.d(TAG, "onActivityResult: done taking a photo.");
            Log.d(TAG, "onActivityResult: attempting to return to gallery.");

            //navigate back to our gallery.

            init();
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);

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
                android.R.layout.simple_spinner_item, directories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
            galleryImage.setImageResource( R.drawable.search );
            shareButton.setShareContent(null);
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: selected an image: " + imgURLs.get(i));

                setImage(imgURLs.get(i), galleryImage, mAppend);
                setShareButton(imgURLs.get(i));

            /*
            if (ShareDialog.canShow(SharePhotoContent.class)) {
                shareDialog.show(content);
            }
             */



            }
        });

    }

    private void setShareButton(String imgURL  ){
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
