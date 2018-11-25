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
            "Authorization:key=AAAALlQXs3A:APA91bESJsKPCRv8kNH7fQYQ-5Z3VBv5rrORfnNyquA2VVhl85Cxnqk4yIQV9ol9VI8tWkXK1IFEsv5ffsSfz9sShEfQWj18jy70N5gbJlMyievhHGQIUG3oI2RWVzBq6vMUrz5OKfti"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
