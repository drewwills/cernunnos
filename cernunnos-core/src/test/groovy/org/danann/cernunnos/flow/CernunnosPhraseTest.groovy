package org.danann.cernunnos.flow;

import org.junit.Assert;
import org.junit.Test;

import org.danann.cernunnos.CacheHelper;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.danann.cernunnos.runtime.SimpleEntityConfig;
import org.danann.cernunnos.runtime.XmlGrammar;

class CernunnosPhraseTest {

    @Test
    void testUseTaskNotLocation() {
    
        CernunnosPhrase cp = new CernunnosPhrase();

        // The task we will invoke...
        boolean taskUsed = false;
        def k = { req, res -> taskUsed = true; } as Task;
        
        // Reagents to bootstrap the CernunnosPhrase...
        def mappings = [
                    (CernunnosPhrase.TASK):new LiteralPhrase(k),
                    (CacheHelper.CACHE):CacheHelper.CACHE.getDefault(),
                    (CacheHelper.CACHE_MODEL):CacheHelper.CACHE_MODEL.getDefault(),
                    (ResourceHelper.CONTEXT_SOURCE):ResourceHelper.CONTEXT_SOURCE.getDefault(),
                    (ResourceHelper.LOCATION_PHRASE):ResourceHelper.LOCATION_PHRASE.getDefault()
                ];
        def config = new SimpleEntityConfig(XmlGrammar.getMainGrammar(), 'crn', null, cp.getFormula(), mappings);
        
        cp.init(config);
        cp.evaluate(new RuntimeRequestResponse(), new RuntimeRequestResponse());
        
        Assert.assertTrue('Failed to use the specified TASK reagent', taskUsed);
                
    }
    
    @Test
    void testUseLocationNotTask() {
    
        CernunnosPhrase cp = new CernunnosPhrase();
        
        // Reagents to bootstrap the CernunnosPhrase...
        def phr = { req, res -> return 'classpath:/org/danann/cernunnos/flow/return-success.crn'; } as Phrase;
        def mappings = [
                    (CernunnosPhrase.TASK):phr,
                    (CacheHelper.CACHE):CacheHelper.CACHE.getDefault(),
                    (CacheHelper.CACHE_MODEL):CacheHelper.CACHE_MODEL.getDefault(),
                    (ResourceHelper.CONTEXT_SOURCE):ResourceHelper.CONTEXT_SOURCE.getDefault(),
                    (ResourceHelper.LOCATION_PHRASE):phr
                ];
        def config = new SimpleEntityConfig(XmlGrammar.getMainGrammar(), 'crn', null, cp.getFormula(), mappings);
        
        cp.init(config);
        def req = new RuntimeRequestResponse();
        req.setAttribute('Attributes.ORIGIN', 'classpath:/not/applicable');
        def rslt = cp.evaluate(req, new RuntimeRequestResponse());
        
        Assert.assertTrue('Failed to use the specified LOCATION reagent', rslt.equals('success'));
                
    }

}
