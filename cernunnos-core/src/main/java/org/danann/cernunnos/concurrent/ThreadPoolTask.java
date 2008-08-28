package org.danann.cernunnos.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ThreadPoolTask extends AbstractContainerTask {
     
    private Phrase attributeNamePhrase;
    private Phrase threadsPhrase;
    private Phrase useExistingPhrase;

    public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
             "Optional name under which the new ExecutorService will be registered as a request attribute.  If omitted, " +
             "the name 'ConcurrentAttributes.EXECUTOR_SERVICE' will be used.", new LiteralPhrase(ConcurrentAttributes.EXECUTOR_SERVICE));
     
     public static final Reagent THREADS = new SimpleReagent("THREADS", "@threads", ReagentType.PHRASE, String.class,
             "Number of threads to use in the thread pool, defaults to 4.", new LiteralPhrase(4));
     
     public static final Reagent USE_EXISTING = new SimpleReagent("USE_EXISTING", "@use-existing", ReagentType.PHRASE, String.class,
             "If true an ExecutorService that already exists for the specified attribute name will be used instead of creating a new " +
             "ExecutorService. Defaults to false.", new LiteralPhrase(Boolean.FALSE.toString()));

     /* (non-Javadoc)
      * @see org.danann.cernunnos.Bootstrappable#getFormula()
      */
     public Formula getFormula() {
         Reagent[] reagents = new Reagent[] {ATTRIBUTE_NAME, THREADS, USE_EXISTING, SUBTASKS};
         return new SimpleFormula(this.getClass(), reagents);
     }
     
     @Override
     public void init(EntityConfig config) {
         super.init(config);
         
         this.attributeNamePhrase = (Phrase) config.getValue(ATTRIBUTE_NAME);
         this.threadsPhrase = (Phrase) config.getValue(THREADS);
         this.useExistingPhrase = (Phrase) config.getValue(USE_EXISTING);
     }

     /* (non-Javadoc)
      * @see org.danann.cernunnos.Task#perform(org.danann.cernunnos.TaskRequest, org.danann.cernunnos.TaskResponse)
      */
     public void perform(TaskRequest req, TaskResponse res) {
         final String poolAttributeName = (String) this.attributeNamePhrase.evaluate(req, res);
         final boolean useExisting = Boolean.valueOf((String) this.useExistingPhrase.evaluate(req, res));
         
         final ExecutorService executorService;
         final boolean usingExisting;
         if (useExisting && req.hasAttribute(poolAttributeName)) {
             // Try using an existing pool
             executorService = (ExecutorService) req.getAttribute(poolAttributeName);
             usingExisting = true;
         }
         else {
             //No existing pool or configured to ignore it, create a new pool
             final int threads = Integer.parseInt((String) this.threadsPhrase.evaluate(req, res));
             executorService = Executors.newFixedThreadPool(threads);
             usingExisting = false;
         }
         
         try {
             //Provide the pool as a response attribute
             res.setAttribute(poolAttributeName, executorService);
             
             //Delegate to sub-task execution
             this.performSubtasks(req, res);
         }
         finally {
             //If using an existing pool it should not get shutdown here
             if (usingExisting) {
                 return;
             }
             
             //Cleanly shutdown the pool
             executorService.shutdown();
             
             //Wait for all tasks to complete
             try {
                 executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
             }
             catch (InterruptedException e) {
                 this.log.warn("Interrupted while waiting for thread pool to shutdown", e);
             }
         }
     }
}