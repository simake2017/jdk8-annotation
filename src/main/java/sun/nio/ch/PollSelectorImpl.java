/*
 * Copyright (c) 2001, 2010, Oracle and/or its affiliates. All rights reserved.
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
import java.util.*;
import sun.misc.*;


/**
 * An implementation of Selector for Solaris.
 */

class PollSelectorImpl
    extends AbstractPollSelectorImpl
{

    // File descriptors used for interrupt
    private int fd0;
    private int fd1;

    // Lock for interrupt triggering and clearing
    private Object interruptLock = new Object();
    private boolean interruptTriggered = false;

    /**
     * Package private constructor called by factory method in
     * the abstract superclass Selector.
     */
    PollSelectorImpl(SelectorProvider sp) {
        /**
         * wangyang
         *  这里传进来的是 channels是 1
         */
        super(sp, 1, 1);
        /*
            获取一个管道 文件
            long类型
         */
        long pipeFds = IOUtil.makePipe(false);
        fd0 = (int) (pipeFds >>> 32); // 高32位 读
        fd1 = (int) pipeFds; //低32位 写
        pollWrapper = new PollArrayWrapper(INIT_CAP); // 存放 socket 结构体 ，存放在 堆外内存 中
        pollWrapper.initInterrupt(fd0, fd1);
        channelArray = new SelectionKeyImpl[INIT_CAP];  ////--> 用于本次 轮询到的 selectionKey的数组存放，key当中 有 channel selector 事件的 引用
    }

    protected int doSelect(long timeout)
        throws IOException
    {
        if (channelArray == null)
            throw new ClosedSelectorException();
        processDeregisterQueue();
        try {
            begin();
            pollWrapper.poll(totalChannels, 0, timeout);
        } finally {
            end();
        }
        processDeregisterQueue();
        /**
         * wangyang impl 在这里进行 获取到 selectedKey 的 更新
         */
        int numKeysUpdated = updateSelectedKeys();
        if (pollWrapper.getReventOps(0) != 0) {
            // Clear the wakeup pipe
            pollWrapper.putReventOps(0, 0);
            synchronized (interruptLock) {
                IOUtil.drain(fd0);
                interruptTriggered = false;
            }
        }
        return numKeysUpdated;
    }

    protected void implCloseInterrupt() throws IOException {
        // prevent further wakeup
        synchronized (interruptLock) {
            interruptTriggered = true;
        }
        FileDispatcherImpl.closeIntFD(fd0);
        FileDispatcherImpl.closeIntFD(fd1);
        fd0 = -1;
        fd1 = -1;
        pollWrapper.release(0);
    }

    /*
        wakeup有2个作用  ，通过管道 发送的方式


     */
    public Selector wakeup() {
        synchronized (interruptLock) {
            if (!interruptTriggered) {
                pollWrapper.interrupt();
                interruptTriggered = true;
            }
        }
        return this;
    }

}
