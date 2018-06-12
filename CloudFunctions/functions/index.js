const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.syncNickname = functions.database.ref('/users/{uid}/data/nickname').onUpdate((change, context) => {
    const uid = context.params.uid;
    const nickname = change.after.val();
    const groupsPromise = admin.database().ref(`/users/${uid}/groups`).once('value');
    return Promise.all([groupsPromise]).then(results => {
        results[0].forEach(childSnap => {
            const groupId = childSnap.ref.key;
            admin.database().ref(`/groups/${groupId}/founderNickname`).set(nickname);
        });
        return;
    });
});

exports.requestJoinGroup = functions.database.ref('/groups/{groupId}/notify/requestJoinGroup/{id}')
.onCreate((snapshot, context) => {
    const noti = snapshot.val();
    const joinGroupPromise = admin.database().ref(`/groups/${noti.groupId}/users/${noti.fromUid}`).set(false);
    return Promise.all([joinGroupPromise]).then(results => {
        return handleNotification(snapshot);
    });
});

function handleNotification(snapshot) {
    const noti = snapshot.val();
    const tokensPromise = admin.database().ref(`/users/${noti.toUid}/tokens`).once('value');
    const removePromise = admin.database().ref(snapshot.ref).remove();
    return Promise.all([tokensPromise, removePromise]).then(results => {
        let tokens = [];
        const tokensSnap = results[0];
        tokensSnap.forEach(childSnap => {
            if (childSnap.val()) {
                tokens.push(childSnap.ref.key);
            }
        });
        const payload = {
            'data' : {
                'action' : noti.action.toString(),
                'groupId' : noti.groupId
            }
        }
        return sendMessageToDevices(tokensSnap.ref, tokens, payload);
    });
}

function sendMessageToDevices(tokensRef, tokens, payload) {
    return admin.messaging().sendToDevice(tokens, payload).then(res => {
        for (let i = 0; i < res.results.length; i++) {
            const result = res.results[i];
            if (result.error !== undefined &&
                result.error.code === 'messaging/invalid-registration-token') {
                tokensRef.child(tokens[i]).remove();
            }
        }
        return res;
    });
}

exports.syncGroupOrder = functions.database.ref('/groups/{groupId}/orders/{orderId}')
.onWrite((change, context) => {
    const order = change.after.val();
    const orderId = order.id;
    const takerUid = order.takerUid;
    let promises = [];
    if (takerUid !== null) {
        const syncUserOrderPromise = admin.database().ref(`/users/${takerUid}/orders/${orderId}`).set(order);
        promises.push(syncUserOrderPromise);
    }
    return Promise.all(promises);
});
