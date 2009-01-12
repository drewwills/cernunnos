/*
 * Copyright 2009 Eric Dalquist
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

import org.danann.cernunnos.CacheHelper.Factory;

/**
 * Provides common logic for CacheHelper Factories. Subclasses may override methods as needed
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractCacheHelperFactory<K extends Serializable, V> implements Factory<K, V> {
    /**
     * Returns {@link Class#getSimpleName()}
     * 
     * @see org.danann.cernunnos.CacheHelper.Factory#getCacheNamespace(java.io.Serializable)
     */
    public Serializable getCacheNamespace(K key) {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns false
     * 
     * @see org.danann.cernunnos.CacheHelper.Factory#isThreadSafe(java.io.Serializable, java.lang.Object)
     */
    public boolean isThreadSafe(K key, V instance) {
        return false;
    }
}
