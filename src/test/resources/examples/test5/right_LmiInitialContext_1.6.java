/**
 * Copyright (C) 2002,2004 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: LmiInitialContext.java,v 1.6 2005-02-04 17:52:48 el-vadimo Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.jndi.lmi;

//java import
import java.rmi.Remote;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.ObjectFactory;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> LmiInitialContext </code> is the CAROL LMI JNDI SPI Context for
 * local context. This context is accessible only in local jvm, this is a
 * singleton (the close method do nothing). This cotext bind and return Local
 * Java Refferences
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @see javax.naming.Context
 * @see javax.naming.InitialContext
 * @version 1.0, 15/07/2002
 */
public class LmiInitialContext implements Context {

    /**
     * Lmi Environment
     */
    private static Hashtable lmiEnv = new Hashtable();

    /**
     * Lmi bindings
     */
    private static Hashtable bindings = new Hashtable();

    /**
     * Lmi Name Parser
     */
    private static NameParser lmiParser = new LmiNameParser();

    /**
     * Resolve a Remote Object: If this object is a reference return the
     * reference
     * @param o the object to resolve
     * @param n the name of this object
     * @return a <code>Referenceable</code> if o is a Reference and the
     *         inititial object o if else
     */
    private Object resolveObject(Object o, Name name) {
        try {
            if (o instanceof Reference) {
                // build of the Referenceable object with is Reference
                Reference objRef = (Reference) o;
                ObjectFactory objFact = (ObjectFactory) (Thread.currentThread().getContextClassLoader()
                        .loadClass(objRef.getFactoryClassName())).newInstance();
                return objFact.getObjectInstance(objRef, name, this, this.getEnvironment());
            } else {
                return o;
            }
        } catch (Exception e) {
            TraceCarol.error("LmiInitialContext.resolveObject()", e);
            return o;
        }
    }

    /**
     * Encode an Object : If the object is a referenceable bind this reference
     * @param o the object to encode
     * @return a <code>Remote Object</code> if o is a ressource o if else
     */
    private Object encodeObject(Object o) throws NamingException {
        try {
            if ((!(o instanceof Remote)) && (o instanceof Referenceable)) {
                return ((Referenceable) o).getReference();
            } else if ((!(o instanceof Remote)) && (o instanceof Reference)) {
                return (Reference) o;
            } else {
                return o;
            }
        } catch (Exception e) {
            throw new NamingException("" + e);
        }
    }

