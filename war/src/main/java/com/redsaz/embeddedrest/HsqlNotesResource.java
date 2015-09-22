/*
 * Copyright 2015 Redsaz <redsaz@gmail.com>.
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
package com.redsaz.embeddedrest;

import com.github.slugify.Slugify;
import static com.redsaz.embeddedrest.model.tables.Note.NOTE;
import com.redsaz.embeddedrest.model.tables.records.NoteRecord;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hsqldb.jdbc.JDBCPool;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep3;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

/**
 * Stores and accesses notes.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
@Default
@ApplicationScoped
public class HsqlNotesResource implements NotesResource {

    private static final JDBCPool pool = initPool();
    private static final Slugify SLG = initSlug();
    private static final int SHORTENED_MAX = 60;
    private static final int SHORTENED_MIN = 12;

    @Override
    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<>();
        try (Connection c = pool.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);
            List<NoteRecord> nrs = context.selectFrom(NOTE).fetch();
            for (NoteRecord nr : nrs) {
                Note result = new Note(nr.getValue(NOTE.ID), nr.getValue(NOTE.URINAME), nr.getValue(NOTE.TITLE), nr.getValue(NOTE.BODY));
                notes.add(result);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot retrieve notes: " + ex.getMessage(), ex);
        }

        return notes;
    }

    @Override
    public Note getNote(long id) {
        try (Connection c = pool.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

            NoteRecord nr = context.selectFrom(NOTE).where(NOTE.ID.eq(id)).fetchOne();
            Note result = new Note(nr.getValue(NOTE.ID), nr.getValue(NOTE.URINAME), nr.getValue(NOTE.TITLE), nr.getValue(NOTE.BODY));
            return result;
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot get note_id=" + id + " because: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Note> createAll(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            return Collections.emptyList();
        }
        try (Connection c = pool.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

            InsertValuesStep3<NoteRecord, String, String, String> query = context.insertInto(NOTE).columns(NOTE.URINAME, NOTE.TITLE, NOTE.BODY);
            for (Note note : notes) {
                Note sanitized = sanitizeAndPutId(note);
                query.values(sanitized.getUriName(), sanitized.getTitle(), sanitized.getBody());
            }
            Result<NoteRecord> records = query.returning().fetch();
            List<Note> results = new ArrayList<>(records.size());
            for (NoteRecord nr : records) {
                Note result = new Note(nr.getValue(NOTE.ID), nr.getValue(NOTE.URINAME), nr.getValue(NOTE.TITLE), nr.getValue(NOTE.BODY));
                results.add(result);
            }
            return results;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create notes: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Note> updateAll(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            return Collections.emptyList();
        }
        RuntimeException updateFailure = null;
        List<Long> ids = new ArrayList<>(notes.size());
        for (Note note : notes) {
            try (Connection c = pool.getConnection()) {
                DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

                Note sanitized = sanitizeAndPutId(note);
                context.update(NOTE).set(NOTE.URINAME, sanitized.getUriName())
                        .set(NOTE.TITLE, sanitized.getTitle())
                        .set(NOTE.BODY, sanitized.getBody())
                        .where(NOTE.ID.eq(sanitized.getId())).execute();
                ids.add(sanitized.getId());
            } catch (SQLException ex) {
                if (updateFailure == null) {
                    updateFailure = new RuntimeException("Failed to update one or more notes.");
                }
                updateFailure.addSuppressed(ex);
            }
        }
        if (updateFailure != null) {
            throw updateFailure;
        }

        try (Connection c = pool.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

            Result<NoteRecord> records = context.selectFrom(NOTE).where(NOTE.ID.in(ids)).fetch();
            List<Note> results = new ArrayList<>(records.size());
            for (NoteRecord nr : records) {
                Note result = new Note(nr.getValue(NOTE.ID), nr.getValue(NOTE.URINAME), nr.getValue(NOTE.TITLE), nr.getValue(NOTE.BODY));
                results.add(result);
            }
            return results;
        } catch (SQLException ex) {
            throw new RuntimeException("Sucessfully updated note_ids=" + ids
                    + " but failed to return the updated records: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteNote(long id) {
        try (Connection c = pool.getConnection()) {
            DSLContext context = DSL.using(c, SQLDialect.HSQLDB);

            context.delete(NOTE).where(NOTE.ID.eq(id)).execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete note_id=" + id
                    + " because: " + ex.getMessage(), ex);
        }
    }

    /**
     * A note must have at least a uri, a title, and/or a body. If none of them
     * are present then note cannot be sanitized. The ID will remain unchanged.
     *
     * @param note The note to sanitize
     * @return A new note instance with sanitized data.
     */
    private static Note sanitizeAndPutId(Note note) {
        String uriName = note.getUriName();
        if (uriName == null || uriName.isEmpty()) {
            uriName = note.getTitle();
            if (uriName == null || uriName.isEmpty()) {
                uriName = shortened(note.getBody());
                if (uriName == null || uriName.isEmpty()) {
                    throw new IllegalArgumentException("Note must have at least a uri, title, or body.");
                }
            }
        }
        uriName = SLG.slugify(uriName);

        String title = note.getTitle();
        if (title == null) {
            title = shortened(note.getBody());
            if (title == null) {
                title = "";
            }
        }
        String body = note.getBody();
        if (body == null) {
            body = "";
        }

        return new Note(note.getId(), uriName, title, body);
    }

    private static String shortened(String text) {
        if (text == null || text.length() <= SHORTENED_MAX) {
            return text;
        }
        text = text.substring(0, SHORTENED_MAX);
        String candidate = text.replaceFirst("\\S+$", "");
        if (candidate.length() < SHORTENED_MIN) {
            candidate = text;
        }

        return candidate + "...";
    }

    private static JDBCPool initPool() {
        JDBCPool jdbc = new JDBCPool();
        jdbc.setUrl("jdbc:hsqldb:file:notesdb");
        jdbc.setUser("SA");
        jdbc.setPassword("SA");

        try (Connection c = jdbc.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
            Liquibase liquibase = new Liquibase("embeddedrest-db.yaml", new ClassLoaderResourceAccessor(), database);
            liquibase.update((String) null);
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot initialize notes resource: " + ex.getMessage(), ex);
        } catch (DatabaseException ex) {
            throw new RuntimeException("Cannot initialize notes resource: " + ex.getMessage(), ex);
        } catch (LiquibaseException ex) {
            throw new RuntimeException("Cannot initialize notes resource: " + ex.getMessage(), ex);
        }
        return jdbc;
    }

    private static Slugify initSlug() {
        Slugify sluggy;
        try {
            sluggy = new Slugify();
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't initialize Slugify.");
        }
        return sluggy;
    }
}
