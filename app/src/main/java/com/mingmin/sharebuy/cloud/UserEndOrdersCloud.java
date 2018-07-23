package com.mingmin.sharebuy.cloud;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.mingmin.sharebuy.Order;
import com.mingmin.sharebuy.database.Buyer;
import com.mingmin.sharebuy.database.Db;
import com.mingmin.sharebuy.database.EndOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public class UserEndOrdersCloud {
    private final String TAG = getClass().getSimpleName();
    private Firestore fs;
    private Db db;
    private ListenerRegistration registration;

    public UserEndOrdersCloud(Context context) {
        fs = Firestore.getInstance();
        db = Db.getInstance(context);
    }

    public interface UserEndOrdersListener {
        void onUserOrderChanged(Order order);
    }

    public void addUserEndOrdersListener(String uid, final Date updateTime) {
        if (registration != null) {
            return;
        }
        registration = fs.getUserEndOrdersCol(uid).whereGreaterThan("updateTime", updateTime)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "user end orders listen failed.", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                                Date updateTime = snap.getDate("updateTime");
                                String orderId = snap.getId();
                                String groupId = snap.getString("groupId");
                                if (groupId != null) {
                                    storeGroupOrderData(orderId, groupId, updateTime);
                                } else {
                                    UserEndOrderDoc.Personal personalOrder = new UserEndOrderDoc.Personal();
                                    personalOrder.setBuyCount(snap.getLong("personal.buyCount").intValue());
                                    personalOrder.setImageUrl(snap.getString("personal.imageUrl"));
                                    personalOrder.setName(snap.getString("personal.name"));
                                    personalOrder.setDesc(snap.getString("personal.desc"));
                                    personalOrder.setPrice(snap.getLong("personal.price").intValue());
                                    personalOrder.setCoinUnit(snap.getLong("personal.coinUnit").intValue());
                                    storePersonalOrderData(orderId, updateTime, personalOrder);
                                }
                            }
                        }
                    }
                });
    }

    public void removeUserEndOrdersListener() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    private void storePersonalOrderData(final String orderId, final Date updateTime, final UserEndOrderDoc.Personal personalOrder) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                EndOrder endOrder = new EndOrder();
                endOrder.setupPersonalOrderValues(orderId, updateTime, personalOrder);
                db.endOrderDAO().insertEndOrder(endOrder);
            }
        });
    }

    private void storeGroupOrderData(final String orderId, final String groupId, final Date updateTime) {
        fs.getGroupOrderDoc(groupId, orderId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final GroupOrderDoc groupOrderDoc = documentSnapshot.toObject(GroupOrderDoc.class);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                EndOrder endOrder = new EndOrder();
                                endOrder.setupGroupOrderValues(orderId, groupId, updateTime, groupOrderDoc);
                                db.endOrderDAO().insertEndOrder(endOrder);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });

        fs.getGroupOrderBuyersCol(groupId, orderId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            final ArrayList<Buyer> buyers = new ArrayList<>();
                            for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                                BuyerDoc buyerDoc = snap.toObject(BuyerDoc.class);
                                String uid = snap.getId();
                                Buyer buyer = new Buyer();
                                buyer.setupBuyerDocValues(orderId, uid, buyerDoc);
                                buyers.add(buyer);
                            }
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    db.buyerDAO().insertBuyers(buyers);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }
}