    /**
     * Constructor, load communication framework and instaciate initial contexts
     */
    public LmiInitialContext() throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.LmiInitialContext()");
        }
    }

    /**
     * Constructor, load communication framework and instaciate initial contexts
     */
    public LmiInitialContext(Hashtable ev) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.LmiInitialContext(Hashtable env)");
        }
        if (ev != null) {
            lmiEnv = (Hashtable) (ev.clone());
        }
    }

    // Inital context wrapper see the Context documentation for this methods
    public Object lookup(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.lookup(\"" + name + "\")");
        }
        if ((name == null) || (name.equals(""))) {
            return (new LmiInitialContext(lmiEnv));
        }
        Object o = bindings.get(name);
        if (o != null) {
            return resolveObject(o, new CompositeName(name));
        } else {
            throw new NameNotFoundException(name + " not found");
        }
    }

    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    public void bind(String name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.bind(\"" + name + "\","
                    + simpleClass(obj.getClass().getName()) + " object)");
        }
        if (name.equals("")) {
            throw new InvalidNameException("Cannot bind empty name");
        }
        if (bindings.get(name) != null) {
            throw new NameAlreadyBoundException("Use rebind to override");
        }
        bindings.put(name, encodeObject(obj));
    }

    public void bind(Name name, Object obj) throws NamingException {
        bind(name.toString(), obj);

    }

    public void rebind(String name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.rebind(\"" + name + "\","
                    + simpleClass(obj.getClass().getName()) + " object)");
        }
        if (name.equals("")) {
            throw new InvalidNameException("Cannot bind empty name");
        }
        bindings.put(name, encodeObject(obj));
    }

    public void rebind(Name name, Object obj) throws NamingException {
        rebind(name.toString(), obj);
    }

    public void unbind(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.unbind(\"" + name + "\")");
        }
        if (name.equals("")) {
            throw new InvalidNameException("Cannot unbind empty name");
        }
        bindings.remove(name);
    }

    public void unbind(Name name) throws NamingException {
        unbind(name.toString());
    }

    public void rename(String oldName, String newName) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.rename(\"" + oldName + "\",\"" + newName + "\")");
        }
        if (oldName.equals("") || newName.equals("")) throw new InvalidNameException("Cannot rename empty name");
        if (bindings.get(newName) != null) throw new NameAlreadyBoundException(newName + " is already bound");

        Object oldb = bindings.remove(oldName);
        if (oldb == null) throw new NameNotFoundException(oldName + " not bound");
        bindings.put(newName, oldb);
    }

    public void rename(Name oldname, Name newname) throws NamingException {
        rename(oldname.toString(), newname.toString());
    }

    public NamingEnumeration list(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.list(\"" + name + "\")");
        }
        if (name.equals("")) {
            return new LmiNames(bindings.keys());
        }

        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context) target).list("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    public NamingEnumeration list(Name name) throws NamingException {
        return list(name.toString());
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.listBindings(\"" + name + "\")/rmi name=\"");
        }
        if (name.equals("")) {
            return new LmiBindings(bindings.keys());
        }
        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context) target).listBindings("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        return listBindings(name.toString());
    }

    public void destroySubcontext(String name) throws NamingException {
        TraceCarol.error("LmiInitialContext.destroySubcontext(\"" + name + "\"): Not supported");
        throw new OperationNotSupportedException("LmiInitialContext.destroySubcontext(\"" + name + "\"): Not supported");
    }

    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    public Context createSubcontext(String name) throws NamingException {
        TraceCarol.error("LmiInitialContext.createSubcontext(\"" + name + "\"): Not supported");
        throw new OperationNotSupportedException("LmiInitialContext.createSubcontext(\"" + name + "\"): Not supported");
    }

    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

    public Object lookupLink(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.lookupLink(\"" + name + "\")");
        }
        return lookup(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    public NameParser getNameParser(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.getNameParser(\"" + name + "\")");
        }
        return lmiParser;
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return getNameParser(name.toString());
    }

    public String composeName(String name, String prefix) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.composeName(" + name + "," + prefix + ")");
        }
        Name result = composeName(new CompositeName(name), new CompositeName(prefix));
        return result.toString();
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.composeName(" + name + "," + prefix + ")");
        }
        Name result = (Name) (prefix.clone());
        result.addAll(name);
        return result;
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.addToEnvironment(\"" + propName + "\","
                    + simpleClass(propVal.getClass().getName()) + " object)");
        }
        if (lmiEnv == null) {
            lmiEnv = new Hashtable();
        }
        return lmiEnv.put(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.removeFromEnvironment(\"" + propName + "\")");
        }
        if (lmiEnv == null) return null;
        return lmiEnv.remove(propName);
    }

    public Hashtable getEnvironment() throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.getEnvironment()");
        }
        if (lmiEnv == null) {
            lmiEnv = new Hashtable();
        }
        return lmiEnv;
    }

    public void close() throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.close()");
        }
    }

    public String getNameInNamespace() throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LmiInitialContext.getNameInNamespace()");
        }
        return "lmiContext";
    }

    /**
     * Just the name of the class without the package
     */
    private String simpleClass(String c) {
        return c.substring(c.lastIndexOf('.') + 1);
    }

    // Class for enumerating name/class pairs
    class LmiNames implements NamingEnumeration {

        Enumeration names;

        LmiNames(Enumeration names) {
            this.names = names;
        }

        public boolean hasMoreElements() {
            return names.hasMoreElements();
        }

        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        public Object nextElement() {
            String name = (String) names.nextElement();
            String className = bindings.get(name).getClass().getName();
            return new NameClassPair(name, className);
        }

        public Object next() throws NamingException {
            return nextElement();
        }

        public void close() throws NamingException {
            names = null;
        }
    }

    // Class for enumerating bindings
    class LmiBindings implements NamingEnumeration {

        Enumeration names;

        LmiBindings(Enumeration names) {
            this.names = names;
        }

        public boolean hasMoreElements() {
            return names.hasMoreElements();
        }

        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        public Object nextElement() {
            String name = (String) names.nextElement();
            return new Binding(name, bindings.get(name));
        }

        public Object next() throws NamingException {
            return nextElement();
        }

        public void close() throws NamingException {
            names = null;
        }
    }
}
