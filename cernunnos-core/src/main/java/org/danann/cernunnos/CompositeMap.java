/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.danann.cernunnos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * A composite (layered) view of multiple Maps. The underlying Maps are not modifiable through this class so it may be
 * treated as if it has been wrapped with {@link java.util.Collections#unmodifiableMap(Map)}
 * <br/>
 * <b>WARNING The following operations require merging all composite Maps into a single object and are expensive:</b>
 * {@link #size()}<br/>
 * {@link #equals(Object)}<br/>
 * {@link #hashCode()}<br/>
 * {@link #toString()}<br/>
 * 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CompositeMap<K, V> implements Map<K, V> {
    private final List<Map<K, V>> compositeMaps;
    
    public CompositeMap(Map<K, V>... maps) {
        if (maps == null || maps.length == 0) {
            throw new IllegalArgumentException("At least one map must be specified");
        }
        
        this.compositeMaps = Arrays.asList(maps);
    }
    
    public CompositeMap(List<Map<K, V>> maps) {
        if (maps == null || maps.size() == 0) {
            throw new IllegalArgumentException("At least one map must be specified");
        }
        
        this.compositeMaps = new ArrayList<Map<K,V>>(maps);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        for (final Map<K, V> compositeMap : this.compositeMaps) {
            if (compositeMap.containsKey(key)) {
                return true;
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        for (final Map<K, V> compositeMap : this.compositeMaps) {
            if (compositeMap.containsValue(value)) {
                return true;
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        final List<Set<java.util.Map.Entry<K, V>>> sets = new ArrayList<Set<java.util.Map.Entry<K, V>>>(this.compositeMaps.size());
        for (final Map<K, V> compositeMap : this.compositeMaps) {
            sets.add(compositeMap.entrySet());
        }
        return new CompositeSet<java.util.Map.Entry<K, V>>(sets);
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public V get(Object key) {
        for (final Map<K, V> compositeMap : this.compositeMaps) {
            if (compositeMap.containsKey(key)) {
                return compositeMap.get(key);
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        for (final Map<K, V> compositeMap : this.compositeMaps) {
            if (!compositeMap.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() {
        final List<Set<K>> sets = new ArrayList<Set<K>>(this.compositeMaps.size());
        for (final Map<K, V> compositeMap : this.compositeMaps) {
            sets.add(compositeMap.keySet());
        }
        return new CompositeSet<K>(sets);
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return this.getMergedMap().size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection<V> values() {
        final List<Collection<V>> collections = new ArrayList<Collection<V>>(this.compositeMaps.size());
        for (final Map<K, V> compositeMap : this.compositeMaps) {
            collections.add(compositeMap.values());
        }
        return new CompositeCollection<V>(collections);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        
        return this.getMergedMap().equals(o);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.getMergedMap().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getMergedMap().toString();
    }
    
    protected Map<K, V> getMergedMap() {
        final Map<K, V> mergedMap = new HashMap<K, V>();

        //Iterate backwards to the layering is correct
        final ListIterator<Map<K, V>> listIterator = this.compositeMaps.listIterator(this.compositeMaps.size());
        while (listIterator.hasPrevious()) {
            final Map<K, V> compositeMap = listIterator.previous();
            mergedMap.putAll(compositeMap);
        }
        
        return mergedMap;
    }

    
    
    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends K, ? extends V> t) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }
    
    
}
