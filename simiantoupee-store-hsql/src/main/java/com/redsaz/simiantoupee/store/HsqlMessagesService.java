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
import com.redsaz.simiantoupee.api.model.MessageAddress;
import static com.redsaz.simiantoupee.model.tables.Address.ADDRESS;
import com.redsaz.simiantoupee.model.tables.records.AddressRecord;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.jooq.InsertValuesStep2;
import org.jooq.InsertValuesStep5;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.Result;
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
    public List<BasicMessage> getPreviewMessages() {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);
            Result<Record4<String, String, String, Long>> nrs = context.select(MESSAGE.ID, MESSAGE.SUBJECT, MESSAGE.ABSTRACT, MESSAGE.SENDER_ID).from(MESSAGE).fetch();
            LOG.info("Messages: {}", nrs.size());
            return previewRecordsToPreviewMessages(nrs);
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
    public String create(MessageAddress sender, InputStream messageStream) {
        if (messageStream == null) {
            return null;
        }
        LOG.debug("About to create a message.");
        byte[] rawMessage = getRawMessage(messageStream);
        BasicMessage basicMessage = getBasicMessageFromRaw(sender, rawMessage);
        LOG.debug(" Sender: {}", basicMessage.getSender());

        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

            InsertValuesStep5<MessageRecord, String, Long, String, String, byte[]> query = context.insertInto(MESSAGE).columns(MESSAGE.ID, MESSAGE.SENDER_ID, MESSAGE.SUBJECT, MESSAGE.ABSTRACT, MESSAGE.RAW);

            query.values(basicMessage.getId(), sender.getId(), truncateText(basicMessage.getSubject(), 100, true), truncateText(basicMessage.getBody(), 100, false), rawMessage);
            query.execute();
            return basicMessage.getId();
        } catch (SQLException ex) {
            throw new AppServerException("Failed to create message: " + ex.getMessage(), ex);
        }
    }

    @Override
    public MessageAddress getAddress(String address) {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);
            AddressRecord ar = context.selectFrom(ADDRESS).where(ADDRESS.EMAIL.eq(address)).fetchOne();
            if (ar == null) {
                return null;
            }
            return new MessageAddress(ar.getValue(ADDRESS.ID),
                    ar.getValue(ADDRESS.EMAIL),
                    ar.getValue(ADDRESS.NAME));
        } catch (SQLException ex) {
            throw new AppServerException("Failed to retrieve sender record for " + address + ": " + ex.getMessage(), ex);
        }
    }

    @Override
    public MessageAddress getAddress(long id) {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);
            AddressRecord ar = context.selectFrom(ADDRESS).where(ADDRESS.ID.eq(id)).fetchOne();
            return new MessageAddress(ar.getValue(ADDRESS.ID),
                    ar.getValue(ADDRESS.EMAIL),
                    ar.getValue(ADDRESS.NAME));
        } catch (SQLException ex) {
            throw new AppServerException("Failed to retrieve sender record for id: " + id + ": " + ex.getMessage(), ex);
        }
    }

    @Override
    public MessageAddress createAddress(String address, String name) {
        try (Connection c = POOL.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);
            InsertValuesStep2 query = context.insertInto(ADDRESS, ADDRESS.EMAIL, ADDRESS.NAME);
            Record idRecord = query.values(address, name).returning(ADDRESS.ID).fetchOne();
            return getAddress(idRecord.getValue(ADDRESS.ID));
        } catch (SQLException ex) {
            throw new AppServerException("Failed to create address record for " + address + ": " + ex.getMessage(), ex);
        }
    }

    private static BasicMessage getBasicMessageFromRaw(MessageAddress sender, byte[] rawMessage) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(rawMessage);
            MimeMessage mimeMessage = new MimeMessage(SESSION, bais);
            return new BasicMessage(calcId(rawMessage), sender, mimeMessage.getSubject(), getBasicBodyGist(mimeMessage), rawMessage.length);
        } catch (MessagingException ex) {
            throw new AppServerException("Failed to create message: " + ex.getMessage(), ex);
        }
    }

    private static JDBCPool initPool() {
        LOG.info("Initing DB...");
        File dbDir = new File("./simiantoupee");
        if (!dbDir.exists() && !dbDir.mkdirs()) {
            throw new RuntimeException("Could not create " + dbDir);
        }
        File dbFile = new File(dbDir, "simiantoupeedb");
        JDBCPool jdbc = new JDBCPool();
        jdbc.setUrl("jdbc:hsqldb:" + dbFile.toURI());
        LOG.info("DB URL: {}", jdbc.getUrl());
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

    private BasicMessage recordToBasicMessage(MessageRecord nr) {
        if (nr == null) {
            return null;
        }
        return getBasicMessageFromRaw(getAddress(nr.getValue(MESSAGE.SENDER_ID)), nr.getValue(MESSAGE.RAW));
    }

    private List<BasicMessage> recordsToBasicMessages(List<MessageRecord> nrs) {
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

    private List<BasicMessage> previewRecordsToPreviewMessages(Result<Record4<String, String, String, Long>> nrs) {
        if (nrs == null) {
            return null;
        }
        List<BasicMessage> messages = new ArrayList<>(nrs.size());
        for (Record4 nr : nrs) {
            BasicMessage result = new BasicMessage(nr.getValue(MESSAGE.ID), getAddress(nr.getValue(MESSAGE.SENDER_ID)), nr.getValue(MESSAGE.SUBJECT), nr.getValue(MESSAGE.ABSTRACT), 0);
            messages.add(result);
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
            for (int i = 0; i < rawMessage.length; ++i) {
                digest.update(rawMessage[i]);
            }
            byte[] hash = digest.digest();
            return BaseEncoding.base64Url().omitPadding().encode(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new AppServerException("Unable to use SHA-256 Digest. Reason: " + ex.getMessage(), ex);
        }
    }

    private static String getBasicBodyGist(MimeMessage mimeMessage) {
        if (mimeMessage == null) {
            return null;
        }
        try {
            Object content = mimeMessage.getContent();
            if (content == null) {
                return null;
            } else if (content instanceof String) {
                return (String) content;
            } else if (content instanceof MimeMultipart) {
                return multipartToBasicBodyGist((MimeMultipart) content);
            } else {
                throw new AppServerException("Don't know how to handle type " + content.getClass().getName());
            }
        } catch (MessagingException | IOException ex) {
            throw new AppServerException("Could not read message content.");
        }
    }

    private static String multipartToBasicBodyGist(MimeMultipart multipart) throws MessagingException, IOException {
        int numParts = multipart.getCount();
        for (int i = 0; i < numParts; ++i) {
            MimeBodyPart body = (MimeBodyPart) multipart.getBodyPart(i);
            Object content = body.getContent();
            if (content == null) {
                // Do nothing. Try a different body part.
            } else if (content instanceof String) {
                return (String) content;
            } else if (content instanceof MimeMultipart) {
                return multipartToBasicBodyGist((MimeMultipart) content);
            } else {
                throw new AppServerException("Don't know how to handle type " + content.getClass().getName());
            }
        }
        return null;
    }

    private static String truncateText(String text, int maxChars, boolean ellipses) {
        if (maxChars < 1 || (ellipses && maxChars < 4)) {
            throw new RuntimeException("maxChars must be at least 1 character long without ellipses, or 4 with ellipses.");
        }
        if (text == null) {
            return "";
        } else if (text.length() > maxChars) {
            String result;
            if (ellipses) {
                StringBuilder sb = new StringBuilder(text.substring(0, maxChars - 3));
                sb.append("...");
                result = sb.toString();
            } else {
                result = text.substring(0, maxChars);
            }
            return result;
        }
        return text;
    }

}
