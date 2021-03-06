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

package org.danann.cernunnos.core;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public final class LogTask implements Task {

	// Instance Members.
	private Phrase loggerName;
    private Phrase level;
	private Phrase prefix;
	private Phrase message;
	private Phrase suffix;
    private Phrase exception;

	/*
	 * Public API.
	 */

    public static final Reagent LOGGER_NAME = new SimpleReagent("LOGGER_NAME", "@logger-name", ReagentType.PHRASE,
                        String.class, "The name of the logger to write to.  The default is " +
                        "org.danann.cernunnos.core.LogTask", new LiteralPhrase("org.danann.cernunnos.core.LogTask"));
    
	public static final Reagent LEVEL = new SimpleReagent("LEVEL", "@level", ReagentType.PHRASE, String.class,
						"The log-level associated with MESSAGE.  From least to most serious, the available " +
						"log levels are ['trace' | 'debug' | 'info' | 'warn' | 'error' | 'fatal'].  The" +
						" default is 'info'.", new LiteralPhrase("info"));
    
    public static final Reagent EXCEPTION = new SimpleReagent("EXCEPTION", "@exception", ReagentType.PHRASE, Throwable.class,
                        "The optional exception to log with the message.", new LiteralPhrase(null));

	public static final Reagent PREFIX = new SimpleReagent("PREFIX", "@prefix", ReagentType.PHRASE, String.class,
						"Characters that preceed MESSAGE.  The default is an empty string.", new LiteralPhrase(""));

	public static final Reagent MESSAGE = new SimpleReagent("MESSAGE", "text()", ReagentType.PHRASE, String.class,
						"Message to write to the log.");

	public static final Reagent SUFFIX = new SimpleReagent("SUFFIX", "@suffix", ReagentType.PHRASE, String.class,
						"Characters that follow MESSAGE.  The default is an empty string.", new LiteralPhrase(""));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {LOGGER_NAME, LEVEL, PREFIX, MESSAGE, SUFFIX, EXCEPTION};
		final Formula rslt = new SimpleFormula(LogTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
        this.loggerName = (Phrase) config.getValue(LOGGER_NAME);
		this.level = (Phrase) config.getValue(LEVEL);
		this.prefix = (Phrase) config.getValue(PREFIX);
		this.message = (Phrase) config.getValue(MESSAGE);
		this.suffix = (Phrase) config.getValue(SUFFIX);
		this.exception = (Phrase) config.getValue(EXCEPTION);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		final String lvl = (String) level.evaluate(req, res);
        final String logger = (String) loggerName.evaluate(req, res);

        final StringBuilder msg = new StringBuilder();
		try {
			final Log log = LogFactory.getLog(logger);
			final Method logEnabledMethod = Log.class.getMethod("is" + capitalize(lvl) + "Enabled", new Class[] { });
			
			final boolean enabled = (Boolean)logEnabledMethod.invoke(log);
            if (enabled) {
    			msg.append(prefix.evaluate(req, res));
    			msg.append(message.evaluate(req, res));
    			msg.append(suffix.evaluate(req, res));
    			
    			final Throwable t = (Throwable) exception.evaluate(req, res);

    			final Method logMethod;
    			final Object[] args;
    			if (t == null) {
    			    logMethod = Log.class.getMethod(lvl, new Class[] {Object.class});
    			    args = new Object[] {msg.toString()};
    			}
    			else {
                    logMethod = Log.class.getMethod(lvl, new Class[] {Object.class, Throwable.class});
                    args = new Object[] {msg.toString(), t};
    			}
			    
			    logMethod.invoke(log, args);
            }
		} catch (Throwable t) {
			throw new RuntimeException("Error logging the specified message:  [" + lvl + "] " + msg, t);
		}

	}

	protected String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
