package com.ssafy.backend.ingestion.notification;

public interface DailySummaryPublisher {
    void publish(DailySummaryNotification notification);
}
