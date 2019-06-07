package com.example.beastincarnate.SmartScanner.UI.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.beastincarnate.SmartScanner.R;
import com.example.beastincarnate.SmartScanner.UI.Views.CameraPreview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
//import static android.provider.MediaStore.Filesv.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private ProgressDialog mProgressDialog;
    String fileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        final Intent intent = getIntent();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_EXTERNAL_STORAGE);
        } else {
           // setUpUI();
        }
    }

    private void extractBarCode(final CameraActivity cameraActivity, Uri uri) {
        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(cameraActivity,uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(image != null){
            FirebaseVisionBarcodeDetector barcodeDetector = FirebaseVision.getInstance().getVisionBarcodeDetector();
            barcodeDetector.detectInImage(image)
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                                    printBarcodes(firebaseVisionBarcodes);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if(mProgressDialog.isShowing())
                                        mProgressDialog.dismiss();
                                    finish();
                                    Toast.makeText(cameraActivity, "Failed to extract Barcode!!", Toast.LENGTH_SHORT).show();
                                }
                            });
        }

    }

    private void printBarcodes(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
        for(FirebaseVisionBarcode barcode : firebaseVisionBarcodes){
            Log.d("Barcode: "," "+barcode.getDisplayValue()+" "+barcode.getRawValue());
        }
        if(mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        finish();
    }

    private void extractText(final Context context, byte[] buffer) {
        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(480)   // 480x360 is typically sufficient for
                .setHeight(360)  // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                //.setRotation(rotation)
                .build();
        FirebaseVisionImage firebaseVisionImage = null;
//        try {
//            //firebaseVisionImage = (FirebaseVisionImage) FirebaseVisionImage.fromFilePath(context);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if(firebaseVisionImage != null){
            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            firebaseVisionTextRecognizer.processImage(firebaseVisionImage)
                                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                                @Override
                                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                                    copyTextToClipboard(firebaseVisionText);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    if(mProgressDialog.isShowing())
                                                        mProgressDialog.dismiss();
                                                    finish();
                                                    Toast.makeText(CameraActivity.this, "Failed to extract text", Toast.LENGTH_SHORT).show();
                                                }
                                            });

        }
    }

    private void copyTextToClipboard(FirebaseVisionText result) {
        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text",result.getText());
        clipboardManager.setPrimaryClip(clip);
        if(mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        Toast.makeText(CameraActivity.this, "Text successfully copied to clipboard", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean checkCameraHardware(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            return true;
        else
            return false;
    }

    public static Camera getCameraInstance(Context context){
        Camera c = null;
        try {
            c = Camera.open();
        }catch (Exception e){
            Toast.makeText(context, "Could not open Camera", Toast.LENGTH_SHORT).show();
        }
        return c;
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MajorII");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MajorII", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }


    private File createImageFile() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileName = image.getName();
        return image;
    }

}
