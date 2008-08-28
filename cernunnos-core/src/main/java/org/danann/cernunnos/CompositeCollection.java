/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.danann.cernunnos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A composite (layered) view of multiple Collections. The underlying Collections are not modifiable through this class
 * so it may be treated as if it has been wrapped with {@link java.util.Collections#unmodifiableCollection(Collection)}
 * <br/>
 * <b>WARNING The following operations require merging all composite Collection into a single Collection and are
 * before actual exection of the method logic and are expensive: O(N):</b>
 * {@link #size()}<br/>
 * {@link #iterator()}<br/>
 * {@link #toArray()}<br/>
 * {@link #toArray(Object[])}<br/>
 * {@link #equals(Object)}<br/>
 * {@link #hashCode()}<br/>
 * {@link #toString()}<br/>
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CompositeCollection<E> implements Collection<E> {
    private final List<Collection<E>> compositeCollections;
    
    public CompositeCollection(Collection<E>... sets) {
        if (sets == null || sets.length == 0) {
            throw new IllegalArgumentException("At least one set must be specified");
        }
        
        this.compositeCollections = Arrays.asList(sets);
    }
    
    public CompositeCollection(List<? extends Collection<E>> sets) {
        if (sets == null || sets.size() == 0) {
            throw new IllegalArgumentException("At least one set must be specified");
        }
        
        this.compositeCollections = new ArrayList<Collection<E>>(sets);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        for (final Collection<E> compositeCollection : this.compositeCollections) {
            if (compositeCollection.contains(o)) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
        for (final Object o : c) {
            boolean found = false;
            for (final Collection<E> compositeCollection : this.compositeCollections) {
                if (compositeCollection.contains(o)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                return false;
            }
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        for (final Collection<E> compositeCollection : this.compositeCollections) {
            if (!compositeCollection.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    public Iterator<E> iterator() {
        return Collections.unmodifiableCollection(this.getMergedCollection()).iterator();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    public int size() {
        return this.getMergedCollection().size();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray() {
        return this.getMergedCollection().toArray();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
        return this.getMergedCollection().toArray(a);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Collection))
            return false;
        
        return this.getMergedCollection().equals(o);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.getMergedCollection().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getMergedCollection().toString();
    }

    
    /**
     * Merges all of the composite collections into a singles representation
     */
    protected Collection<E> getMergedCollection() {
        final Collection<E> mergedCollection = createMergingCollection();

        //Iterate backwards to the layering is correct
        final ListIterator<Collection<E>> listIterator = this.compositeCollections.listIterator(this.compositeCollections.size());
        while (listIterator.hasPrevious()) {
            final Collection<E> compositeCollection = listIterator.previous();
            mergedCollection.addAll(compositeCollection);
        }
        
        return mergedCollection;
    }

    /**
     * @return The collection to do the merging with, LinkedList in this case
     */
    protected Collection<E> createMergingCollection() {
        return new LinkedList<E>();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(E o) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
}
