package org.danann.cernunnos.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link RejectedExecutionHandler} that waits for additional space to be available in the queue
 * used by the {@link ThreadPoolExecutor} and places the {@link Runnable} in the queue.
 */
public final class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {
     public static final BlockingRejectedExecutionHandler INSTANCE = new BlockingRejectedExecutionHandler();
     private static final Log LOG = LogFactory.getLog(BlockingRejectedExecutionHandler.class);

    /* (non-Javadoc)
     * @see java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.lang.Runnable, java.util.concurrent.ThreadPoolExecutor)
     */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (executor.isShutdown()) {
            throw new RejectedExecutionException();
        }
        
        final BlockingQueue<Runnable> poolQueue = executor.getQueue();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Putting Runnable into BlockingQueue(size=" + poolQueue.size() + ") directly and potentially waiting due to executor rejection");
        }
        
        try {
            poolQueue.put(r);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Put Runnable into BlockingQueue(size=" + poolQueue.size() + ") directly due to executor rejection");
            }
        }
        catch (InterruptedException e) {
            throw new RejectedExecutionException("Interrupted while waiting to queue Runnable", e);
        }
    }
 }