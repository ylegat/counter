package com.github.ylegat;

import static com.github.ylegat.uncheck.Uncheck.uncheck;
import java.util.Map;
import java.util.Properties;
import com.github.ylegat.domain.event.Event;
import com.google.gson.Gson;

public class EventSerializer {

    private final Map<String, Class<? extends Event>> eventsMap;
    private Gson gson;

    public EventSerializer(Map<String, Class<? extends Event>> eventsMap) {
        this.eventsMap = eventsMap;
        this.gson = new Gson();
    }

    public String serialize(Event event) {
        return uncheck(() -> gson.toJson(event));
    }

    public <T extends Event> T deserialize(String json) {
        Properties properties = gson.fromJson(json, Properties.class);
        String eventType = (String) properties.get("eventType");
        Class<? extends Event> eventClass = eventsMap.get(eventType);
        return deserialize(json, eventClass);
    }

    private <T extends Event> T deserialize(String json, Class eventClass) {
        return (T) gson.fromJson(json, eventClass);
    }
}
