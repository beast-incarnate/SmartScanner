package com.example.beastincarnate.SmartScanner.Api;

import com.example.beastincarnate.SmartScanner.Api.Services.GetClothesSuggestionsService;
import com.example.beastincarnate.SmartScanner.Api.Services.GetClothesTagsService;
import com.example.beastincarnate.SmartScanner.Api.Services.RetrofitClient;

public class ApiUtils {

    public ApiUtils(){

    }

    public static final String BASE_URL = "http://192.168.43.151:3000";

    public static GetClothesTagsService getClothesTagsService() {
        return RetrofitClient.getClient(BASE_URL).create(GetClothesTagsService.class);
    }

    public static GetClothesSuggestionsService getClothesSuggestionsService() {
        return RetrofitClient.getClient(BASE_URL).create(GetClothesSuggestionsService.class);
    }
}