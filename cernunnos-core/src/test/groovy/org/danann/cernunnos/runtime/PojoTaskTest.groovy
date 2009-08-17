package org.danann.cernunnos.runtime;

import org.junit.Assert;
import org.junit.Test;

class PojoTaskTest {

    @Test
    void testEmptyPerform() {
    
        boolean wasUsed = false;
        def foo = { wasUsed = true; } as Foo;
        def attr = [ 'foo':foo ];
    
        def pojo = new PojoTask();
        pojo.setLocation('classpath:/org/danann/cernunnos/invoke-foo.bar.crn');
        pojo.setRequestAttributes(attr);
        pojo.afterPropertiesSet();
        
        pojo.perform();
        Assert.assertTrue(wasUsed);

    }

    @Test
    void testMapPerform() {
    
        boolean wasUsed = false;
        def foo = { wasUsed = true; } as Foo;
        def attr = [ 'foo':foo ];
    
        def pojo = new PojoTask();
        pojo.setLocation('classpath:/org/danann/cernunnos/invoke-foo.bar.crn');
        pojo.afterPropertiesSet();
        
        pojo.perform(attr);
        Assert.assertTrue(wasUsed);

    }

    @Test
    void testEmptyEvaluate() {
    
        def pojo = new PojoTask();
        pojo.setLocation('classpath:/org/danann/cernunnos/return-success.crn');
        pojo.afterPropertiesSet();
        
        def rslt = pojo.evaluate([ 'Attributes.STRING':'foo' ]);
        Assert.assertTrue(rslt.equals('success'));

    }

    @Test
    void testVarargsEvaluate() {
    
        def pojo = new PojoTask();
        pojo.setLocation('classpath:/org/danann/cernunnos/return-first-arg.crn');
        pojo.afterPropertiesSet();
        
        def rslt = pojo.evaluate('monkey');
        Assert.assertTrue(rslt.equals('monkey'));

    }

    @Test
    void testMapEvaluate() {
    
        def pojo = new PojoTask();
        pojo.setLocation('classpath:/org/danann/cernunnos/return-Attributes.STRING.crn');
        pojo.afterPropertiesSet();
        
        def rslt = pojo.evaluate([ 'Attributes.STRING':'foo' ]);
        Assert.assertTrue(rslt.equals('foo'));
    
    }
    
}

interface Foo { void bar(); }
