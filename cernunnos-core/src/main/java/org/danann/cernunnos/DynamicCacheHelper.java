/*
 * Copyright 2008 Eric Dalquist
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

package org.danann.cernunnos;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DynamicCacheHelper<K extends Serializable, V> implements CacheHelper<K, V> {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private final Phrase cachePhrase;
    private final Phrase cacheModelPhrase;
    
    //Instance variables for cache-one storage
    private final ThreadLocal<V> threadInstanceHolder = new ThreadLocal<V>();
    private final ThreadLocal<K> threadKeyHolder = new ThreadLocal<K>();
    private V instance;
    private K key;
    
    public DynamicCacheHelper(EntityConfig config) {
        this.cachePhrase = (Phrase) config.getValue(CacheHelper.CACHE);
        this.cacheModelPhrase = (Phrase) config.getValue(CacheHelper.CACHE_MODEL);
    }
    
    /* (non-Javadoc)
     * @see org.danann.cernunnos.cache.CacheHelper#getCachedObject(org.danann.cernunnos.TaskRequest, org.danann.cernunnos.TaskResponse, java.lang.Object, org.danann.cernunnos.cache.CacheHelper.Factory)
     */
    public V getCachedObject(TaskRequest req, TaskResponse res, K key, Factory<K, V> factory) {
        final CacheMode cacheMode = CacheMode.valueOf((String) this.cacheModelPhrase.evaluate(req, res));
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Getting cached object for '" + key + "' using cache mode " + cacheMode + " and factory " + factory);
        }
        
        //Load the cache only if cache-all is enabled
        final ConcurrentMap<Tuple<Serializable, K>, Object> cache;
        final Tuple<Serializable, K> compoundCacheKey;
        switch (cacheMode) {
            case NONE: {
                return factory.createObject(key);
            }
            
            default:
            case ONE: {
                cache = null;
                compoundCacheKey = null;
            }
            break;
            
            case ALL: {
                cache = (ConcurrentMap<Tuple<Serializable, K>, Object>) this.cachePhrase.evaluate(req, res);
                final Serializable cacheNamespace = factory.getCacheNamespace(key);
                compoundCacheKey = new Tuple<Serializable, K>(cacheNamespace, key);
            }
            break;
        }
        
        //Determine the object to synchronize around
        final Object syncTarget = factory.getMutex(key);

        //get or create & cache the target object
        V instance = null;
        synchronized (syncTarget) {
            //Get the object from the local variables if no cache is available
            if (cache == null) {
                //Try for a thread-local instance first
                if (this.compareKeys(key, this.threadKeyHolder.get())) {
                    instance = this.threadInstanceHolder.get();
                }
                //Next try for a singleton instance
                else if (this.compareKeys(key, this.key)) {
                    instance = this.instance;
                } 
            }
            //Look in the passed cache for the instance
            else {
                final Object object = cache.get(compoundCacheKey);
                
                //If the cached object is a ThreadLocal use it for the instance
                if (object instanceof ThreadLocal<?>) {
                    instance = ((ThreadLocal<V>) object).get();
                }
                //If not assume it is the instance 
                else {
                    instance = (V) object;
                }
            }
            
            //If no instance was found create and cache one
            if (instance == null) {
                instance = factory.createObject(key);
                final boolean threadSafe = factory.isThreadSafe(key, instance);
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Cache miss for '" + key + "' created '" + instance + "' threadSafe=" + threadSafe);
                }
                
                //If no cache is available store the instance in the local variables
                if (cache == null) {
                    if (threadSafe) {
                        this.instance = instance;
                        this.key = key;
                    }
                    else {
                        this.threadInstanceHolder.set(instance);
                        this.threadKeyHolder.set(key);
                    }
                }
                //Cache available store there
                else {
                    if (threadSafe) {
                        cache.put(compoundCacheKey, instance);
                    }
                    else {
                        ThreadLocal<V> threadInstanceHolder = (ThreadLocal<V>)cache.get(compoundCacheKey);
                        if (threadInstanceHolder == null) {
                            threadInstanceHolder = new ThreadLocal<V>();
                            
                            while (true) {
                                Object existing = cache.putIfAbsent(compoundCacheKey, threadInstanceHolder);
                                if (existing == null) {
                                    //nothing existed for that key, put was successful
                                    break;
                                }
                                
                                if (existing instanceof ThreadLocal) {
                                    //Existing ThreadLocal, just use it
                                    threadInstanceHolder = (ThreadLocal)existing;
                                    break;
                                }
                                
                                //something other than a ThreadLocal already exists, try replacing with the ThreadLocal
                                final boolean replaced = cache.replace(compoundCacheKey, threadInstanceHolder, existing);
                                if (replaced) {
                                    //Replace worked!
                                    break;
                                }
                                
                                //Replace didn't work, try the whole process again, yay non-blocking!
                            }
                            
                            if (cache instanceof EvictionAwareCache) {
                                ((EvictionAwareCache) cache).registerCacheEvictionListener(ThreadLocalCacheEvictionListener.INSTANCE);
                            }
                        }

                        threadInstanceHolder.set(instance);
                    }
                }
            }
            else if (this.logger.isDebugEnabled()) {
                this.logger.debug("Cache hit for '" + key + "' using '" + instance + "'");
            }
        }
        
        return instance;
    }
    
    /**
     * Basic logic to compare two keys for equality
     */
    protected final boolean compareKeys(K k1, K k2) {
        return k1 == k2 || (k1 != null && k1.equals(k2));
    }
    
    private static final class ThreadLocalCacheEvictionListener<K1, V1> implements CacheEvictionListener<K1, V1> {
        public static final ThreadLocalCacheEvictionListener INSTANCE = new ThreadLocalCacheEvictionListener();
        
        public void onEviction(K1 key, V1 value) {
            if (value instanceof ThreadLocal) {
                ((ThreadLocal) value).remove();
            }
        }
    }
}
