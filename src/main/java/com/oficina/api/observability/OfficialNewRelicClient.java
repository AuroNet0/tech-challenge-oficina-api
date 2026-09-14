package com.oficina.api.observability;

import com.newrelic.api.agent.NewRelic;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OfficialNewRelicClient implements NewRelicClient {

    @Override
    public void recordCustomEvent(String eventType, Map<String, ?> attributes) {
        NewRelic.getAgent().getInsights().recordCustomEvent(eventType, attributes);
    }

    @Override
    public void recordMetric(String name, float value) {
        NewRelic.recordMetric(name, value);
    }

    @Override
    public void recordResponseTimeMetric(String name, long millis) {
        NewRelic.recordResponseTimeMetric(name, millis);
    }
}
