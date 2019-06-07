package com.example.beastincarnate.SmartScanner.UI.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.example.beastincarnate.SmartScanner.Api.ApiUtils;
import com.example.beastincarnate.SmartScanner.Api.Services.GetClothesTagsService;
import com.example.beastincarnate.SmartScanner.Models.Tags;
import com.example.beastincarnate.SmartScanner.R;
import com.example.beastincarnate.SmartScanner.UI.Adapters.HomeAdapter;
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
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE_CODE = 1;
    private ListView mListView;
    private Observable<Tags> mObservable;
    private GetClothesTagsService getClothesTagsService;
    private static final String TAG = "MainActivity";
    private HomeAdapter homeAdapter;
    public static String fileName = "";
    public static int type;
    private ProgressDialog mDialog;
    static String colors[] = {"Black","Black","Blue","Black","Black"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("value",0);
        editor.commit();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_EXTERNAL_STORAGE);
        } else {
            setUpUI();
        }
    }

    private void setUpUI() {
        mListView = findViewById(R.id.list_view_1);
        String titles[] = {"Scan QR Code", "Copy Text", "Find Similar Products"};
        int imageIDs[] = {R.drawable.qr_color, R.drawable.txt_color, R.drawable.similar};
        homeAdapter = new HomeAdapter(this, titles, imageIDs);
        mListView.setAdapter(homeAdapter);
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("Please Wait!!");
        mDialog.setMessage("Identifying the Clothing..");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_EXTERNAL_STORAGE && resultCode == RESULT_OK) {
            setUpUI();
        } else if(requestCode == REQUEST_EXTERNAL_STORAGE && resultCode == RESULT_CANCELED){
            Toast.makeText(this, "Permission necessary for app to function 2", Toast.LENGTH_SHORT).show();
        }

        if(requestCode == REQUEST_IMAGE_CAPTURE_CODE && resultCode == RESULT_OK){
            if(!fileName.isEmpty()){
                String path = "/storage/emulated/0/Android/data/com.example.beastincarnate.majorii/files/"+"Pictures/"+fileName;
                File image = new File(path);
                switch (type){
                    case 0:
                        extractBarCode(Uri.fromFile(image));
                        break;
                    case 1:
                        extractText(Uri.fromFile(image));
                        break;
                    case 2:
                        requestTagsForImage(image);
                        break;
                }
            }else{
                Toast.makeText(this, "File is Empty!!"+" "+type, Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == REQUEST_IMAGE_CAPTURE_CODE && resultCode == RESULT_CANCELED){
            Toast.makeText(this, "Please take image!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestTagsForImage(File image) {

        RequestBody imageFile = RequestBody.create(MediaType.parse("image/jpg"),image);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image",image.getName(),imageFile);
        RequestBody imageDesc = RequestBody.create(MediaType.parse("text/plain"),"Clothes");
        getClothesTagsService = ApiUtils.getClothesTagsService();
        mObservable = getClothesTagsService.getClothesTags(imagePart,imageDesc);
        mObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Tags>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if(mDialog != null)
                            mDialog.show();
                        Log.d(TAG,"Subscribed");
                    }

                    @Override
                    public void onNext(Tags tags) {
                        Intent intent = new Intent(MainActivity.this,SuggestionActivity.class);
                        intent.putExtra("tag",tags.getTag());
                        SharedPreferences preferences = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                        int val = preferences.getInt("value",0);
                        intent.putExtra("color",colors[val%5]);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("value",val+1);
                        editor.commit();
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, tags.getTag() , Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Toast.makeText(MainActivity.this, "Error in fetching tags!!"+" "+e.getMessage()+" "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG,e.getMessage()+ "\n"+e.getLocalizedMessage()+"\n"+e.getCause());
                    }

                    @Override
                    public void onComplete() {
                        if(mDialog != null)
                            mDialog.dismiss();
                        unsubscribe();
                    }
                });

    }

    private void unsubscribe(){
        if(mObservable != null)
            mObservable.unsubscribeOn(Schedulers.io());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribe();
    }

    private void extractBarCode(Uri uri) {
        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this,uri);
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
                            if(mDialog.isShowing())
                                mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed to extract Barcode!!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private void printBarcodes(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
        String out = "";
        Toast.makeText(this, "Came1", Toast.LENGTH_SHORT).show();
        for(FirebaseVisionBarcode barcode : firebaseVisionBarcodes){
            out = out + barcode.getDisplayValue()+"\n";
            Log.d("Barcode: "," "+barcode.getDisplayValue()+" "+barcode.getRawValue());
        }
        Toast.makeText(this, out, Toast.LENGTH_SHORT).show();
        if(mDialog.isShowing())
            mDialog.dismiss();
    }

    private void extractText(Uri uri) {
//        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
//                .setWidth(480)   // 480x360 is typically sufficient for
//                .setHeight(360)  // image recognition
//                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
//                //.setRotation(rotation)
//                .build();
        FirebaseVisionImage firebaseVisionImage = null;
        try {
            firebaseVisionImage = (FirebaseVisionImage) FirebaseVisionImage.fromFilePath(this,uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                            if(mDialog.isShowing())
                                mDialog.dismiss();
                            finish();
                            Toast.makeText(MainActivity.this, "Failed to extract text", Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void copyTextToClipboard(FirebaseVisionText result) {
        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text",result.getText());
        clipboardManager.setPrimaryClip(clip);
        if(mDialog.isShowing())
            mDialog.dismiss();
        Toast.makeText(MainActivity.this, "Text successfully copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
