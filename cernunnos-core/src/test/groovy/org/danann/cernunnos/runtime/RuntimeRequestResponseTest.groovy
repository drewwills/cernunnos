package org.danann.cernunnos.runtime;

import org.junit.Assert;
import org.junit.Test;

class RuntimeRequestResponseTest {

    @Test
    void testMapConstructor() {
    
        def attrs = [ foo:'bar' ];
        def rrr = new RuntimeRequestResponse(attrs);
        
        Assert.assertTrue('RuntimeRequestResponse does not contain the expected attribute', rrr.getAttribute('foo').equals('bar'));
    
    }

}
