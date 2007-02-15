/*
 * Copyright 2007 Andrew Wills
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.danann.cernunnos.mail;

import java.util.Properties;
import javax.mail.Address;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class SendEmailTask implements Task {

    // Instance Members.
    private Phrase protocol;
    private Phrase hostname;
    private Phrase port;
    private Phrase username;
    private Phrase password;
    private Phrase from;
    private Phrase to;
    private Phrase subject;
    private Phrase text;
    private Phrase debug;

    /*
     * Public API.
     */

    public static final Reagent PROTOCOL = new SimpleReagent("PROTOCOL", "@protocol", ReagentType.PHRASE, 
    							String.class, "Transport protocol for sending the email message.");

    public static final Reagent HOSTNAME = new SimpleReagent("HOSTNAME", "@hostname", ReagentType.PHRASE,
    							String.class, "The hostname of the outgoing mail server.");

    public static final Reagent PORT = new SimpleReagent("PORT", "@port", ReagentType.PHRASE,
    							String.class, "the port number of the outgoing mail server.");

    public static final Reagent USERNAME = new SimpleReagent("USERNAME", "@username", ReagentType.PHRASE,
    							String.class, "The username to use when authenticating with the "
    							+ "outgoing mail server (if authentication is required).", null);

    public static final Reagent PASSWORD = new SimpleReagent("PASSWORD", "@password", ReagentType.PHRASE,
    							String.class, "The password to use when authenticating with the "
    							+ "outgoing mail server (if authentication is required).", null);

    public static final Reagent FROM = new SimpleReagent("LOCATION", "@from", ReagentType.PHRASE,
    							String.class, "The email address of the sender.");

    public static final Reagent TO = new SimpleReagent("TO_FILE", "@to", ReagentType.PHRASE, 
    							String.class, "Email address of the intended recipient.");

    public static final Reagent SUBJECT = new SimpleReagent("SUBJECT", "@subject", ReagentType.PHRASE,
    							String.class, "The email subject line.", new LiteralPhrase(""));

    public static final Reagent TEXT = new SimpleReagent("TEXT", "text()", ReagentType.PHRASE,
    							String.class, "The email message body.");

    public static final Reagent DEBUG = new SimpleReagent("DEBUG", "@debug", ReagentType.PHRASE, Boolean.class,
                        		"Flag to turn on/off JavaMail debug information.",Boolean.FALSE);

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {PROTOCOL, HOSTNAME, PORT, USERNAME,
                                PASSWORD, FROM, TO, SUBJECT, TEXT, DEBUG};
        final Formula rslt = new SimpleFormula(SendEmailTask.class, reagents);
        return rslt;
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.protocol = (Phrase) config.getValue(PROTOCOL);
        this.hostname = (Phrase) config.getValue(HOSTNAME);
        this.port = (Phrase) config.getValue(PORT);
        this.username = (Phrase) config.getValue(USERNAME);
        this.password = (Phrase) config.getValue(PASSWORD);
        this.from = (Phrase) config.getValue(FROM);
        this.to = (Phrase) config.getValue(TO);
        this.subject = (Phrase) config.getValue(SUBJECT);
        this.text = (Phrase) config.getValue(TEXT);
        this.debug = (Phrase) config.getValue(DEBUG);

    }

    public void perform(TaskRequest req, TaskResponse res) {

        String fProtocol = (String) protocol.evaluate(req, res);
        String fHostname = (String) hostname.evaluate(req, res);
        String fPort = (String) port.evaluate(req, res);
        String fUsername = null;
        String fPassword = null;

        Properties p = new Properties();
        p.setProperty("mail.transport.protocol", fProtocol);
        p.setProperty("mail." + fProtocol + ".host", fHostname);
        p.setProperty("mail." + fProtocol + ".port", fPort);

        if (username != null && password != null) {
            fUsername = (String) username.evaluate(req, res);
            fPassword = (String) password.evaluate(req, res);
            p.setProperty("mail." + fProtocol + ".auth", "true");
        }

        String fTo = (String) to.evaluate(req, res);
        String fFrom = (String) from.evaluate(req, res);
        String fSubject = (String) subject.evaluate(req, res);
        String fText = (String) text.evaluate(req, res);

        try {

            Session s = Session.getDefaultInstance(p);
            s.setDebug(Boolean.parseBoolean((String) debug.evaluate(req, res)));

            MimeMessage msg = new MimeMessage(s);
            msg.setFrom(new InternetAddress(fFrom));
            msg.setSubject(fSubject);
            msg.setText(fText);

            Transport n = s.getTransport(fProtocol);
            n.connect(fHostname, Integer.parseInt(fPort), fUsername, fPassword);
            n.sendMessage(msg, new Address[] {new InternetAddress(fTo)});

        } catch (Throwable t) {
            String msg = "Unable to send the specified email."
                + "\n\tprotocol=" + fProtocol
                + "\n\thostname=" + fHostname
                + "\n\tport=" + fPort
                + "\n\tto=" + fTo
                + "\n\tfrom=" + fFrom
                + "\n\tsubject=" + fSubject
                + "\n\ttext=" + (fText.length() > 13  ? fText.substring(0, 10) + "..." : fText);
            throw new RuntimeException(msg, t);
        }

    }

}