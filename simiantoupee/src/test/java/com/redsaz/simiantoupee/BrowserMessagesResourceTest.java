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
package com.redsaz.simiantoupee;

import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.testng.annotations.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * @author Redsaz <redsaz@gmail.com>
 */
public class BrowserMessagesResourceTest extends BaseResourceTest {

    @Test(dataProvider = DEFAULT_DP)
    public void testListBasicMessages(Context context) throws URISyntaxException {
        // Given that the service is running...
        // ... When the user views the messages page...
        MockHttpRequest request = MockHttpRequest.get("/messages").accept(MediaType.TEXT_HTML);
        HttpResponse response = context.invoke(request);

        // ... Then the messages list page should be returned.
        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);

        verify(context.messagesService).getBasicMessages();
        verify(context.templater).buildFromTemplate(any(), any(String.class));
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testGetBasicMessage(Context context) throws URISyntaxException {
        // Given that a message with id=1 exists...
        // ... when the user requests the message page...
        MockHttpRequest request = MockHttpRequest.get("/messages/" + EXISTING_MESSAGE_ID).accept(MediaType.TEXT_HTML);
        HttpResponse response = context.invoke(request);

        // ... Then the page should be retrieved, and the messages contents accessed.
        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);

        verify(context.messagesService).getBasicMessage(EXISTING_MESSAGE_ID);
        verify(context.templater).buildFromTemplate(any(), any(String.class));
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testGetBasicMessageNotFound(Context context) throws URISyntaxException {
        // Given there is not a message with id=0...
        // ...when the message id=0 page is requested...
        MockHttpRequest request = MockHttpRequest.get("/messages/" + NON_EXISTING_MESSAGE_ID).accept(MediaType.TEXT_HTML);
        HttpResponse response = context.invoke(request);

        // ...then a 404 should be returned.
        assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_FOUND);

        verify(context.messagesService).getBasicMessage(NON_EXISTING_MESSAGE_ID);
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testDeleteMessage(Context context) throws URISyntaxException {
        // Given that a message with id=1 exists...
        // ... when the user requests the message deleted...
        MockHttpRequest request = MockHttpRequest
                .post("/messages/delete")
                .addFormHeader("id", EXISTING_MESSAGE_ID)
                .accept(MediaType.TEXT_HTML);
        HttpResponse response = context.invoke(request);

        // ... Then the message should be deleted, and the client instructed to
        // redirect to the messages list page.
        assertEquals(response.getStatus(), HttpServletResponse.SC_SEE_OTHER);
        URI actualLocation = (URI) response.getOutputHeaders().getFirst("Location");
        URI expectedLocation = URI.create("/messages");
        assertEquals(actualLocation, expectedLocation);
        verify(context.messagesService).deleteMessage(EXISTING_MESSAGE_ID);
    }

}
