package com.example.artem.realtimelocationappedmtlesson.Remote;

import com.example.artem.realtimelocationappedmtlesson.Models.MyResponse;
import com.example.artem.realtimelocationappedmtlesson.Models.Request;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
        "Content-Type:application/json",
            "Authorization:key=AAAAoa9GbSI:APA91bE0R2zZN1drM04MXXOOEMbXsxNSgkne63o5QKSnWtV_5AREInFKr-e9ekLQsJqO20vPJrndlS0v2LCdT70Sf8xh-OlP_4Z_j-NK0wZoi5tIcM_YwVyazJ3B5eyVS9tZbrHkSyip"
    })

    @POST("fcm/send")
    Observable<MyResponse> sendFriendRequestToUser(@Body Request body);
}
