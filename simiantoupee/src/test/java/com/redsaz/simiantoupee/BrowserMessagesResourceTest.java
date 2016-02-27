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

import com.redsaz.simiantoupee.api.exceptions.AppClientException;
import com.redsaz.simiantoupee.api.model.Message;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import static org.mockito.Mockito.when;
import org.testng.annotations.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * @author Redsaz <redsaz@gmail.com>
 */
public class BrowserMessagesResourceTest extends BaseResourceTest {

    @Test(dataProvider = DEFAULT_DP)
    public void testListMessages(Context context) throws URISyntaxException {
        // Given that the service is running...
        // ... When the user views the messages page...
        MockHttpRequest request = MockHttpRequest.get("/messages").accept(MediaType.TEXT_HTML);
        HttpResponse response = context.invoke(request);

        // ... Then the messages list page should be returned.
        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);

        verify(context.messagesService).getMessages();
        verify(context.templater).buildFromTemplate(any(), any(String.class));
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testEditMessage(Context context) throws URISyntaxException {
        // Given that a message with id=1 exists...
        // ... when the user requests the message edit page...
        MockHttpRequest request = MockHttpRequest.get("/messages/1/edit").accept(MediaType.TEXT_HTML);
        HttpResponse response = context.invoke(request);

        // ... Then the page should be retrieved, and the messages contents accessed.
        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);

        verify(context.messagesService).getMessage(1L);
        verify(context.templater).buildFromTemplate(any(), any(String.class));
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testEditMessageNotFound(Context context) throws URISyntaxException {
        // Given there is not a message with id=0...
        // ...when the message id=0 edit page is requested...
        MockHttpRequest request = MockHttpRequest.get("/messages/0/edit").accept(MediaType.TEXT_HTML);
        HttpResponse response = context.invoke(request);

        // ...then a 404 should be returned.
        assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_FOUND);

        verify(context.messagesService).getMessage(0L);
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testGetMessage(Context context) throws URISyntaxException {
        // Given that a message with id=1 exists...
        // ... when the user requests the message page...
        MockHttpRequest request = MockHttpRequest.get("/messages/1").accept(MediaType.TEXT_HTML);
        HttpResponse response = context.invoke(request);

        // ... Then the page should be retrieved, and the messages contents accessed.
        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);

        verify(context.messagesService).getMessage(1L);
        verify(context.templater).buildFromTemplate(any(), any(String.class));
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testGetMessageNotFound(Context context) throws URISyntaxException {
        // Given there is not a message with id=0...
        // ...when the message id=0 page is requested...
        MockHttpRequest request = MockHttpRequest.get("/messages/0").accept(MediaType.TEXT_HTML);
        HttpResponse response = context.invoke(request);

        // ...then a 404 should be returned.
        assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_FOUND);

        verify(context.messagesService).getMessage(0L);
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testCreateMessage(Context context) throws URISyntaxException {
        // Given that the service is running...
        // ... when the user requests the create message page...
        MockHttpRequest request = MockHttpRequest.get("/messages/create").accept(MediaType.TEXT_HTML);
        HttpResponse response = context.invoke(request);

        // ... Then the page should be retrieved.
        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);

        verify(context.templater).buildFromTemplate(any(), any(String.class));
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testDeleteMessage(Context context) throws URISyntaxException {
        // Given that a message with id=1 exists...
        // ... when the user requests the message deleted...
        MockHttpRequest request = MockHttpRequest
                .post("/messages/delete")
                .addFormHeader("id", "1")
                .accept(MediaType.TEXT_HTML);
        HttpResponse response = context.invoke(request);

        // ... Then the message should be deleted, and the client instructed to
        // redirect to the messages list page.
        assertEquals(response.getStatus(), HttpServletResponse.SC_SEE_OTHER);
        URI actualLocation = (URI) response.getOutputHeaders().getFirst("Location");
        URI expectedLocation = URI.create("/messages");
        assertEquals(actualLocation, expectedLocation);
        verify(context.messagesService).deleteMessage(1L);
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testFinishEditOrCreateMessage_Edit(Context context) throws URISyntaxException {
        // Given that a message with id=1 exists...
        // ... when the user requests the message edited...
        MockHttpRequest request = MockHttpRequest
                .post("/messages")
                .addFormHeader("id", "1")
                .addFormHeader("title", "Example Title")
                .addFormHeader("body", "Example Body")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.TEXT_HTML);
        when(context.messagesService.updateAll(any(List.class)))
                .thenReturn(Collections.singletonList(new Message(1, "example-title", "Example Title", "Example Body")));
        HttpResponse response = context.invoke(request);

        // ... Then the message should be edited, and the client instructed to
        // redirect to the messages list page.
        assertEquals(response.getStatus(), HttpServletResponse.SC_SEE_OTHER);
        URI actualLocation = (URI) response.getOutputHeaders().getFirst("Location");
        URI expectedLocation = URI.create("/messages");
        assertEquals(actualLocation, expectedLocation);
        verify(context.messagesService).updateAll(any(List.class));
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testFinishEditOrCreateMessage_EditNotFound(Context context) throws URISyntaxException {
        // Given that a message with id=2 does not exist...
        // ... when the user requests the message edited...
        MockHttpRequest request = MockHttpRequest
                .post("/messages")
                .addFormHeader("id", "2")
                .addFormHeader("title", "Example Title")
                .addFormHeader("body", "Example Body")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.TEXT_HTML);
        when(context.messagesService.updateAll(any(List.class)))
                .thenThrow(new NotFoundException("Failed to update one or more messages."));
        HttpResponse response = context.invoke(request);

        // ... Then not found status code should be returned.
        assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_FOUND);
        verify(context.messagesService).updateAll(any(List.class));
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testFinishEditOrCreateMessage_Create(Context context) throws URISyntaxException {
        // Given that the user is on the create message page...
        // ... when the user clicks the button to create the message...
        MockHttpRequest request = MockHttpRequest
                .post("/messages")
                .addFormHeader("id", "0")
                .addFormHeader("title", "Example Title")
                .addFormHeader("body", "Example Body")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.TEXT_HTML);
        when(context.messagesService.createAll(any(List.class)))
                .thenReturn(Collections.singletonList(new Message(2, "example-title", "Example Title", "Example Body")));
        HttpResponse response = context.invoke(request);

        // ... Then the message should be edited, and the client instructed to
        // redirect to the messages list page.
        assertEquals(response.getStatus(), HttpServletResponse.SC_SEE_OTHER);
        URI actualLocation = (URI) response.getOutputHeaders().getFirst("Location");
        URI expectedLocation = URI.create("/messages");
        assertEquals(actualLocation, expectedLocation);
        verify(context.messagesService).createAll(any(List.class));
    }

    @Test(dataProvider = DEFAULT_DP)
    public void testFinishEditOrCreateMessage_Create_NoContentError(Context context) throws URISyntaxException {
        // Given that the user is on the create message page...
        // ... when the user does not fill out any content and
        // clicks the button to create the message...
        MockHttpRequest request = MockHttpRequest
                .post("/messages")
                .addFormHeader("id", "0")
                .addFormHeader("title", "")
                .addFormHeader("body", "")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.TEXT_HTML);
        when(context.messagesService.createAll(any(List.class)))
                .thenThrow(new AppClientException("Message must have at least a uri, title, or body."));
        HttpResponse response = context.invoke(request);

        // ... Then the message should not be created and the client receives an
        // error page.
        assertEquals(response.getStatus(), HttpServletResponse.SC_BAD_REQUEST);
        verify(context.messagesService).createAll(any(List.class));
    }

}
