/*
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.net.SocketException;
import java.util.*;
import sun.misc.*;


/**
 * Base Selector implementation class.
 */

abstract class SelectorImpl
    extends AbstractSelector
{

    /*
        具体的selectedKeys
        这是具体 触发 的事件   的 key
     */
    // The set of keys with data ready for an operation
    protected Set<SelectionKey> selectedKeys;

    // The set of keys registered with this Selector
    /**
     * 这是所有的
     */
    protected HashSet<SelectionKey> keys;

    // Public views of the key sets
    /*
        这是封装的是 目前 注册在当前selector上的 keys

     */
    private Set<SelectionKey> publicKeys;             // Immutable

    /*
        这里是有事件操作的keys的集合， 对上面进行封装是因为需要通过 封装对外暴漏  避免被改动

     */
    //
    private Set<SelectionKey> publicSelectedKeys;     // Removal allowed, but not addition --> 允许移除 但不允许添加

    protected SelectorImpl(SelectorProvider sp) {
        super(sp);
        keys = new HashSet<SelectionKey>();
        selectedKeys = new HashSet<SelectionKey>();
        if (Util.atBugLevel("1.4")) {
            publicKeys = keys;
            publicSelectedKeys = selectedKeys;
        } else {
            publicKeys = Collections.unmodifiableSet(keys);
            publicSelectedKeys = Util.ungrowableSet(selectedKeys);
        }
    }

    public Set<SelectionKey> keys() {
        if (!isOpen() && !Util.atBugLevel("1.4"))
            throw new ClosedSelectorException();
        return publicKeys;
    }

    /**
     * publicKeys 是对 selectedKeys的包装
     * @return
     */
    public Set<SelectionKey> selectedKeys() {
        if (!isOpen() && !Util.atBugLevel("1.4"))
            throw new ClosedSelectorException();
        return publicSelectedKeys;
    }

    protected abstract int doSelect(long timeout) throws IOException;

    /*
        查询的时候 会使用 synchronize 关键 字

     */
    private int lockAndDoSelect(long timeout) throws IOException {
        synchronized (this) {
            if (!isOpen())
                throw new ClosedSelectorException();
            synchronized (publicKeys) {
                synchronized (publicSelectedKeys) {
                    return doSelect(timeout);
                }
            }
        }
    }

    /*
    wangyang
    important 重要
    这里当 timeout是0的时候 返回的是 -1， linux poll当timeout为 -1 的时候会一直阻塞
     */
    public int select(long timeout)
        throws IOException
    {
        if (timeout < 0)
            throw new IllegalArgumentException("Negative timeout");
        return lockAndDoSelect((timeout == 0) ? -1 : timeout);
    }

    /*
        进行select 阻塞，默认 是 0
     */
    public int select() throws IOException {
        return select(0);
    }

    /*
        wangyang
        这里会立即返回 
     */
    public int selectNow() throws IOException {
        return lockAndDoSelect(0);
    }

    public void implCloseSelector() throws IOException {
        wakeup();
        synchronized (this) {
            synchronized (publicKeys) {
                synchronized (publicSelectedKeys) {
                    implClose();
                }
            }
        }
    }

    protected abstract void implClose() throws IOException;

    void putEventOps(SelectionKeyImpl sk, int ops) { }

    /*
        selector 调用该方法，
        注册对应的 key对象（里面包含 channel，与selector--自己）
     */
    protected final SelectionKey register(AbstractSelectableChannel ch,
                                          int ops,
                                          Object attachment)
    {
        if (!(ch instanceof SelChImpl))
            throw new IllegalSelectorException();

        /*
            将 channel与selector封装进来

         */
        SelectionKeyImpl k = new SelectionKeyImpl((SelChImpl)ch, this);
        k.attach(attachment);
        synchronized (publicKeys) {
            /*
                channel，注册对应的key

             */
            implRegister(k);
        }
        k.interestOps(ops);
        return k;
    }

    protected abstract void implRegister(SelectionKeyImpl ski);

    void processDeregisterQueue() throws IOException {
        // Precondition: Synchronized on this, keys, and selectedKeys
        Set cks = cancelledKeys();
        synchronized (cks) {
            if (!cks.isEmpty()) {
                Iterator i = cks.iterator();
                while (i.hasNext()) {
                    SelectionKeyImpl ski = (SelectionKeyImpl)i.next();
                    try {
                        implDereg(ski);
                    } catch (SocketException se) {
                        IOException ioe = new IOException(
                            "Error deregistering key");
                        ioe.initCause(se);
                        throw ioe;
                    } finally {
                        i.remove();
                    }
                }
            }
        }
    }

    protected abstract void implDereg(SelectionKeyImpl ski) throws IOException;

    abstract public Selector wakeup();

}
