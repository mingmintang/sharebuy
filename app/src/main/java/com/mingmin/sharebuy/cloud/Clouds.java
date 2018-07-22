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
import com.google.firebase.firestore.FieldValue;
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
import com.mingmin.sharebuy.Order;
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

        private InitUserDataResult(int status) {
            this.status = status;
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
                    if (!documentSnapshot.contains("tokens." + token)) {
                        ref.update("tokens." + token, true)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        InitUserDataResult result = new InitUserDataResult(
                                                InitUserDataResult.STATUS_UPDATE_TOKEN);
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
                                InitUserDataResult.STATUS_NO_UPDATE);
                        source.setResult(result);
                    }
                } else {
                    // first login
                    UserDoc userDoc = new UserDoc();
                    userDoc.addToken(token);
                    ref.set(userDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            InitUserDataResult result = new InitUserDataResult(
                                    InitUserDataResult.STATUS_FIRST_LOGIN);
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

    public Task<Void> updateUserToken(String uid, String token) {
        return fs.getUserDoc(uid).update("tokens." + token, true);
    }

    public Task<Void> createNewGroup(String groupName, String managerUid, String managerName) {
        WriteBatch batch = fs.getWriteBatch();

        DocumentReference groupRef = fs.getGroupsCol().document();
        GroupDoc groupDoc = new GroupDoc(groupName, managerUid, managerName);
        DocumentReference userGroupRef = fs.getUserGroupDoc(managerUid, groupRef.getId());
        UserGroupDoc userGroupDoc = new UserGroupDoc(groupName, managerUid, managerName);

        batch.set(groupRef, groupDoc);
        batch.set(userGroupRef, userGroupDoc);

        return batch.commit();
    }

    public interface UserGroupsListener {
        void onUserGroupsChanged(ArrayList<Group> groups);
    }

    public void addUserGroupsListener(final String uid, final UserGroupsListener listener) {
        userGroupsRegistration = fs.getUserGroupsCol(uid).orderBy("joinTime", Query.Direction.DESCENDING)
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
                                String name = documentSnapshot.getString("name");
                                String managerUid = documentSnapshot.getString("managerUid");
                                String managerName = documentSnapshot.getString("managerName");
                                Group group = new Group(documentSnapshot.getId(), name, managerUid, managerName);
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
                                String name = documentSnapshot.getString("name");
                                String managerUid = documentSnapshot.getString("managerUid");
                                String managerName = documentSnapshot.getString("managerName");
                                Group group = new Group(documentSnapshot.getId(), name, managerUid, managerName);
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

    public Task<ArrayList<Group>> getUserGroups(final String uid) {
        final TaskCompletionSource<ArrayList<Group>> source = new TaskCompletionSource<>();
        fs.getUserGroupsCol(uid).orderBy("joinTime", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Group> groups = new ArrayList<>();
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                                String name = snap.getString("name");
                                String managerUid = snap.getString("managerUid");
                                String managerName = snap.getString("managerName");
                                String myName;
                                if (managerUid.equals(uid)) {
                                    myName = managerName;
                                } else {
                                    myName = snap.getString("myName");
                                }
                                Group group = new Group(snap.getId(), name, managerUid, managerName, myName);
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

    public Task<Void> requestJoinGroup(final Group group, String uid, String myName) {
        final TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        final GroupNotification notification = new GroupNotification(
                uid,
                group.getManagerUid(),
                Notification.ACTION_REQUEST_JOIN_GROUP,
                group.getId(),
                myName);

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

    public Task<Integer> getGroupSearchCode(String groupId) {
        final TaskCompletionSource<Integer> source = new TaskCompletionSource<>();
        fs.getGroupDoc(groupId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        int searchCode = documentSnapshot.getLong("searchCode").intValue();
                        source.setResult(searchCode);
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

    public Task<Group> getGroupByGroupId(String groupId) {
        final TaskCompletionSource<Group> source = new TaskCompletionSource<>();
        fs.getGroupDoc(groupId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString("name");
                        String managerUid = documentSnapshot.getString("managerUid");
                        String managerName = documentSnapshot.getString("managerName");
                        Group group = new Group(documentSnapshot.getId(), name, managerUid, managerName);
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
        joiningGroupMembersRegistration = fs.getGroupJoiningMembersCol(groupId)
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
                                String name = documentSnapshot.getString("name");
                                Member member = new Member(documentSnapshot.getId(), name);
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
        joinedGroupMembersRegistration = fs.getGroupMembersCol(groupId)
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
                                String name = documentSnapshot.getString("name");
                                Member member = new Member(documentSnapshot.getId(), name);
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
        fs.getGroupMembersCol(groupId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Member> members = new ArrayList<>();
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                                String name = snap.getString("name");
                                Member member = new Member(snap.getId(), name);
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

    public Task<Boolean> checkMemberNameDuplicate(Group group, String memberName) {
        final TaskCompletionSource<Boolean> source = new TaskCompletionSource<>();
        if (group.getManagerName().equals(memberName)) {
            source.setResult(true);
            return source.getTask();
        }
        fs.getGroupMembersCol(group.getId()).whereEqualTo("name", memberName).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: empty");
                            source.setResult(false);
                        } else {
                            String result = queryDocumentSnapshots.getDocuments().get(0).getId();
                            Log.d(TAG, "onSuccess: duplicate=" + result);
                            source.setResult(true);
                        }
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

    public Task<Void> acceptJoinGroup(Group group, String memberUid, String memberName) {
        WriteBatch batch = fs.getWriteBatch();
        DocumentReference groupMemberRef = fs.getGroupMemberDoc(group.getId(), memberUid);
        DocumentReference joiningMemberRef = fs.getGroupJoiningMemberDoc(group.getId(), memberUid);
        DocumentReference userGroupRef = fs.getUserGroupDoc(memberUid, group.getId());
        MemberDoc memberDoc = new MemberDoc(memberName);
        UserGroupDoc userGroupDoc = new UserGroupDoc(group.getName(), group.getManagerUid(), group.getManagerName(), memberName);

        batch.set(groupMemberRef, memberDoc);
        batch.delete(joiningMemberRef);
        batch.set(userGroupRef, userGroupDoc);

        return batch.commit();
    }

    public Task<Void> declineJoinGroup(String groupId, String memberUid) {
        return fs.getGroupJoiningMemberDoc(groupId, memberUid).delete();
    }

    public Task<Void> exitGroup(String groupId, String memberUid) {
        WriteBatch batch = fs.getWriteBatch();
        DocumentReference groupMemberRef = fs.getGroupMemberDoc(groupId, memberUid);
        DocumentReference userGroupRef = fs.getUserGroupDoc(memberUid, groupId);

        batch.delete(groupMemberRef);
        batch.delete(userGroupRef);

        return batch.commit();
    }

    public Task<Void> buildGroupOrder(final int orderState, String imagePath, final String uid, final GroupOrderDoc groupOrderDoc, final Group group) {
        final StorageReference imagePathRef = Storage.getInstance().createOrderImagePathRef();
        final File imageFile = new File(imagePath);
        UploadTask uploadImageTask = imagePathRef.putFile(Uri.fromFile(imageFile));
        Task<Void> buildGroupOrderTask = uploadImageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                imageFile.delete();
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                Uri downloadUri = task.getResult();
                groupOrderDoc.setImageUrl(downloadUri.toString());

                WriteBatch batch = fs.getWriteBatch();
                // Create order id
                DocumentReference userOrderRef = fs.getUserOrdersCol(uid).document();
                String orderId = userOrderRef.getId();

                groupOrderDoc.setState(orderState);
                groupOrderDoc.setGroupId(group.getId());
                groupOrderDoc.setManagerUid(uid);
                groupOrderDoc.setManagerName(group.getMyName());
                DocumentReference groupOrderRef = fs.getGroupOrderDoc(group.getId(), orderId);
                batch.set(groupOrderRef, groupOrderDoc);

                if (groupOrderDoc.getBuyCount() > 0) {
                    DocumentReference orderBuyerRef = fs.getGroupOrderBuyerDoc(group.getId(), orderId, uid);
                    BuyerDoc buyerDoc = new BuyerDoc(group.getMyName(), groupOrderDoc.getBuyCount(), groupOrderDoc.getBuyCount());
                    batch.set(orderBuyerRef, buyerDoc);
                }

                UserOrderDoc userOrderDoc = new UserOrderDoc(group.getMyName(), group.getId());
                batch.set(userOrderRef, userOrderDoc);
                return batch.commit();
            }
        });

        return buildGroupOrderTask;
    }

    public Task<DocumentReference> buildPersonalOrder(final UserEndOrderDoc.Personal personalOrder, final String imagePath, final String uid) {
        final StorageReference imagePathRef = Storage.getInstance().createOrderImagePathRef();
        final File imageFile = new File(imagePath);
        UploadTask uploadImageTask = imagePathRef.putFile(Uri.fromFile(imageFile));
        Task<DocumentReference> buildPersonalOrderTask = uploadImageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imagePathRef.getDownloadUrl();
            }
        }).continueWithTask(new Continuation<Uri, Task<DocumentReference>>() {
            @Override
            public Task<DocumentReference> then(@NonNull Task<Uri> task) throws Exception {
                imageFile.delete();
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                Uri downloadUri = task.getResult();
                personalOrder.setImageUrl(downloadUri.toString());
                UserEndOrderDoc userEndOrderDoc = new UserEndOrderDoc(personalOrder);
                return fs.getUserEndOrdersCol(uid).add(userEndOrderDoc);
            }
        });

        return buildPersonalOrderTask;
    }

    public Query getGroupOrdersQuery(String groupId) {
        return fs.getGroupOrdersCol(groupId).orderBy("updateTime", Query.Direction.DESCENDING).limit(30);
    }

    public Task<ArrayList<Buyer>> getOrderBuyersForDisplay(String groupId, String orderId) {
        final TaskCompletionSource<ArrayList<Buyer>> source = new TaskCompletionSource<>();
        fs.getGroupOrderBuyersCol(groupId, orderId).orderBy("orderTime", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Buyer> buyers = new ArrayList<>();
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                                String name = snap.getString("name");
                                int buyCount = snap.getLong("buyCount").intValue();
                                Buyer buyer = new Buyer(snap.getId(), name, buyCount);
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

    // First buy the order for group member
    public Task<Void> buyGroupOrder(final String groupId, final String orderId, final String uid, final String myName, final int buyCount) {
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
                WriteBatch batch = fs.getWriteBatch();
                DocumentReference userOrderRef = fs.getUserOrderDoc(uid, orderId);
                DocumentReference orderBuyerRef = fs.getGroupOrderBuyerDoc(groupId, orderId, uid);

                UserOrderDoc userOrderDoc = new UserOrderDoc(myName, groupId);
                batch.set(userOrderRef, userOrderDoc);
                batch.set(orderBuyerRef, new BuyerDoc(myName, buyCount, buyCount));
                return batch.commit();
            }
        });

        return buyGroupOrderTask;
    }

    public Query getUserOrdersQuery(String uid) {
        return fs.getUserOrdersCol(uid).orderBy("updateTime");
    }

    public Task<Void> endGroupOrder(final String groupId, final String orderId, final String uid) {
        final DocumentReference groupOrderDoc = fs.getGroupOrderDoc(groupId, orderId);
        Task<Void> endGroupOrderTask = fs.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                transaction.update(groupOrderDoc, "state", Order.STATE_END,
                        "updateTime", FieldValue.serverTimestamp());
                return null;
            }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                WriteBatch batch = fs.getWriteBatch();
                DocumentReference userOrderRef = fs.getUserOrderDoc(uid, orderId);
                DocumentReference userEndOrderRef = fs.getUserEndOrderDoc(uid, orderId);

                batch.delete(userOrderRef);
                UserEndOrderDoc userEndOrderDoc = new UserEndOrderDoc(groupId);
                batch.set(userEndOrderRef, userEndOrderDoc);

                return batch.commit();
            }
        });

        return endGroupOrderTask;
    }
}
