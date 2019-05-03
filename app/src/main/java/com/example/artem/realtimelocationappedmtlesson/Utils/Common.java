package com.example.artem.realtimelocationappedmtlesson.Utils;

import com.example.artem.realtimelocationappedmtlesson.Models.User;
import com.example.artem.realtimelocationappedmtlesson.Remote.IFCMService;
import com.example.artem.realtimelocationappedmtlesson.Service.RetrofitClient;

import retrofit2.Retrofit;

public class Common {
    public static final String USER_INFO = "UserInformation";
    public static final String USER_UID_SAVE_KEY = "SaveUid";
    public static final String TOKENS = "Tokens";
    public static final String FROM_NAME = "FromName";
    public static final String ACCEPT_LIST = "acceptList";
    public static final String FROM_UID = "FromUid";
    public static final String TO_UID = "ToUid";
    public static final String TO_NAME = "ToName";
    public static User loggedUser;

    public static IFCMService getIFCMService(){
        return RetrofitClient.getClient("https://fcm.googleapis.com/")
                .create(IFCMService.class);
    }
}
