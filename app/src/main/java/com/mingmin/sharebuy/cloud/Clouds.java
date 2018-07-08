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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.mingmin.sharebuy.Group;
import com.mingmin.sharebuy.User;
import com.mingmin.sharebuy.notification.GroupNotification;
import com.mingmin.sharebuy.notification.Notification;
import com.mingmin.sharebuy.utils.InternetCheck;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class Clouds {
    private final String TAG = getClass().getSimpleName();
    private static Clouds instance;
    private final Fs fs;
    private ListenerRegistration userGroupsRegistration;

    public static Clouds getInstance() {
        if (instance == null) {
            instance = new Clouds();
        }
        return instance;
    }

    private Clouds() {
        fs = Fs.getInstance();
    }

    public class InitUserDataResult {
        public static final int STATUS_FIRST_LOGIN = 0;
        public static final int STATUS_UPDATE_TOKEN = 1;
        public static final int STATUS_NO_UPDATE = 2;

        public int status;
        public String nickname;

        private InitUserDataResult(int status, String nickname) {
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

    public Task<Void> createNewGroup(String groupName, User user) {
        WriteBatch batch = fs.getWriteBatch();

        DocumentReference addGroupRef = fs.getGroupsRef().document();
        GroupDoc groupDoc = new GroupDoc(groupName, user.getUid(), user.getNickname());
        DocumentReference addGroupInUserRef = fs.getUserGroupRef(user.getUid(), addGroupRef.getId());
        batch.set(addGroupRef, groupDoc);
        batch.set(addGroupInUserRef, groupDoc);

        return batch.commit();
    }

    public interface UserGroupsListener {
        void onUserGroupsChanged(ArrayList<Group> groups);
        void onUserGroupsIsEmpty();
    }

    public void addUserGroupsListener(String uid, final UserGroupsListener listener) {
        userGroupsRegistration = fs.getUserGroupsRef(uid).orderBy("createTime", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "user groups listen failed.", e);
                            return;
                        }
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Group> groups = new ArrayList<>();
                            List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                GroupDoc groupDoc = documentSnapshot.toObject(GroupDoc.class);
                                Group group = new Group(documentSnapshot.getId(), groupDoc);
                                groups.add(group);
                            }
                            listener.onUserGroupsChanged(groups);
                        } else {
                            Log.d(TAG, "Query user groups is empty.");
                            listener.onUserGroupsIsEmpty();
                        }
                    }
                });
    }

    public void removeUserGroupsListener() {
        if (userGroupsRegistration != null) {
            userGroupsRegistration.remove();
        }
    }

    public Task<ArrayList<Group>> searchGroupsBySearchCode(int searchCode) {
        final TaskCompletionSource<ArrayList<Group>> dbSource = new TaskCompletionSource<>();
        fs.getGroupsRef().whereEqualTo("searchCode", searchCode).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Group> groups = new ArrayList<>();
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                GroupDoc groupDoc = documentSnapshot.toObject(GroupDoc.class);
                                Group group = new Group(documentSnapshot.getId(), groupDoc);
                                groups.add(group);
                            }
                        } else {
                            Log.d(TAG, "Query search code is empty.");
                        }
                        dbSource.setResult(groups);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dbSource.setException(e);
                    }
                });

        return dbSource.getTask();
    }

    public Task<Void> requestJoinGroup(final Group group, String uid) {
        final TaskCompletionSource<Void> dbSource = new TaskCompletionSource<>();
        final GroupNotification notification = new GroupNotification(
                uid,
                group.getFounderUid(),
                Notification.ACTION_REQUEST_JOIN_GROUP,
                group.getId());

        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(InternetCheck.Result result) {
                if(result.hasInternet) {
                    fs.getRequestJoinGroupRef(group.getId()).add(notification)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    dbSource.setResult(null);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dbSource.setException(e);
                                }
                            });
                } else {
                    dbSource.setException(result.ioException);
                }
            }
        });

        return dbSource.getTask();
    }

    public Task<Group> getGroupByGroupId(String groupId) {
        final TaskCompletionSource<Group> dbSource = new TaskCompletionSource<>();
        fs.getGroupRef(groupId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        GroupDoc groupDoc = documentSnapshot.toObject(GroupDoc.class);
                        Group group = new Group(documentSnapshot.getId(), groupDoc);
                        dbSource.setResult(group);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dbSource.setException(e);
                    }
                });

        return dbSource.getTask();
    }
}
