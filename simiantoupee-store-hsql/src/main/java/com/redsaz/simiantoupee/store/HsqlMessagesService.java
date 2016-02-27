/*
 * Copyright 2016 Redsaz <redsaz@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redsaz.simiantoupee.store;

import com.redsaz.simiantoupee.api.exceptions.AppException;
import com.redsaz.simiantoupee.api.exceptions.AppServerException;
import com.redsaz.simiantoupee.api.exceptions.NotFoundException;
import com.redsaz.simiantoupee.api.model.Message;
import static com.redsaz.simiantoupee.model.tables.Message.MESSAGE;
import com.redsaz.simiantoupee.model.tables.records.MessageRecord;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hsqldb.jdbc.JDBCPool;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep3;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import com.redsaz.simiantoupee.api.MessagesService;

/**
 * Stores and accesses messages.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class HsqlMessagesService implements MessagesService {

    private static final JDBCPool POOL = initPool();

    @Override
    public List<Message> getMessages() {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);
            List<MessageRecord> nrs = context.selectFrom(MESSAGE).fetch();
            return recordsToMessages(nrs);
        } catch (SQLException ex) {
            throw new AppServerException("Cannot retrieve messages: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Message getMessage(long id) {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

            MessageRecord nr = context.selectFrom(MESSAGE).where(MESSAGE.ID.eq(id)).fetchOne();
            return recordToMessage(nr);
        } catch (SQLException ex) {
            throw new AppServerException("Cannot get message_id=" + id + " because: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Message> createAll(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

            InsertValuesStep3<MessageRecord, String, String, String> query = context.insertInto(MESSAGE).columns(MESSAGE.URINAME, MESSAGE.TITLE, MESSAGE.BODY);
            for (Message message : messages) {
                query.values(message.getUriName(), message.getTitle(), message.getBody());
            }
            Result<MessageRecord> nrs = query.returning().fetch();
            return recordsToMessages(nrs);
        } catch (SQLException ex) {
            throw new AppServerException("Failed to create messages: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Message> updateAll(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        RuntimeException updateFailure = null;
        List<Long> ids = new ArrayList<>(messages.size());
        for (Message message : messages) {
            try (Connection c = POOL.getConnection()) {
                DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

                int numMessagesAffected = context.update(MESSAGE)
                        .set(MESSAGE.URINAME, message.getUriName())
                        .set(MESSAGE.TITLE, message.getTitle())
                        .set(MESSAGE.BODY, message.getBody())
                        .where(MESSAGE.ID.eq(message.getId())).execute();
                if (numMessagesAffected != 1) {
                    throw new NotFoundException("Failed to update message_id="
                            + message.getId() + " because it does not exist.");
                }
                ids.add(message.getId());
            } catch (SQLException ex) {
                if (updateFailure == null) {
                    updateFailure = new AppException("Failed to update one or more messages.");
                }
                updateFailure.addSuppressed(ex);
            } catch (NotFoundException ex) {
                if (updateFailure == null) {
                    updateFailure = ex;
                } else {
                    updateFailure.addSuppressed(ex);
                }
            }
        }
        if (updateFailure != null) {
            throw updateFailure;
        }

        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

            Result<MessageRecord> records = context.selectFrom(MESSAGE).where(MESSAGE.ID.in(ids)).fetch();
            return recordsToMessages(records);
        } catch (SQLException ex) {
            throw new AppServerException("Sucessfully updated message_ids=" + ids
                    + " but failed to return the updated records: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteMessage(long id) {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

            context.delete(MESSAGE).where(MESSAGE.ID.eq(id)).execute();
        } catch (SQLException ex) {
            throw new AppServerException("Failed to delete message_id=" + id
                    + " because: " + ex.getMessage(), ex);
        }
    }

    private static JDBCPool initPool() {
        System.out.println("Initing DB...");
        File dbDir = new File("./simiantoupee");
        if (!dbDir.exists() && !dbDir.mkdirs()) {
            throw new RuntimeException("Could not create " + dbDir);
        }
        File dbFire = new File(dbDir, "simiantoupeedb");
        JDBCPool jdbc = new JDBCPool();
        jdbc.setUrl("jdbc:hsqldb:" + dbFire.toURI());
        jdbc.setUser("SA");
        jdbc.setPassword("SA");

        try (Connection c = jdbc.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
            Liquibase liquibase = new Liquibase("simiantoupee-db.yaml", new ClassLoaderResourceAccessor(), database);
            liquibase.update((String) null);
        } catch (SQLException | LiquibaseException ex) {
            throw new AppServerException("Cannot initialize messages service: " + ex.getMessage(), ex);
        }
        return jdbc;
    }

    private static Message recordToMessage(MessageRecord nr) {
        if (nr == null) {
            return null;
        }
        return new Message(nr.getValue(MESSAGE.ID), nr.getValue(MESSAGE.URINAME), nr.getValue(MESSAGE.TITLE), nr.getValue(MESSAGE.BODY));
    }

    private static List<Message> recordsToMessages(List<MessageRecord> nrs) {
        if (nrs == null) {
            return null;
        }
        List<Message> messages = new ArrayList<>(nrs.size());
        for (MessageRecord nr : nrs) {
            Message result = recordToMessage(nr);
            if (result != null) {
                messages.add(result);
            }
        }
        return messages;
    }

}
