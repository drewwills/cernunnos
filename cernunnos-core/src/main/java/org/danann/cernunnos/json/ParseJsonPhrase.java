/*
 * Copyright 2009 Andrew Wills
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

package org.danann.cernunnos.json;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.danann.cernunnos.AbstractCacheHelperFactory;
import org.danann.cernunnos.CacheHelper;
import org.danann.cernunnos.DynamicCacheHelper;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ParseJsonPhrase implements Phrase {
    // Instance Members.
    private final ResourceHelper resource = new ResourceHelper();
    private CacheHelper<String, JSONObject> jsonCache;

	/*
	 * Public API.
	 */


    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ResourceHelper.CONTEXT_TARGET, 
                ResourceHelper.LOCATION_PHRASE, CacheHelper.CACHE, 
                CacheHelper.CACHE_MODEL};
		return new SimpleFormula(getClass(), reagents);
	}
	
    public void init(EntityConfig config) {

        // Instance Members.
        this.resource.init(config);
        this.jsonCache = new DynamicCacheHelper<String, JSONObject>(config);

    }

    public Object evaluate(TaskRequest req, TaskResponse res) {
        
        URL loc = resource.evaluate(req, res);

        final JsonFactory jsonFactory = new JsonFactory();
        return this.jsonCache.getCachedObject(req, res, loc.toExternalForm(), jsonFactory);
        
    }
    
    protected static final class JsonFactory extends AbstractCacheHelperFactory<String, JSONObject> {

        public JSONObject createObject(String key) {
            InputStream inpt = null;
            try {

                // Read by passing a URL -- don't manage the URLConnection yourself...
                inpt = new URL(key).openStream();

                StringBuffer buff = new StringBuffer();
                byte[] bytes = new byte[1024];
                for (int len = inpt.read(bytes); len > 0; len = inpt.read(bytes)) {
                    buff.append(new String(bytes, 0, len));
                }

                final JSONObject rslt = (JSONObject) JSONSerializer.toJSON(buff.toString());
                return rslt;

            }
            catch (Throwable t) {
                throw new RuntimeException("Unable to read the specified JSON:  " + key, t);
            }
            finally {
                if (inpt != null) {
                    try {
                        inpt.close();
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                }
            }
        }

        public Object getMutex(String key) {
            return key;
        }

    }

}
