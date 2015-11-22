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

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * The base entrypoint for the application, lists the classes with endpoints
 * which comprise the application.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
@ApplicationPath("/rest")
public class EmbeddedRestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(NotesService.class);
        classes.add(JacksonJsonProvider.class);
        classes.add(StaticContentFilter.class);
        classes.add(Templater.class);
        return classes;
    }

}
