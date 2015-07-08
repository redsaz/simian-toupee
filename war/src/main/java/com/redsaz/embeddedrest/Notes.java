package com.redsaz.embeddedrest;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author shayne
 */
public class Notes {

    private List<Note> notes;

    @JsonCreator
    public Notes(List<Note> inNotes) {
        notes = Collections.unmodifiableList(new ArrayList<>(inNotes));
    }

    public List<Note> getNotes() {
        return notes;
    }
}
