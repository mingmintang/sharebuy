package com.mingmin.sharebuy.cloud;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.mingmin.sharebuy.Order;
import com.mingmin.sharebuy.User;
import com.mingmin.sharebuy.UserOrderRecyclerAdapter;

import java.util.HashMap;

public class UserOrderCloud {
    private final String TAG = getClass().getSimpleName();
    private Firestore fs;
    private HashMap<String, ListenerRegistration> registrations;

    public UserOrderCloud() {
        fs = Firestore.getInstance();
        registrations = new HashMap<>();
    }

    public interface UserOrderListener {
        void onUserOrderChanged(Order order, UserOrderRecyclerAdapter.OrderHolder holder, User user);
    }

    public void addUserOrderListener(final User user, final String myName, String groupId, final String orderId, final UserOrderRecyclerAdapter.OrderHolder holder) {
        if (registrations.containsKey(orderId)) {
            return;
        }
        ListenerRegistration registration = fs.getGroupOrderDoc(groupId, orderId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "user order listen failed.", e);
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            GroupOrderDoc groupOrderDoc = documentSnapshot.toObject(GroupOrderDoc.class);
                            Order order = new Order(orderId, groupOrderDoc);
                            order.setMyName(myName);
                            holder.onUserOrderChanged(order, holder, user);
                        } else {
                            Log.d(TAG, "User order is empty.");
                        }

                    }
                });
        registrations.put(orderId, registration);
    }

    public void removeUserOrderListener(String orderId) {
        ListenerRegistration reg = registrations.get(orderId);
        if (reg != null) {
            reg.remove();
            registrations.remove(orderId);
        }
    }

    public void removeAllListener() {
        if (!registrations.isEmpty()) {
            for (ListenerRegistration reg : registrations.values()) {
                reg.remove();
            }
            registrations.clear();
        }
    }
}
