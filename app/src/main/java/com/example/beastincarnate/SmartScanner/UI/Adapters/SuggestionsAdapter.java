package com.example.beastincarnate.SmartScanner.UI.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beastincarnate.SmartScanner.Models.Info;
import com.example.beastincarnate.SmartScanner.Models.Suggestions;
import com.example.beastincarnate.SmartScanner.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class SuggestionsAdapter extends BaseAdapter {

    Suggestions suggestions;
    Context mContext;
    private static final String TAG = "SuggestionsAdapter";

    public SuggestionsAdapter(Suggestions suggestions, Context mContext) {
        this.suggestions = suggestions;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return suggestions.getSuggestions().size();
    }

    @Override
    public Object getItem(int i) {
        return suggestions.getSuggestions().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.view_two,viewGroup,false);
        ImageView imageView = row.findViewById(R.id.imageView2);
        TextView textView = row.findViewById(R.id.textView2);
        TextView textView1 = row.findViewById(R.id.textView3);
        final Info info = suggestions.getSuggestions().get(i);
        Log.d(TAG,info.getImageURL()+" "+info.getPrice());
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(info.getLink()));
                mContext.startActivity(intent);
            }
        });
        textView.setText(info.getTitle());
        textView1.setText(info.getPrice());
        Picasso.get().load(info.getImageURL()).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                Toast.makeText(mContext, "Success!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(mContext, e.getMessage()+"\n"+e.getLocalizedMessage()+"\n"+e.getCause(), Toast.LENGTH_SHORT).show();
            }
        });
        return row;
    }
}
