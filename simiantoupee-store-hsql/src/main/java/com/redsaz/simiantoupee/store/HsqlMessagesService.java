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

import com.google.common.io.BaseEncoding;
import com.redsaz.simiantoupee.api.exceptions.AppServerException;
import com.redsaz.simiantoupee.api.model.BasicMessage;
import static com.redsaz.simiantoupee.model.tables.Message.MESSAGE;
import com.redsaz.simiantoupee.model.tables.records.MessageRecord;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hsqldb.jdbc.JDBCPool;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import com.redsaz.simiantoupee.api.MessagesService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.jooq.InsertValuesStep3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores and accesses messages.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class HsqlMessagesService implements MessagesService {

    private static final Logger LOG = LoggerFactory.getLogger(HsqlMessagesService.class);
    private static final JDBCPool POOL = initPool();
    private static final Session SESSION = Session.getDefaultInstance(new Properties());

    @Override
    public List<BasicMessage> getBasicMessages() {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);
            List<MessageRecord> nrs = context.selectFrom(MESSAGE).fetch();
            LOG.info("Messages: {}", nrs.size());
            return recordsToBasicMessages(nrs);
        } catch (SQLException ex) {
            throw new AppServerException("Cannot retrieve messages: " + ex.getMessage(), ex);
        }
    }

    @Override
    public BasicMessage getBasicMessage(String id) {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);
            MessageRecord nr = context.selectFrom(MESSAGE).where(MESSAGE.ID.eq(id)).fetchOne();
            return recordToBasicMessage(nr);
        } catch (SQLException ex) {
            throw new AppServerException("Cannot get message_id=" + id + " because: " + ex.getMessage(), ex);
        }
    }

    @Override
    public MimeMessage getMessage(String id) {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);
            MessageRecord nr = context.selectFrom(MESSAGE).where(MESSAGE.ID.eq(id)).fetchOne();
            return recordToMessage(nr);
        } catch (SQLException ex) {
            throw new AppServerException("Cannot get message_id=" + id + " because: " + ex.getMessage(), ex);
        }
    }

    @Override
    public InputStream getMessageStream(String id) {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);
            MessageRecord nr = context.selectFrom(MESSAGE).where(MESSAGE.ID.eq(id)).fetchOne();
            return recordToMessageStream(nr);
        } catch (SQLException ex) {
            throw new AppServerException("Cannot get message_id=" + id + " because: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteMessage(String id) {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);
            context.delete(MESSAGE).where(MESSAGE.ID.eq(id)).execute();
        } catch (SQLException ex) {
            throw new AppServerException("Failed to delete message_id=" + id
                    + " because: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String create(InputStream messageStream) {
        if (messageStream == null) {
            return null;
        }
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

            InsertValuesStep3<MessageRecord, String, String, byte[]> query = context.insertInto(MESSAGE).columns(MESSAGE.ID, MESSAGE.SUBJECT, MESSAGE.RAW);

            LOG.info("About to create a message.");
            byte[] rawMessage = getRawMessage(messageStream);
            BasicMessage basicMessage = getBasicMessageFromRaw(rawMessage);
            LOG.info("Subject: {}", basicMessage.getSubject());
            LOG.info("   Body: {}", basicMessage.getBody());
            query.values(basicMessage.getId(), basicMessage.getSubject(), rawMessage);
            query.execute();
            return basicMessage.getId();
        } catch (SQLException ex) {
            throw new AppServerException("Failed to create message: " + ex.getMessage(), ex);
        }
    }

    private static BasicMessage getBasicMessageFromRaw(byte[] rawMessage) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(rawMessage);
            MimeMessage mimeMessage = new MimeMessage(SESSION, bais);
            return new BasicMessage(calcId(rawMessage), mimeMessage.getSubject(), "asdf", rawMessage.length);
        } catch (MessagingException ex) {
            throw new AppServerException("Failed to create message: " + ex.getMessage(), ex);
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

    private static BasicMessage recordToBasicMessage(MessageRecord nr) {
        if (nr == null) {
            return null;
        }
        return getBasicMessageFromRaw(nr.getValue(MESSAGE.RAW));
    }

    private static List<BasicMessage> recordsToBasicMessages(List<MessageRecord> nrs) {
        if (nrs == null) {
            return null;
        }
        List<BasicMessage> messages = new ArrayList<>(nrs.size());
        for (MessageRecord nr : nrs) {
            BasicMessage result = recordToBasicMessage(nr);
            if (result != null) {
                messages.add(result);
            }
        }
        return messages;
    }

    private static MimeMessage recordToMessage(MessageRecord nr) {
        if (nr == null) {
            return null;
        }
        try {
            byte[] rawMessage = nr.getValue(MESSAGE.RAW);
            ByteArrayInputStream bais = new ByteArrayInputStream(rawMessage);
            return new MimeMessage(SESSION, bais);
        } catch (MessagingException ex) {
            throw new AppServerException("Could not parse stored message.", ex);
        }
    }

    private static InputStream recordToMessageStream(MessageRecord nr) {
        if (nr == null) {
            return null;
        }
        byte[] rawMessage = nr.getValue(MESSAGE.RAW);
        return new ByteArrayInputStream(rawMessage);
    }

    private static byte[] getRawMessage(InputStream is) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(16384)) {
            byte[] buffer = new byte[8192];
            for (int length = is.read(buffer); length >= 0; length = is.read(buffer)) {
                baos.write(buffer, 0, length);
            }
            is.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new AppServerException("Unable to get raw message. Reason: " + ex.getMessage(), ex);
        }
    }

    private static String calcId(byte[] rawMessage) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawMessage);
            return BaseEncoding.base64Url().omitPadding().encode(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new AppServerException("Unable to use SHA-256 Digest. Reason: " + ex.getMessage(), ex);
        }
    }

}
