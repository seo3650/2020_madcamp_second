package com.example.tab;

import android.os.Environment;

import java.io.File;

public class FilePaths {

    //"storage/emulated/0"
    public static String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public static String PICTURES = ROOT_DIR + "/Pictures";
    public static String CAMERA = ROOT_DIR + "/DCIM/Camera";
    public static String DOWNLOAD = ROOT_DIR + "/Download";
    //public String DOCUMENT = ROOT_DIR +"/document";
}
