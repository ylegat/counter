package com.github.ylegat.infrastructure;

import com.github.ylegat.EventSerializer;
import com.github.ylegat.domain.EventStore;
import com.github.ylegat.domain.event.Event;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.github.ylegat.uncheck.Uncheck.uncheck;
import static java.util.Collections.emptySet;

public class SQLEventStore extends EventStore {

    private final EventSerializer eventSerializer;

    private final Connection connection;

    public SQLEventStore(EventSerializer eventSerializer) {
        this(eventSerializer, emptySet());
    }

    public SQLEventStore(EventSerializer eventSerializer, Set<String> conflictingEvents) {
        super(conflictingEvents);
        this.eventSerializer = eventSerializer;
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
            statement.setString(1, event.aggregateId);
            statement.setLong(2, event.version);
            statement.setString(3, eventSerializer.serialize(event));
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
                Event event = eventSerializer.deserialize(data);
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
