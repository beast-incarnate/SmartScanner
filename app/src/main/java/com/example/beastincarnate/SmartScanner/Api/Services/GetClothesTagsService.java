package com.example.beastincarnate.SmartScanner.Api.Services;

import com.example.beastincarnate.SmartScanner.Models.Tags;


import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface GetClothesTagsService {

    @Multipart
    @POST("get_tags")
    Observable<Tags> getClothesTags(@Part MultipartBody.Part image, @Part("desc") RequestBody description);


}
