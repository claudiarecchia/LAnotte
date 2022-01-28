package it.univaq.lanotte.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;

    public FirebaseMessagingService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }


    public String sendPreparedOrderNotification(String token, String codeToCollect, String businessName) throws FirebaseMessagingException {

        Notification notification = Notification
                .builder()
                .setTitle("L'Anotte")
                .setBody("Il tuo ordine da " + businessName + " è pronto per essere ritirato! \n" +
                        "Utilizza il codice " + codeToCollect + " per confermare la tua identità nel locale.\n" +
                        "Puoi leggere nuovamente il codice nella sezione Archivio Ordini dell'app.")
                .build();

        Message message = Message
                .builder()
                .setToken(token)
                .setNotification(notification)
                // .putAllData(note.getData())
                .build();

        return firebaseMessaging.send(message);
    }

    public String sendCollectedOrderNotification(String token, String businessName) throws FirebaseMessagingException {

        Notification notification = Notification
                .builder()
                .setTitle("L'Anotte")
                .setBody("Il tuo ordine da " + businessName + " è stato ritirato! \n" +
                        "Grazie per il tuo acquisto.")
                .build();

        Message message = Message
                .builder()
                .setToken(token)
                .setNotification(notification)
                // .putAllData(note.getData())
                .build();

        return firebaseMessaging.send(message);
    }

}
