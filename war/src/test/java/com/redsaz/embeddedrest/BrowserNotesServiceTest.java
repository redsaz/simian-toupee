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

import java.net.URISyntaxException;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.resteasy.mock.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Redsaz <redsaz@gmail.com>
 */
public class BrowserNotesServiceTest extends Assert {

    @Test
    public void testListNotesBrowser() throws URISyntaxException {
        Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

        ResteasyProviderFactory
                .getContextDataMap()
                .put(HttpServletRequest.class, new MockHttpServletRequest());

        NotesResource mockedNotesResource = mock(NotesResource.class);
        when(mockedNotesResource.getNotes()).thenReturn(Collections.<Note>emptyList());
        dispatcher.getRegistry().addSingletonResource(new BrowserNotesService(mockedNotesResource, new Templater()));//addResourceFactory(noDefaults);

        MockHttpRequest request = MockHttpRequest.get("/notes").accept(MediaType.TEXT_HTML);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);

        verify(mockedNotesResource).getNotes();
    }
}
