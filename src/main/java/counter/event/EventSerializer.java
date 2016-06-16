package counter.event;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.github.ylegat.uncheck.Uncheck.uncheck;

public class EventSerializer {

    private ObjectMapper objectMapper;

    public EventSerializer() {
        objectMapper = new ObjectMapper();
    }

    public String serialize(Object object) {
        return uncheck(() -> objectMapper.writeValueAsString(object));
    }

    public <T> T deserialize(String object, Class<T> clazz) {
        return uncheck(() -> objectMapper.readValue(object, clazz));
    }
}
