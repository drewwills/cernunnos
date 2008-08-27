package org.danann.cernunnos.concurrent;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
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

    public static final Reagent EXECUTOR_SERVICE = new SimpleReagent("EXECUTOR_SERVICE", "@executor-service", ReagentType.PHRASE,
    			ExecutorService.class, "Optional instance of ExecutorService.  The default is a request attribute under the " +
    			"name 'ConcurrentAttributes.EXECUTOR_SERVICE'.", new AttributePhrase(ConcurrentAttributes.EXECUTOR_SERVICE));

    /* (non-Javadoc)
     * @see org.danann.cernunnos.Bootstrappable#getFormula()
     */
    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {EXECUTOR_SERVICE, AbstractContainerTask.SUBTASKS};
        return new SimpleFormula(this.getClass(), reagents);
    }
    
    @Override
    public void init(EntityConfig config) {
        super.init(config);        
        this.executorServicePhrase = (Phrase) config.getValue(EXECUTOR_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.danann.cernunnos.Task#perform(org.danann.cernunnos.TaskRequest, org.danann.cernunnos.TaskResponse)
     */
    public void perform(final TaskRequest req, final TaskResponse res) {
    	
        final ExecutorService executorService = (ExecutorService) executorServicePhrase.evaluate(req, res);
        
        // Copy all attributes into the response to ensure changes to higher level request objects don't
        // break execution of child tasks.
        final Map<String, Object> attributes = req.getAttributes();
        for (final Map.Entry<String, Object> attributeEntry : attributes.entrySet()) {
            res.setAttribute(attributeEntry.getKey(), attributeEntry.getValue());
        }
        
        
        //Submit the sub-tasks to the thread pool
        executorService.submit(new Runnable() {
            public void run() {
                ConcurrentTask.this.performSubtasks(req, res);                
            }
        });
        
    }
    
}
