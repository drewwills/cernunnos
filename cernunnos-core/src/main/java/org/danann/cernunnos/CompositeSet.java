/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.danann.cernunnos;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A composite (layered) view of multiple Sets. The underlying Sets are not modifiable through this class
 * so it may be treated as if it has been wrapped with {@link java.util.Collections#unmodifiableSet(Set)}
 * <br/>
 * <b>WARNING The following operations require merging all composite Set into a single Set and are
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
public class CompositeSet<E> extends CompositeCollection<E> implements Set<E> {
    public CompositeSet(Set<E>... sets) {
        super(sets);
    }

    public CompositeSet(List<Set<E>> sets) {
        super(sets);
    }

    /* (non-Javadoc)
     * @see org.danann.cernunnos.CompositeCollection#createMergingCollection()
     */
    @Override
    protected Collection<E> createMergingCollection() {
        return new HashSet<E>();
    }
}
