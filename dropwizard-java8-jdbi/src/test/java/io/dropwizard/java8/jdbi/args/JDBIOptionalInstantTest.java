package io.dropwizard.java8.jdbi.args;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.java8.jdbi.DBIFactory;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JDBIOptionalInstantTest {
    private final Environment env = new Environment("test-optional-date-time", Jackson.newObjectMapper(),
            Validators.newValidator(), new MetricRegistry(), null);

    private TaskDao dao;

    @Before
    public void setupTests() throws IOException {
        final DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass("org.h2.Driver");
        dataSourceFactory.setUrl("jdbc:h2:mem:date-time-optional-" + System.currentTimeMillis() + "?user=sa");
        dataSourceFactory.setInitialSize(1);
        final DBI dbi = new DBIFactory().build(env, dataSourceFactory, "test");
        try (Handle h = dbi.open()) {
            h.execute("CREATE TABLE tasks (" +
                    "id INT PRIMARY KEY, " +
                    "assignee VARCHAR(255) NOT NULL, " +
                    "start_date TIMESTAMP, " +
                    "end_date TIMESTAMP, " +
                    "comments VARCHAR(1024) " +
                    ")");
        }
        dao = dbi.onDemand(TaskDao.class);
    }

    @Test
    public void testPresent() {
        final Optional<Instant> endDate = Optional.of(Instant.parse("2015-11-03T10:15:30.00Z"));
        dao.insert(1, Optional.of("John Hughes"), Instant.now(), endDate, Optional.<String>empty());

        assertThat(dao.findEndDateById(1)).isEqualTo(endDate);
    }

    @Test
    public void testAbsent() {
        dao.insert(2, Optional.of("Kate Johansen"), Instant.now(),
                Optional.<Instant>empty(), Optional.of("To be done"));

        assertThat(dao.findEndDateById(2).isPresent()).isFalse();
    }

    interface TaskDao {

        @SqlUpdate("INSERT INTO tasks(id, assignee, start_date, end_date, comments) " +
                "VALUES (:id, :assignee, :start_date, :end_date, :comments)")
        void insert(@Bind("id") int id, @Bind("assignee") Optional<String> assignee,
                    @Bind("start_date") Instant startDate, @Bind("end_date") Optional<Instant> endDate,
                    @Bind("comments") Optional<String> comments);

        @SqlQuery("SELECT end_date FROM tasks WHERE id = :id")
        @SingleValueResult
        Optional<Instant> findEndDateById(@Bind("id") int id);
    }
}
