package org.danann.cernunnos.concurrent;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.AttributePhrase;
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
public class ConcurrentTask extends AbstractContainerTask {

    private Phrase executorServicePhrase;
    private Phrase failFastPhrase;

    public static final Reagent EXECUTOR_SERVICE = new SimpleReagent("EXECUTOR_SERVICE", "@executor-service", ReagentType.PHRASE,
    			ExecutorService.class, "Optional instance of ExecutorService.  The default is a request attribute under the " +
    			"name 'ConcurrentAttributes.EXECUTOR_SERVICE'.", new AttributePhrase(ConcurrentAttributes.EXECUTOR_SERVICE));
    
    public static final Reagent FAIL_FAST = new SimpleReagent("FAIL_FAST", "@fail-fast", ReagentType.PHRASE,
            String.class, "If true an exception from a sub task will cause the parent thread pool to be shutdown immediately. The" +
    		"default is true.", new LiteralPhrase("true"));

    /* (non-Javadoc)
     * @see org.danann.cernunnos.Bootstrappable#getFormula()
     */
    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {EXECUTOR_SERVICE, FAIL_FAST, AbstractContainerTask.SUBTASKS};
        return new SimpleFormula(this.getClass(), reagents);
    }
    
    @Override
    public void init(EntityConfig config) {
        super.init(config);        
        this.executorServicePhrase = (Phrase) config.getValue(EXECUTOR_SERVICE);
        this.failFastPhrase = (Phrase) config.getValue(FAIL_FAST);
    }
    
    /* (non-Javadoc)
     * @see org.danann.cernunnos.Task#perform(org.danann.cernunnos.TaskRequest, org.danann.cernunnos.TaskResponse)
     */
    public void perform(final TaskRequest req, final TaskResponse res) {
    	
        final ExecutorService executorService = (ExecutorService) executorServicePhrase.evaluate(req, res);
        final boolean failFast = Boolean.parseBoolean((String) this.failFastPhrase.evaluate(req, res));
        
        // Copy all attributes into the response to ensure changes to higher level request objects don't
        // break execution of child tasks.
        final Map<String, Object> attributes = req.getAttributes();
        for (final Map.Entry<String, Object> attributeEntry : attributes.entrySet()) {
            res.setAttribute(attributeEntry.getKey(), attributeEntry.getValue());
        }
        
        
        //Submit the sub-tasks to the thread pool
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    ConcurrentTask.this.performSubtasks(req, res);
                }
                catch (Throwable t) {
                    if (failFast) {
                        //We want this as close to the top of the catch as possible so the pool is shutdown as soon as possible
                        executorService.shutdown();
                        if (executorService instanceof ThreadPoolExecutor) {
                            ((ThreadPoolExecutor)executorService).getQueue().clear();
                        }
                        log.debug("Shut down ExecutorService due to exception from ConcurrentTask subtask");
                    }
                    
                    final String msg;
                    if (failFast) {
                        msg = " The parent ExecutorService has been shutdown since fail-fast was set to true";
                    }
                    else {
                        msg = " Conccurent execution will continue.";
                    }
                    log.error("Exception thrown while performing subtask in its own thread." + msg, t);
                }
            }
        });
        
    }
    
}
