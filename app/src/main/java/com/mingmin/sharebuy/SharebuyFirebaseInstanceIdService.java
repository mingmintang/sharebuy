package com.mingmin.sharebuy;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class SharebuyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        sendRegistrationToFirebase();
    }

    private static DatabaseReference getDatabaseReference() {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = null;
        if (fuser != null) {
            ref = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(fuser.getUid())
                    .child("data")
                    .child("token");
        }
        return ref;
    }

    public static void sendRegistrationToFirebase() {
        DatabaseReference ref = getDatabaseReference();
        if (ref != null) {
            ref.setValue(FirebaseInstanceId.getInstance().getToken());
        }
    }

    public static void checkRegistrationUpdated() {
        final DatabaseReference ref = getDatabaseReference();
        if (ref != null) {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String preToken = (String) dataSnapshot.getValue();
                    String token = FirebaseInstanceId.getInstance().getToken();
                    if (preToken == null || !preToken.equals(token)) {
                        ref.setValue(token);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
