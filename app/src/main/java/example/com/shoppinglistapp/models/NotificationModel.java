package example.com.shoppinglistapp.models;

public class NotificationModel {
    private String notificationMessage, senderUserEmail;

    public NotificationModel() {}

    public NotificationModel(String notificationMessage, String senderUserEmail) {
        this.notificationMessage = notificationMessage;
        this.senderUserEmail = senderUserEmail;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public String getSenderUserEmail() {
        return senderUserEmail;
    }
}
