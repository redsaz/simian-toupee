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

import java.util.Collections;
import java.util.List;

/**
 * The "Business Logic" layer for storing, accessing, and retrieving of notes.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class NotesResource {

    public List<Note> getNotes() {
        Note note = new Note("asdf", "Howdy", "I'm a body.");
        return Collections.singletonList(note);
    }

    public Note getNote(String uriName) {
        return new Note(uriName, "Why Yes This is a Note",
                "Why would you think otherwise.");
    }
}
