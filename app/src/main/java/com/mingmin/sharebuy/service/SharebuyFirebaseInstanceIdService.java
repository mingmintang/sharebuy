package com.mingmin.sharebuy.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.mingmin.sharebuy.cloud.Fdb;

public class SharebuyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        sendRegistrationToFirebase();
    }

    private static DatabaseReference getDatabaseReference() {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        String token = FirebaseInstanceId.getInstance().getToken();
        DatabaseReference ref = null;
        if (fuser != null && token != null) {
            ref = Fdb.getUserTokenRef(fuser.getUid(), token);
        }
        return ref;
    }

    public static void sendRegistrationToFirebase() {
        DatabaseReference ref = getDatabaseReference();
        if (ref != null) {
            ref.setValue(true);
        }
    }

    public static void checkRegistrationUpdated() {
        final DatabaseReference ref = getDatabaseReference();
        if (ref != null) {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        ref.setValue(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
