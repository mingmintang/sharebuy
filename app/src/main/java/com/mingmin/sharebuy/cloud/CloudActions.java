package com.mingmin.sharebuy.cloud;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mingmin.sharebuy.User;
import com.mingmin.sharebuy.notification.GroupNotification;
import com.mingmin.sharebuy.notification.Notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CloudActions {

    private static CloudActions instance;
    private final Fdb fdb;

    public static CloudActions getInstance() {
        if (instance == null) {
            instance = new CloudActions();
        }
        return instance;
    }

    private CloudActions() {
        fdb = Fdb.getInstance();
    }

    public Task<Void> exitGroup(String groupId, String uid) {
        Task<Void> deleteMember = fdb.getGroupMemberRef(groupId, uid).removeValue();
        Task<Void> deleteGroupInUser = fdb.getUserGroupRef(groupId, uid).removeValue();

        return Tasks.whenAll(deleteMember, deleteGroupInUser);
    }

    public Task<HashMap<String, Member>> readGroupMembers(String groupId) {
        final TaskCompletionSource<HashMap<String, Member>> dbSource = new TaskCompletionSource<>();
        fdb.getGroupMembersRef(groupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, Member> members = new HashMap<>();
                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            Member member = childSnap.getValue(Member.class);
                            members.put(childSnap.getKey(), member);
                        }
                        dbSource.setResult(members);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dbSource.setException(databaseError.toException());
                    }
                });

        return dbSource.getTask();
    }

    public Task<Order> readGroupOrder(String groupId, String orderId) {
        final TaskCompletionSource<Order> dbSource = new TaskCompletionSource<>();
        fdb.getGroupOrderRef(groupId, orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Order order = dataSnapshot.getValue(Order.class);
                        dbSource.setResult(order);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dbSource.setException(databaseError.toException());
                    }
                });

        return dbSource.getTask();
    }

    public Task<Integer> readGroupOrderBuyCount(String groupId, String orderId) {
        final TaskCompletionSource<Integer> dbSource = new TaskCompletionSource<>();
        fdb.getGroupOrderBuyCountRef(groupId, orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int buyCount = (int) dataSnapshot.getValue();
                        dbSource.setResult(buyCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dbSource.setException(databaseError.toException());
                    }
                });

        return dbSource.getTask();
    }

    public Task<Boolean> buyGroupOrder(final String groupId, final String orderId, final String uid, final int buyCount) {
        final TaskCompletionSource<Boolean> dbSource = new TaskCompletionSource<>();
        fdb.getGroupOrderSyncRef(groupId, orderId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Map<String, Long> sync = (HashMap<String, Long>) mutableData.getValue();
                if (sync != null) {
                    long state = sync.get(Order.KEY_STATE);
                    long maxBuyCount = sync.get(Order.KEY_MAX_BUY_COUNT);
                    long boughtCount = sync.get(Order.KEY_BUY_COUNT);
                    if (state == Order.STATE_TAKE) {
                        if (maxBuyCount == -1) { // no limit max buy count
                            mutableData.child(Order.KEY_BUY_COUNT).setValue(boughtCount + buyCount);
                            return Transaction.success(mutableData);
                        } else {
                            long restCount = maxBuyCount - boughtCount;
                            if (restCount > 0 && buyCount <= restCount) {
                                mutableData.child(Order.KEY_BUY_COUNT).setValue(boughtCount + buyCount);
                                return Transaction.success(mutableData);
                            }
                        }
                    }
                }
                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean isSucceeded, DataSnapshot dataSnapshot) {
                if (isSucceeded) {
                    Buyer buyer = new Buyer(uid, buyCount, buyCount);
                    fdb.getGroupOrderBuyersRef(groupId, orderId).child(uid).setValue(buyer)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dbSource.setResult(true);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dbSource.setResult(false);
                                }
                            });
                } else {
                    dbSource.setResult(false);
                }

            }
        });

        return dbSource.getTask();
    }

//    public static Task<ArrayList<Order>> getUserEndOrders(String uid, long endTime) {
//        TaskCompletionSource<ArrayList<Order>> dbSource = new TaskCompletionSource<>();
//        fdb.getUserEndOrdersRef(uid).
//    }
}
