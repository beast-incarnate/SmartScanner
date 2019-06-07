package com.example.beastincarnate.SmartScanner.Api.Services;

import com.example.beastincarnate.SmartScanner.Models.Suggestions;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetClothesSuggestionsService {

    @GET("get_suggestions")
    Observable<Suggestions> getClothesSuggestionsService(@Query("tag") String tag,
                                                                    @Query("color") String color);


}
