package com.oficina.api.observability;

import java.util.Map;

public interface NewRelicClient {

    void recordCustomEvent(String eventType, Map<String, ?> attributes);

    void recordMetric(String name, float value);

    void recordResponseTimeMetric(String name, long millis);
}
