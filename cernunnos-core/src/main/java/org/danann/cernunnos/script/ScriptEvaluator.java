/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.danann.cernunnos.script;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;

/**
 * Provides an abstraction for evaluating scripts that will automatically pre-compile and re-use that
 * {@link CompiledScript} if running on JDK6. If running on JDK5 it will simply cache the script String
 * and re-evaluate it directly each time.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ScriptEvaluator {
    protected static final Log LOGGER = LogFactory.getLog(ScriptEvaluator.class);
    
    protected static final Class<?> compilableClass;
    protected static final Method compileCompilableMethod;
    protected static final Method evalCompiledScriptWithContextMethod;
    protected static final Method evalCompiledScriptWithBindingsMethod;
    
    //Only do class & method loading once
    static {
        Class<?> compilableClassRef = null;
        Method evalCompiledScriptWithContextMethodRef = null;
        Method evalCompiledScriptWithBindingsMethodRef = null;
        Method compileCompilableMethodRef = null;
        try {
            compilableClassRef = ClassUtils.forName("javax.script.Compilable");
            compileCompilableMethodRef = BeanUtils.findMethod(compilableClassRef, "compile", new Class[] { String.class });

            final Class<?> compiledScriptClassRef = ClassUtils.forName("javax.script.CompiledScript");
            evalCompiledScriptWithContextMethodRef = BeanUtils.findMethod(compiledScriptClassRef, "eval", new Class[] { ScriptContext.class });
            evalCompiledScriptWithBindingsMethodRef = BeanUtils.findMethod(compiledScriptClassRef, "eval", new Class[] { Bindings.class });
        }
        catch (ClassNotFoundException e) {
            //Not JDK6+ don't use compiled scripts
        }

        if (compilableClassRef == null || evalCompiledScriptWithContextMethodRef == null || compileCompilableMethodRef == null) {
            LOGGER.info("Failed to load 'javax.script.Compilable', assuming JDK5 and no compilable script support");
            
            compilableClass = null;
            compileCompilableMethod = null;
            evalCompiledScriptWithContextMethod = null;
            evalCompiledScriptWithBindingsMethod = null;
        }
        else {
            LOGGER.info("Loaded 'javax.script.Compilable', assuming JDK6 and compilable script support");
            
            compilableClass = compilableClassRef;
            compileCompilableMethod = compileCompilableMethodRef;
            evalCompiledScriptWithContextMethod = evalCompiledScriptWithContextMethodRef;
            evalCompiledScriptWithBindingsMethod = evalCompiledScriptWithBindingsMethodRef;
        }
    }
    
    private final ScriptEngine scriptEngine;
    private final String script;
    
    private final Object compiledScript;

    public ScriptEvaluator(ScriptEngine engine, String script) {
        this.scriptEngine = engine;
        this.script = script;
        
        //Running under JDK5, don't do compilation
        if (compilableClass == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Running under JDK5, not using compilable script features for script:\n" + script);
            }

            this.compiledScript = null;
        }
        //ScriptEngine doesn't implement Compilable
        else if (!ClassUtils.isAssignable(compilableClass, engine.getClass())) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("ScriptEngine '" + engine + "' does not implement '" + compilableClass + "', not using compilable script features for script:\n" + script);
            }

            this.compiledScript = null;
        }
        //Find the Compilable.eval method to compile the script with
        else {
            Object compiledScript;
            try {
                compiledScript = compileCompilableMethod.invoke(this.scriptEngine, this.script);
            }
            catch (IllegalArgumentException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Failed to compile script using ScriptEngine '" + engine + "', not using compilable script features for script:\n" + script, e);
                }

                compiledScript = null;
            }
            catch (IllegalAccessException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Failed to compile script using ScriptEngine '" + engine + "', not using compilable script features for script:\n" + script, e);
                }

                compiledScript = null;
            }
            catch (InvocationTargetException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Failed to compile script using ScriptEngine '" + engine + "', not using compilable script features for script:\n" + script, e);
                }

                compiledScript = null;
            }
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Compiled script using ScriptEngine '" + engine + "', using compilable script features for script:\n" + script);
            }

            this.compiledScript = compiledScript;
        }
    }
    
    /**
     * Evaluates the script passed to the constructor, either using a CompiledScript if available
     * and supported by the ScriptEngine or directly with ScriptEngine.eval
     */
    public Object eval(ScriptContext scriptContext) throws ScriptException {
        if (this.compiledScript == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No compiled script available, invoking ScriptEngine.eval(String, ScriptContext) for script:\n" + this.script);
            }

            return this.scriptEngine.eval(this.script, scriptContext);
        }

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Compiled script available, invoking CompiledScript.eval(ScriptContext) for script:\n" + this.script);
            }

            return evalCompiledScriptWithContextMethod.invoke(this.compiledScript, scriptContext);
        }
        catch (Exception e) {
            if (e instanceof ScriptException) {
                throw (ScriptException)e;
            }
            
            throw new ScriptException(e);
        }
    }
    
    /**
     * Evaluates the script passed to the constructor, either using a CompiledScript if available
     * and supported by the ScriptEngine or directly with ScriptEngine.eval
     */
    public Object eval(Bindings bindings) throws ScriptException {
        if (this.compiledScript == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No compiled script available, invoking ScriptEngine.eval(String, Bindings) for script:\n" + this.script);
            }

            return this.scriptEngine.eval(this.script, bindings);
        }

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Compiled script available, invoking CompiledScript.eval(Bindings) for script:\n" + this.script);
            }

            return evalCompiledScriptWithBindingsMethod.invoke(this.compiledScript, bindings);
        }
        catch (Exception e) {
            if (e instanceof ScriptException) {
                throw (ScriptException)e;
            }
            
            throw new ScriptException(e);
        }
    }
}
