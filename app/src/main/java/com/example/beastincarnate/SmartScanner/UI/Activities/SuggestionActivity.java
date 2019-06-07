package com.example.beastincarnate.SmartScanner.UI.Activities;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.example.beastincarnate.SmartScanner.Api.ApiUtils;
import com.example.beastincarnate.SmartScanner.Api.Services.GetClothesSuggestionsService;
import com.example.beastincarnate.SmartScanner.Models.Suggestions;
import com.example.beastincarnate.SmartScanner.R;
import com.example.beastincarnate.SmartScanner.UI.Adapters.SuggestionsAdapter;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SuggestionActivity extends AppCompatActivity {
    private Observable<Suggestions> mObservable;
    private GetClothesSuggestionsService getClothesSuggestionsService;
    private static final String TAG = "SuggestionActivity";
    private ProgressDialog mDialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);
        String tag = getIntent().getStringExtra("tag");
        String color = getIntent().getStringExtra("color");
        final ListView listView = findViewById(R.id.lv_suggestions);
        if(!tag.isEmpty()){
            mDialog = new ProgressDialog(this);
            mDialog.setTitle("Please Wait!!");
            mDialog.setMessage("Fetching Suggestions!!");
            getClothesSuggestionsService = ApiUtils.getClothesSuggestionsService();
            mObservable = getClothesSuggestionsService.getClothesSuggestionsService(tag,color);
            mObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Suggestions>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            mDialog.show();
                            Log.d(TAG,"Subscribed");
                        }

                        @Override
                        public void onNext(Suggestions suggestions) {
                            SuggestionsAdapter suggestionsAdapter = new SuggestionsAdapter(suggestions,SuggestionActivity.this);
                            listView.setAdapter(suggestionsAdapter);
                        }

                        @Override
                        public void onError(Throwable e) {
                            if(mDialog != null)
                                mDialog.dismiss();
                            Log.d(TAG,e.getMessage()+"\n"+e.getLocalizedMessage()+" "+"\n"+e.getCause());
                            Toast.makeText(SuggestionActivity.this, "Error Occurred!!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {
                            if(mDialog != null)
                                mDialog.dismiss();
                            unsubscribe();
                        }
                    });
        }
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
}
