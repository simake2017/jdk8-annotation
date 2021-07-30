/***** Lobxxx Translate Finished ******/
/*
 * Copyright (c) 1999, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package javax.management;

import java.security.AccessController;

import com.sun.jmx.mbeanserver.GetPropertyAction;

/**
 * This class represents the name of the Java implementation class of
 * the MBean. It is used for performing queries based on the class of
 * the MBean.
 * <p>
 *  此类表示MBean的Java实现类的名称。它用于根据MBean的类执行查询。
 * 
 * 
 * @serial include
 *
 * <p>The <b>serialVersionUID</b> of this class is <code>-1081892073854801359L</code>.
 *
 * @since 1.5
 */
@SuppressWarnings("serial")  // serialVersionUID is not constant
class ClassAttributeValueExp extends AttributeValueExp {

    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID = -2212731951078526753L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID = -1081892073854801359L;

    private static final long serialVersionUID;
    static {
        boolean compat = false;
        try {
            GetPropertyAction act = new GetPropertyAction("jmx.serial.form");
            String form = AccessController.doPrivileged(act);
            compat = (form != null && form.equals("1.0"));
        } catch (Exception e) {
            // OK: exception means no compat with 1.0, too bad
        }
        if (compat)
            serialVersionUID = oldSerialVersionUID;
        else
            serialVersionUID = newSerialVersionUID;
    }

    /**
    /* <p>
    /* 
     * @serial The name of the attribute
     *
     * <p>The <b>serialVersionUID</b> of this class is <code>-1081892073854801359L</code>.
     */
    private String attr;

    /**
     * Basic Constructor.
     * <p>
     *  基本构造函数。
     * 
     */
    public ClassAttributeValueExp() {
        /* Compatibility: we have an attr field that we must hold on to
        /* <p>
        /* 
           for serial compatibility, even though our parent has one too.  */
        super("Class");
        attr = "Class";
    }


    /**
     * Applies the ClassAttributeValueExp on an MBean. Returns the name of
     * the Java implementation class of the MBean.
     *
     * <p>
     *  在MBean上应用ClassAttributeValueExp。返回MBean的Java实现类的名称。
     * 
     * 
     * @param name The name of the MBean on which the ClassAttributeValueExp will be applied.
     *
     * @return  The ValueExp.
     *
     * @exception BadAttributeValueExpException
     * @exception InvalidApplicationException
     */
    public ValueExp apply(ObjectName name)
            throws BadStringOperationException, BadBinaryOpValueExpException,
                   BadAttributeValueExpException, InvalidApplicationException {
        // getAttribute(name);
        Object result = getValue(name);
        if  (result instanceof String) {
            return new StringValueExp((String)result);
        } else {
            throw new BadAttributeValueExpException(result);
        }
    }

    /**
     * Returns the string "Class" representing its value
     * <p>
     *  返回表示其值的字符串"Class"
     * 
     */
    public String toString()  {
        return attr;
    }


    protected Object getValue(ObjectName name) {
        try {
            // Get the class of the object
            MBeanServer server = QueryEval.getMBeanServer();
            return server.getObjectInstance(name).getClassName();
        } catch (Exception re) {
            return null;
            /* In principle the MBean does exist because otherwise we
               wouldn't be evaluating the query on it.  But it could
               potentially have disappeared in between the time we
               discovered it and the time the query is evaluated.

               Also, the exception could be a SecurityException.

               Returning null from here will cause
               BadAttributeValueExpException, which will in turn cause
            /* <p>
            /*  将不会评估它上的查询。但是它可能在我们发现它和查询被评估的时间之间消失。
            /* 
            /*  此外,异常可能是SecurityException。
            /* 
               this MBean to be omitted from the query result.  */
        }
    }

}
