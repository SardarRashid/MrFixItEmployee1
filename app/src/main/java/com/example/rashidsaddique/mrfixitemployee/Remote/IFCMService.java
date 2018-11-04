package com.example.rashidsaddique.mrfixitemployee.Remote;

import com.example.rashidsaddique.mrfixitemployee.Model.FCMResponse;
import com.example.rashidsaddique.mrfixitemployee.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/jason",
            "Authorization:key=AAAALlQXs3A:APA91bESl8v16CvOaNrLHcdmzCwKcV-xtriUj3WvnUNcP-vFB2QG-4uxUP__t-sRSIFAMRr60f0s9RLFOHYV4Hk3DMwiw-beRHlYjOzj82bDhluKcB3wqHV97XFrsvM0sv8sysBc1ic_"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
