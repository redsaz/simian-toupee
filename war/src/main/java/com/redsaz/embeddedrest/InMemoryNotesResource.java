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
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.enterprise.inject.Default;

/**
 * Stores and accesses notes.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
@Default
public class InMemoryNotesResource implements NotesResource {

    private ConcurrentMap<String, Note> notes = new ConcurrentHashMap<String, Note>();
    private static final Slugify SLG = initSlug();
    private static final int SHORTENED_MAX = 60;
    private static final int SHORTENED_MIN = 12;

    public List<Note> getNotes() {
        Note note = new Note(1L, "asdf", "Howdy", "I'm a body.");
        return Collections.singletonList(note);
    }

    public Note getNote(long id) {
        return new Note(id, "asdf", "Why Yes This is a Note",
                "Why would you think otherwise.");
    }

    public String create(Note note) {
        Note sanitized = sanitize(note);
        notes.putIfAbsent(sanitized.getUriName(), sanitized);
        return note.getUriName();
    }

    /**
     * A note must have at least a uri, a title, and/or a body. If none of them
     * are present then note cannot be sanitized.
     *
     * @param note The note to sanitize
     * @return A new note instance with sanitized data.
     */
    private static Note sanitize(Note note) {
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

        return new Note(0L, uriName, title, body);
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
