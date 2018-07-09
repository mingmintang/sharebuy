const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();
const groupsRef = db.collection('groups');
const usersRef = db.collection('users');
const FieldValue = admin.firestore.FieldValue;

// exports.syncNickname = functions.database.ref('/users/{uid}/userInfo/nickname').onUpdate((change, context) => {
//     const uid = context.params.uid;
//     const nickname = change.after.val();
//     const groupsPromise = admin.database().ref(`/users/${uid}/groups`).once('value');
//     return Promise.all([groupsPromise]).then(results => {
//         results[0].forEach(childSnap => {
//             const groupId = childSnap.ref.key;
//             handleNickname(uid, nickname, groupId);
//         });
//         return;
//     });
// });

// function handleNickname(uid, nickname, groupId) {
//     const founderUidPromise = admin.database().ref(`/groups/${groupId}/group/founderUid`).once('value');
//     return Promise.all([founderUidPromise]).then(results => {
//         const founderUid = results[0].val();
//         if (uid === founderUid) {
//             return admin.database().ref(`/groups/${groupId}/group/founderNickname`).set(nickname);
//         } else {
//             return admin.database().ref(`/groups/${groupId}/members/${uid}/nickname`).set(nickname);
//         }
//     });
// }

exports.requestJoinGroup = functions.firestore.document('groups/{groupId}/requestJoin/{id}')
.onCreate((snapshot, context) => {
    const noti = snapshot.data();
    const memberPromise = usersRef.doc(`${noti.fromUid}`).get();
    return Promise.all([memberPromise]).then(results => {
        const nickname = results[0].get('nickname');
        var member = {
            nickname: nickname,
            joined: false
        }
        groupsRef.doc(`${noti.groupId}`).collection('members').doc(`${noti.fromUid}`).set(member);
        return handleNotification(snapshot);
    });
});

function handleNotification(snapshot) {
    const noti = snapshot.data();
    const founderRef = usersRef.doc(`${noti.toUid}`);
    const founderPromise = founderRef.get();
    const removePromise = snapshot.ref.delete();
    return Promise.all([founderPromise, removePromise]).then(results => {
        const tokensData = results[0].get('tokens');
        const tokens = Object.keys(tokensData);
        const payload = {
            'data' : {
                'action' : noti.action.toString(),
                'groupId' : noti.groupId
            }
        }
        return sendMessageToDevices(founderRef, tokens, payload);
    });
}

function sendMessageToDevices(toUserRef, tokens, payload) {
    return admin.messaging().sendToDevice(tokens, payload).then(res => {
        for (let i = 0; i < res.results.length; i++) {
            const result = res.results[i];
            if (result.error !== undefined &&
                result.error.code === 'messaging/registration-token-not-registered') {
                const tokenField = 'tokens.' + tokens[i];
                toUserRef.update({
                    [tokenField]: FieldValue.delete()
                });
            }
        }
        return res;
    });
}

// exports.syncGroupOrder = functions.database.ref('/groups/{groupId}/orders/{orderId}')
// .onWrite((change, context) => {
//     const order = change.after.val();
//     const orderId = order.id;
//     const takerUid = order.takerUid;
//     let promises = [];
//     if (takerUid !== null) {
//         const syncUserOrderPromise = admin.database().ref(`/users/${takerUid}/orders/${orderId}`).set(order);
//         promises.push(syncUserOrderPromise);
//     }
//     return Promise.all(promises);
// });
