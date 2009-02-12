/*
 * Copyright 2009 Andrew Wills
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.danann.cernunnos;

/**
 * Cernunnos uses this {@link Task} implementation behind-the-scenes where a 
 * task is expected but none is needed.  For example, the 
 * {@link AbstractContainerTask.SUPPRESS_EMPTY_SUBTASKS_WARNINGS} constant uses 
 * NoOpTask to signal that warnings about empty SUBTASKS reagents should not be 
 * issued.  
 * 
 * @author awills
 */
public class NoOpTask implements Task {

    /**
     * Provides an empty {@link Formula}.
     * 
     * @return An empty <code>Formula</code> object
     */
    public Formula getFormula() {
        return new SimpleFormula(getClass(), new Reagent[0]);
    }

    /**
     * Does nothing.
     */
    public void init(EntityConfig config) {}

    /**
     * Does absolutely nothing.
     *
     * @param req Representations the input to the current task.
     * @param res Representations the output of the current task.
     * @return The final, actual value of this <code>Phrase</code>.
     */
    public void perform(TaskRequest req, TaskResponse res) {}

}
