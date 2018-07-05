package com.mingmin.sharebuy.cloud;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.mingmin.sharebuy.User;

import java.util.HashMap;

public class Clouds {
    private static Clouds instance;
    private final Fs fs;

    public static Clouds getInstance() {
        if (instance == null) {
            instance = new Clouds();
        }
        return instance;
    }

    public Clouds() {
        fs = Fs.getInstance();
    }

    public class InitUserDataResult {
        public static final int STATUS_FIRST_LOGIN = 0;
        public static final int STATUS_UPDATE_TOKEN = 1;
        public static final int STATUS_NO_UPDATE = 2;

        public int status;
        public String nickname;

        public InitUserDataResult(int status, String nickname) {
            this.status = status;
            this.nickname = nickname;
        }
    }

    /**
     * 1. check first login
     * 2. check token is updated
     * return InitUserDataResult
     */
    public Task<InitUserDataResult> initUserData(final FirebaseUser fuser, final String token) {
        final TaskCompletionSource<InitUserDataResult> dbSource = new TaskCompletionSource<>();
        final DocumentReference ref = fs.getUserRef(fuser.getUid());
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    final String nickname = (String) documentSnapshot.get("nickname");
                    if (!documentSnapshot.contains("tokens." + token)) {
                        ref.update("tokens." + token, true)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        InitUserDataResult result = new InitUserDataResult(
                                                InitUserDataResult.STATUS_UPDATE_TOKEN, nickname);
                                        dbSource.setResult(result);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dbSource.setException(e);
                                    }
                                });
                    } else {
                        InitUserDataResult result = new InitUserDataResult(
                                InitUserDataResult.STATUS_NO_UPDATE, nickname);
                        dbSource.setResult(result);
                    }
                } else {
                    // first login
                    final String nickname = fuser.getEmail().split("@")[0];
                    UserDoc userDoc = new UserDoc();
                    userDoc.setNickname(nickname);
                    userDoc.addToken(token);
                    ref.set(userDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            InitUserDataResult result = new InitUserDataResult(
                                    InitUserDataResult.STATUS_FIRST_LOGIN, nickname);
                            dbSource.setResult(result);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dbSource.setException(e);
                        }
                    });
                }
            }
        });

        return dbSource.getTask();
    }

    public Task<Void> updateUserNickname(String uid, String nickname) {
        return fs.getUserRef(uid).update("nickname", nickname);
    }

    public Task<Void> updateUserToken(String uid, String token) {
        return fs.getUserRef(uid).update("tokens." + token, true);
    }
}
