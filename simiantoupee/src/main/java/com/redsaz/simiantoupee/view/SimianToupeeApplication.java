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
package com.redsaz.simiantoupee.view;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.redsaz.simiantoupee.api.MessagesService;
import com.redsaz.simiantoupee.api.exceptions.ExceptionMappers;
import com.redsaz.simiantoupee.pop3.Pop3Server;
import com.redsaz.simiantoupee.smtp.SmtpServer;
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
@ApplicationPath("/")
public class SimianToupeeApplication extends Application {

    private static final MessagesService MESSAGES_SERVICE = new SanitizedMessagesService();
    private static final SmtpServer SMTP_SERVER = new SmtpServer(MESSAGES_SERVICE, 40025, "localhost");
    private static final Pop3Server POP3_SERVER = new Pop3Server(MESSAGES_SERVICE, 40110);

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(MessagesResource.class);
        classes.add(BrowserMessagesResource.class);
        classes.add(JacksonJsonProvider.class);
        classes.add(StaticContentFilter.class);
        classes.add(Templater.class);
        classes.add(FreemarkerTemplater.class);
        classes.add(SanitizedMessagesService.class);
        classes.add(ExceptionMappers.class);
        classes.add(ExceptionMappers.AppExceptionMapper.class);
        classes.add(ExceptionMappers.NotFoundMapper.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();
        singletons.add(MESSAGES_SERVICE);
        singletons.add(SMTP_SERVER);
        singletons.add(POP3_SERVER);
        return singletons;
    }

}
