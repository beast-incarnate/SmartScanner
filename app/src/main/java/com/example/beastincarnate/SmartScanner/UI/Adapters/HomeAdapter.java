package com.example.beastincarnate.SmartScanner.UI.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.beastincarnate.SmartScanner.R;
import com.example.beastincarnate.SmartScanner.UI.Activities.MainActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeAdapter extends BaseAdapter {
    private static final int REQUEST_IMAGE_CAPTURE_CODE = 1;
    private Context mContext;
    private String[] titles;
    private int[] imageIds;

    public HomeAdapter(Context mContext, String[] titles, int[] imageIds) {
        this.mContext = mContext;
        this.titles = titles;
        this.imageIds = imageIds;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View row = inflater.inflate(R.layout.view,viewGroup,false);
        TextView tv = row.findViewById(R.id.textView);
        tv.setText(titles[i]);
        ImageView imageView = row.findViewById(R.id.imageView);
        imageView.setImageResource(imageIds[i]);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                    File photoFile = null;
                    photoFile = createImageFile();
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(mContext,
                                "com.example.android.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        MainActivity.type = i;
                        //Toast.makeText(mContext, fileName+" "+type, Toast.LENGTH_SHORT).show();
                        Activity activity = (Activity)mContext;
                        activity.startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE_CODE);
                    }
                }

            }
        });
        return row;
    }

    private File createImageFile() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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

        MainActivity.fileName = image.getName();
        return image;
    }


}
