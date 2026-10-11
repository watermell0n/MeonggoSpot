package com.ssafy.backend.push.notification;

public interface ChatNotificationPublisher {
    void publish(String registrationToken, ChatNotification notification);
}
