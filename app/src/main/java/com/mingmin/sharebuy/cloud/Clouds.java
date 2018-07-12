package com.mingmin.sharebuy.cloud;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mingmin.sharebuy.Buyer;
import com.mingmin.sharebuy.Group;
import com.mingmin.sharebuy.Member;
import com.mingmin.sharebuy.User;
import com.mingmin.sharebuy.notification.GroupNotification;
import com.mingmin.sharebuy.notification.Notification;
import com.mingmin.sharebuy.utils.InternetCheck;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Clouds {
    private final String TAG = getClass().getSimpleName();
    private static Clouds instance;
    private final Firestore fs;
    private ListenerRegistration userGroupsRegistration;
    private ListenerRegistration joiningGroupMembersRegistration;
    private ListenerRegistration joinedGroupMembersRegistration;

    public static Clouds getInstance() {
        if (instance == null) {
            instance = new Clouds();
        }
        return instance;
    }

    private Clouds() {
        fs = Firestore.getInstance();
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
        final TaskCompletionSource<InitUserDataResult> source = new TaskCompletionSource<>();
        final DocumentReference ref = fs.getUserDoc(fuser.getUid());
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
                                        source.setResult(result);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        source.setException(e);
                                    }
                                });
                    } else {
                        InitUserDataResult result = new InitUserDataResult(
                                InitUserDataResult.STATUS_NO_UPDATE, nickname);
                        source.setResult(result);
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
                            source.setResult(result);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            source.setException(e);
                        }
                    });
                }
            }
        });

        return source.getTask();
    }

    public Task<Void> updateUserNickname(String uid, String nickname) {
        return fs.getUserDoc(uid).update("nickname", nickname);
    }

    public Task<Void> updateUserToken(String uid, String token) {
        return fs.getUserDoc(uid).update("tokens." + token, true);
    }

    public Task<Void> createNewGroup(String groupName, User user) {
        WriteBatch batch = fs.getWriteBatch();

        DocumentReference addGroupRef = fs.getGroupsCol().document();
        GroupDoc groupDoc = new GroupDoc(groupName, user.getUid(), user.getNickname());
        DocumentReference addGroupInUserRef = fs.getUserGroupDoc(user.getUid(), addGroupRef.getId());
        batch.set(addGroupRef, groupDoc);
        batch.set(addGroupInUserRef, groupDoc);

        return batch.commit();
    }

    public interface UserGroupsListener {
        void onUserGroupsChanged(ArrayList<Group> groups);
    }

    public void addUserGroupsListener(String uid, final UserGroupsListener listener) {
        userGroupsRegistration = fs.getUserGroupsCol(uid).orderBy("createTime", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "user groups listen failed.", e);
                            return;
                        }
                        ArrayList<Group> groups = new ArrayList<>();
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                GroupDoc groupDoc = documentSnapshot.toObject(GroupDoc.class);
                                Group group = new Group(documentSnapshot.getId(), groupDoc);
                                groups.add(group);
                            }
                        } else {
                            Log.d(TAG, "Query user groups is empty.");
                        }
                        listener.onUserGroupsChanged(groups);
                    }
                });
    }

    public void removeUserGroupsListener() {
        if (userGroupsRegistration != null) {
            userGroupsRegistration.remove();
        }
    }

    public Task<ArrayList<Group>> searchGroupsBySearchCode(int searchCode) {
        final TaskCompletionSource<ArrayList<Group>> source = new TaskCompletionSource<>();
        fs.getGroupsCol().whereEqualTo("searchCode", searchCode).get()
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
                        source.setResult(groups);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        source.setException(e);
                    }
                });

        return source.getTask();
    }

    public Task<ArrayList<Group>> getUserGroups(String uid) {
        final TaskCompletionSource<ArrayList<Group>> source = new TaskCompletionSource<>();
        fs.getUserGroupsCol(uid).orderBy("createTime", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Group> groups = new ArrayList<>();
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                                GroupDoc groupDoc = snap.toObject(GroupDoc.class);
                                Group group = new Group(snap.getId(), groupDoc);
                                groups.add(group);
                            }
                        }
                        source.setResult(groups);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        source.setException(e);
                    }
                });

        return source.getTask();
    }

    public Task<Void> requestJoinGroup(final Group group, String uid) {
        final TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        final GroupNotification notification = new GroupNotification(
                uid,
                group.getFounderUid(),
                Notification.ACTION_REQUEST_JOIN_GROUP,
                group.getId());

        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(InternetCheck.Result result) {
                if(result.hasInternet) {
                    fs.getRequestJoinGroupCol(group.getId()).add(notification)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    source.setResult(null);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    source.setException(e);
                                }
                            });
                } else {
                    source.setException(result.ioException);
                }
            }
        });

        return source.getTask();
    }

    public Task<Group> getGroupByGroupId(String groupId) {
        final TaskCompletionSource<Group> source = new TaskCompletionSource<>();
        fs.getGroupDoc(groupId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        GroupDoc groupDoc = documentSnapshot.toObject(GroupDoc.class);
                        Group group = new Group(documentSnapshot.getId(), groupDoc);
                        source.setResult(group);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        source.setException(e);
                    }
                });

        return source.getTask();
    }

    public interface GroupMembersListener {
        void onJoiningGroupMembersChanged(ArrayList<Member> members);
        void onJoinedGroupMembersChanged(ArrayList<Member> members);
    }

    public void addJoiningGroupMembersListener(String groupId, final GroupMembersListener listener) {
        joiningGroupMembersRegistration = fs.getGroupMembersCol(groupId).whereEqualTo("joined", false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "joining group members listen failed.", e);
                            return;
                        }
                        ArrayList<Member> members = new ArrayList<>();
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                String nickname = (String) documentSnapshot.get("nickname");
                                Member member = new Member(documentSnapshot.getId(), nickname);
                                members.add(member);
                            }
                        } else {
                            Log.d(TAG, "Query joining group members is empty.");
                        }
                        listener.onJoiningGroupMembersChanged(members);
                    }
                });
    }

    public void addJoinedGroupMembersListener(String groupId, final GroupMembersListener listener) {
        joinedGroupMembersRegistration = fs.getGroupMembersCol(groupId).whereEqualTo("joined", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "joined group members listen failed.", e);
                            return;
                        }
                        ArrayList<Member> members = new ArrayList<>();
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                String nickname = (String) documentSnapshot.get("nickname");
                                Member member = new Member(documentSnapshot.getId(), nickname);
                                members.add(member);
                            }
                        } else {
                            Log.d(TAG, "Query joined group members is empty.");
                        }
                        listener.onJoinedGroupMembersChanged(members);
                    }
                });
    }

    public void removeJoiningGroupMembersListener() {
        if (joiningGroupMembersRegistration != null) {
            joiningGroupMembersRegistration.remove();
        }
    }

    public void removeJoinedGroupMembersListener() {
        if (joinedGroupMembersRegistration != null) {
            joinedGroupMembersRegistration.remove();
        }
    }

    public Task<ArrayList<Member>> getJoinedGroupMembers(String groupId) {
        final TaskCompletionSource<ArrayList<Member>> source = new TaskCompletionSource<>();
        fs.getGroupMembersCol(groupId).whereEqualTo("joined", true).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Member> members = new ArrayList<>();
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                                String nickname = (String) snap.get("nickname");
                                Member member = new Member(snap.getId(), nickname);
                                members.add(member);
                            }
                        }
                        source.setResult(members);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        source.setException(e);
                    }
                });

        return source.getTask();
    }

    public Task<Void> acceptJoinGroup(Group group, String uid) {
        WriteBatch batch = fs.getWriteBatch();
        DocumentReference addMemberRef = fs.getGroupMemberDoc(group.getId(), uid);
        DocumentReference addGroupInUserRef = fs.getUserGroupDoc(uid, group.getId());
        batch.update(addMemberRef, "joined", true);
        batch.set(addGroupInUserRef, group.getGroupDoc());

        return batch.commit();
    }

    public Task<Void> declineJoinGroup(String groupId, String uid) {
        return fs.getGroupMemberDoc(groupId, uid).delete();
    }

    public Task<Void> exitGroup(String groupId, String uid) {
        WriteBatch batch = fs.getWriteBatch();
        DocumentReference deleteMemberRef = fs.getGroupMemberDoc(groupId, uid);
        DocumentReference deleteGroupInUserRef = fs.getUserGroupDoc(uid, groupId);
        batch.delete(deleteMemberRef);
        batch.delete(deleteGroupInUserRef);

        return batch.commit();
    }

    public Task<Void> buildNewOrder(final int orderState, String imagePath, final String creatorUid, final OrderDoc orderDoc, @Nullable final String groupId) {
        final StorageReference imagePathRef = Storage.getInstance().createOrderImagePathRef();
        UploadTask uploadImageTask = imagePathRef.putFile(Uri.fromFile(new File(imagePath)));
        Task<Void> buildNewOrderTask = uploadImageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imagePathRef.getDownloadUrl();
            }
        }).continueWithTask(new Continuation<Uri, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Uri> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                Uri downloadUri = task.getResult();
                orderDoc.setImageUrl(downloadUri.toString());

                WriteBatch batch = fs.getWriteBatch();
                DocumentReference addUserOrderRef = fs.getUserOrdersCol(creatorUid).document();
                String orderId = addUserOrderRef.getId();
                orderDoc.setState(orderState);
                switch (orderState) {
                    case Order.STATE_CREATE:
                        orderDoc.setCreatorUid(creatorUid);
                        break;
                    case Order.STATE_TAKE:
                        orderDoc.setCreatorUid(creatorUid);
                        orderDoc.setTakerUid(creatorUid);
                        break;
                    case Order.STATE_END:
                        // End order directly, it would be personal order.
                        orderDoc.setCreatorUid(creatorUid);
                        orderDoc.setTakerUid(creatorUid);
                        orderDoc.setEndTime();
                        break;
                }

                if (groupId != null) {
                    // Group Order
                    orderDoc.setGroupId(groupId);
                    DocumentReference addGroupOrderRef = fs.getGroupOrderDoc(groupId, orderId);
                    batch.set(addGroupOrderRef, orderDoc);
                    if (orderDoc.getBuyCount() > 0) {
                        BuyerDoc buyerDoc = new BuyerDoc(orderDoc.getBuyCount(), orderDoc.getBuyCount());
                        DocumentReference addBuyerRef = fs.getGroupOrderBuyerDoc(groupId, orderId, creatorUid);
                        batch.set(addBuyerRef, buyerDoc);
                    }
                }

                batch.set(addUserOrderRef, orderDoc);
                return batch.commit();
            }
        });

        return buildNewOrderTask;
    }

    public Query getGroupOrdersQuery(String groupId) {
        return fs.getGroupOrdersCol(groupId).orderBy("createTime", Query.Direction.DESCENDING).limit(30);
    }

    public Task<ArrayList<Buyer>> getGroupOrderBuyers(String groupId, String orderId) {
        final TaskCompletionSource<ArrayList<Buyer>> source = new TaskCompletionSource<>();
        fs.getGroupOrderBuyersCol(groupId, orderId).orderBy("orderTime", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Buyer> buyers = new ArrayList<>();
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                                BuyerDoc buyerDoc = snap.toObject(BuyerDoc.class);
                                Buyer buyer = new Buyer(snap.getId(), buyerDoc);
                                buyers.add(buyer);
                            }
                        }
                        source.setResult(buyers);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        source.setException(e);
                    }
                });

        return source.getTask();
    }

    public Task<Void> buyGroupOrder(final String groupId, final String orderId, final String uid, final int buyCount) {
        final DocumentReference groupOrderDoc = fs.getGroupOrderDoc(groupId, orderId);
        Task<Void> buyGroupOrderTask = fs.runTransaction(new Transaction.Function<Long>() {
            @Nullable
            @Override
            public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snap = transaction.get(groupOrderDoc);
                long state = (long) snap.get("state");
                long maxBuyCount = (long) snap.get("maxBuyCount");
                long boughtCount = (long) snap.get("buyCount");
                if (state == com.mingmin.sharebuy.Order.STATE_TAKE) {
                    long amount = boughtCount + buyCount;
                    if (maxBuyCount == 0) { // no limit max buy count
                        transaction.update(groupOrderDoc, "buyCount", amount);
                        return amount;
                    } else {
                        long restCount = maxBuyCount - boughtCount;
                        if (restCount > 0 && buyCount <= restCount) {
                            transaction.update(groupOrderDoc, "buyCount", amount);
                            return amount;
                        }
                        throw new FirebaseFirestoreException("Max Count is not enough", FirebaseFirestoreException.Code.ABORTED);
                    }
                } else {
                    throw new FirebaseFirestoreException("Order is not take state", FirebaseFirestoreException.Code.ABORTED);
                }
            }
        }).continueWithTask(new Continuation<Long, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Long> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fs.getGroupOrderBuyerDoc(groupId, orderId, uid).set(new BuyerDoc(buyCount, buyCount));
            }
        });

        return buyGroupOrderTask;
    }
}
