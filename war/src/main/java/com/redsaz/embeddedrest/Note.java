package com.redsaz.embeddedrest;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 *
 * @author shayne
 */
public class Note {

    private String uriName;

    @JsonCreator
    public Note(String inUriName) {
        uriName = inUriName;
    }

    public String getUriName() {
        return uriName;
    }
}
