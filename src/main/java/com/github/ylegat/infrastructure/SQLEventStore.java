package com.github.ylegat.infrastructure;

import static java.util.Collections.emptySet;
import static com.github.ylegat.uncheck.Uncheck.uncheck;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.github.ylegat.EventSerializer;
import com.github.ylegat.domain.EventStore;
import com.github.ylegat.domain.Event;

public class SQLEventStore extends EventStore {

    private final EventSerializer eventSerializer;

    private final ThreadLocal<Connection> connectionSupplier = ThreadLocal.withInitial(() -> {
        return uncheck(() -> {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection("jdbc:h2:./target/h2", "sa", "");
        });
    });

    public SQLEventStore(EventSerializer eventSerializer) {
        this(eventSerializer, emptySet());
    }

    public SQLEventStore(EventSerializer eventSerializer, Set<String> conflictingEvents) {
        super(conflictingEvents);
        this.eventSerializer = eventSerializer;
        uncheck(() -> init(getConnection()));
    }

    private Connection getConnection() {
        return connectionSupplier.get();
    }

    private Connection init(Connection connection) throws SQLException {
        connection.createStatement()
                  .executeUpdate("DROP TABLE IF EXISTS EVENTS");
        connection.createStatement()
                  .executeUpdate("CREATE TABLE EVENTS (" +
                                         "ID BIGINT PRIMARY KEY AUTO_INCREMENT," +
                                         "AGGREGATE_ID VARCHAR(64) NOT NULL," +
                                         "VERSION BIGINT NOT NULL," +
                                         "EVENT_TYPE VARCHAR(64) NOT NULL," +
                                         "EVENT VARCHAR(256) NOT NULL)");
        connection.createStatement()
                  .executeUpdate("ALTER TABLE events ADD CONSTRAINT u_event UNIQUE(AGGREGATE_ID, VERSION)");
        connection.setAutoCommit(false);
        return connection;
    }

    @Override
    protected boolean save(Event event) {
        try {
            PreparedStatement statement = getConnection().prepareStatement(
                    "INSERT INTO EVENTS(AGGREGATE_ID, VERSION, EVENT, EVENT_TYPE) VALUES(?, ?, ?, ?)");
            statement.setString(1, event.aggregateId);
            statement.setLong(2, event.version);
            statement.setString(3, eventSerializer.serialize(event));
            statement.setString(4, event.eventType);
            statement.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<Event> get(String aggregateId, long fromVersion) {
        return uncheck(() -> {
            PreparedStatement statement = getConnection().prepareStatement("SELECT EVENT_TYPE, EVENT FROM EVENTS WHERE AGGREGATE_ID = ? AND VERSION >= ?");
            statement.setString(1, aggregateId);
            statement.setLong(2, fromVersion);
            ResultSet resultSet = statement.executeQuery();

            List<Event> events = new LinkedList<>();
            while (resultSet.next()) {
                String eventType = resultSet.getString(1);
                String data = resultSet.getString(2);
                Event event = eventSerializer.deserialize(eventType, data);
                events.add(event);
            }

            return events;
        });
    }

    @Override
    protected boolean commit() {
        try {
            getConnection().commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void rollback() {
        uncheck(() -> getConnection().rollback());
    }
}
