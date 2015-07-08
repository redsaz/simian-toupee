package com.redsaz.embeddedrest;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author shayne
 */
public class NotesResource {

    public List<Note> getNotes() {
        Note note = new Note("asdf");
        return Collections.singletonList(note);
    }

    public Note getNote(String uriName) {
        return new Note(uriName);
    }
}
