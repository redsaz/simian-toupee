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
package com.redsaz.simiantoupee.smtp;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.RejectException;

/**
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class AgileAuthenticationHandlerFactory implements AuthenticationHandlerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AgileAuthenticationHandlerFactory.class);

    // PLAIN and LOGIN use base64 encoding.
    // List retrieved from https://en.wikipedia.org/wiki/SMTP_Authentication
    private static final List<String> AUTH_MECHS = Arrays.asList("LOGIN");
    //private static final List<String> AUTH_MECHS = Arrays.asList("PLAIN", "LOGIN", "GSSAPI", "DIGEST-MD5", "MD5", "CRAM-MD5");

    @Override
    public List<String> getAuthenticationMechanisms() {
        return AUTH_MECHS;
    }

    @Override
    public AuthenticationHandler create() {
        return new AuthenticationHandler() {
            private String mech;
            private String user;
            private String pass;

            @Override
            public String auth(String clientInput) throws RejectException {
                if (clientInput == null || clientInput.length() > 684) {
                    throw new RejectException();
                }
                LOG.info("Beginning auth...");
                if (mech == null) {
                    mech = clientInput;
                    return "334 VXNlcm5hbWU6"; // Base64 for "Username:"
                } else if (user == null) {
                    user = base64DecodedString(clientInput);
                    return "334 UGFzc3dvcmQ6"; // Base64 for "Password:"
                }
                pass = base64DecodedString(clientInput);
                LOG.info("Mech: {}\nUser: {}\nPass: {}", mech, user, pass);
                return null;
            }

            @Override
            public Object getIdentity() {
                LOG.info("getIdentity called.");
                return null;
            }
        };
    }

    private static String base64DecodedString(String base64Encoded) {
        try {
            byte[] b = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Encoded);
            return new String(b);
        } catch (RuntimeException ex) {
            throw new RejectException();
        }
    }

}
