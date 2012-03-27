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
import java.util.concurrent.ConcurrentMap;


/**
 * API for a cache implementation for {@link Task} or {@link Phrase} resources that are
 * expensive to create.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @param <K>
 * @param <V>
 */
public interface CacheHelper<K extends Serializable, V> {
    public static final Reagent CACHE = new SimpleReagent("CACHE", "@cache", ReagentType.PHRASE, ConcurrentMap.class,
            "A shared ConcurrentMap to cache items in.  The default is the value of the 'Attributes.CACHE' " +
            "request attribute.",
            new AttributePhrase(Attributes.CACHE));

    public static final Reagent CACHE_MODEL = new SimpleReagent("CACHE_MODEL", "@cache-model", ReagentType.PHRASE, String.class,
            "Specify either NONE, ONE, or ALL.  The default is ONE.", new AttributePhrase(Attributes.CACHE_MODEL, new LiteralPhrase(CacheMode.ONE.toString())));

    /**
     * Get from cache or create and cache an object for the specified request, response and key. The passed
     * Factory will be used to create the object if it does not already exist.
     */
    public abstract V getCachedObject(TaskRequest req, TaskResponse res, K key, Factory<K, V> factory);
    
    public enum CacheMode {
        NONE,
        ONE,
        ALL;
    }
    
    /**
     * Factory used to create new instances of a cached object when needed.
     */
    public interface Factory<K extends Serializable, V> {
        /**
         * Create a new object to cache and return. The key should contain any nessesary information to
         * create the object
         */
        public V createObject(K key);
        
        /**
         * If the object returned by {@link #createObject(Serializable)} is thread-safe. If false the object will be cached but bound to just the current thread.
         */
        public boolean isThreadSafe(K key, V instance);
        
        /**
         * Gets the best mutex to use for synchronizing on when creating the object. This can NEVER return null.
         */
        public Object getMutex(K key);
        
        /**
         * Gets an optional namespace to use for caching to ensure keys with the same values for different objects don't
         * conflict. If this returns null the object is cached in the global namespace.
         */
        public Serializable getCacheNamespace(K key);
    }
    
    /**
     * Marks a cache Map that can notify listeners of entry eviction
     */
    public interface EvictionAwareCache<K1, V1> {
        /**
         * Register a cache eviction listener, if the same listener (determined by {@link CacheEvictionListener#equals(Object)}) is already
         * registered this call is ignored.
         * <br/>
         * This call is thread-safe
         */
        void registerCacheEvictionListener(CacheEvictionListener<K1, V1> listener);
    }
    
    /**
     * Listener that is notified of cache evictions
     */
    public interface CacheEvictionListener<K1, V1> {
        /**
         * Called when an element is evicted. Any {@link RuntimeException} thrown is logged and then swallowed
         */
        void onEviction(K1 key, V1 value);
    }
}