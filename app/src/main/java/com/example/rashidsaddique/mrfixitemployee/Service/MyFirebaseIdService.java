package com.example.rashidsaddique.mrfixitemployee.Service;

import com.example.rashidsaddique.mrfixitemployee.Common.Common;
import com.example.rashidsaddique.mrfixitemployee.Model.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        updateTokenToServer(refreshedToken); //update token realtimme database

    }

    private void updateTokenToServer(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(refreshedToken);
        if(FirebaseAuth.getInstance().getCurrentUser() != null) //if alredy login update  Token
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .setValue(token);
    }
}
