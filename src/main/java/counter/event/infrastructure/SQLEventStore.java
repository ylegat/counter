package counter.event.infrastructure;

import static com.github.ylegat.uncheck.Uncheck.uncheck;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import counter.event.domain.Event;
import counter.event.domain.EventStore;

public class SQLEventStore extends EventStore {

    private final Connection connection;
    private ObjectMapper objectMapper;

    public static void main(String[] args) {
        new SQLEventStore();
    }

    public SQLEventStore() {
        objectMapper = new ObjectMapper();
        connection = uncheck(() -> {
            Class.forName("org.h2.Driver");
            Connection connection = DriverManager.getConnection("jdbc:h2:./target/h2", "sa", "");
            return init(connection);
        });
    }

    private Connection init(Connection connection) throws SQLException {
        connection.createStatement()
                  .executeUpdate("DROP TABLE IF EXISTS EVENTS");
        connection.createStatement()
                  .executeUpdate("CREATE TABLE EVENTS (" +
                                         "ID BIGINT PRIMARY KEY AUTO_INCREMENT," +
                                         "AGGREGATE_ID VARCHAR(64) NOT NULL," +
                                         "VERSION BIGINT NOT NULL," +
                                         "EVENT VARCHAR(256) NOT NULL)");
        connection.createStatement()
                  .executeUpdate("ALTER TABLE events ADD CONSTRAINT u_event UNIQUE(AGGREGATE_ID, VERSION)");
        connection.setAutoCommit(false);
        return connection;
    }

    @Override
    protected boolean save(Event event) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO EVENTS(AGGREGATE_ID, VERSION, EVENT) VALUES(?, ?, ?)");
            statement.setString(1, event.getAggregateId());
            statement.setLong(2, event.getVersion());
            statement.setString(3, uncheck(() -> objectMapper.writeValueAsString(event)));
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Event> get(String aggregateId, long fromVersion) {
        return uncheck(() -> {
            PreparedStatement statement = connection.prepareStatement("SELECT EVENT FROM EVENTS WHERE AGGREGATE_ID = ? AND VERSION >= ?");
            statement.setString(1, aggregateId);
            statement.setLong(2, fromVersion);
            ResultSet resultSet = statement.executeQuery();

            List<Event> events = new LinkedList<>();
            while (resultSet.next()) {
                String data = resultSet.getString(1);
                Event event = objectMapper.readValue(data, Event.class);
                events.add(event);
            }

            return events;
        });
    }

    @Override
    protected void start() {
        System.out.println("start");
    }

    @Override
    protected boolean commit() {
        System.out.println("commit");
        try {
            connection.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void rollback() {
        System.out.println("cancel");
        uncheck(() -> connection.rollback());
    }
}
