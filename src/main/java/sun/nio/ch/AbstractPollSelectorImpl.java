/*
 * Copyright (c) 2001, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * An abstract selector impl.
 */

abstract class AbstractPollSelectorImpl
    extends SelectorImpl
{

    // The poll fd array
    PollArrayWrapper pollWrapper;

    // Initial capacity of the pollfd array
    protected final int INIT_CAP = 10;

    // The list of SelectableChannels serviced by this Selector
    /*
        key集合

     */
    protected SelectionKeyImpl[] channelArray;

    // In some impls the first entry of channelArray is bogus
    protected int channelOffset = 0;

    // The number of valid channels in this Selector's poll array
    protected int totalChannels;

    // True if this Selector has been closed
    private boolean closed = false;

    // Lock for close and cleanup
    private Object closeLock = new Object();

    AbstractPollSelectorImpl(SelectorProvider sp, int channels, int offset) {
        super(sp);
        this.totalChannels = channels;
        this.channelOffset = offset;
    }

    void putEventOps(SelectionKeyImpl sk, int ops) {
        synchronized (closeLock) {
            if (closed)
                throw new ClosedSelectorException();
            pollWrapper.putEventOps(sk.getIndex(), ops);
        }
    }

    public Selector wakeup() {
        pollWrapper.interrupt();
        return this;
    }

    protected abstract int doSelect(long timeout) throws IOException;

    protected void implClose() throws IOException {
        synchronized (closeLock) {
            if (closed)
                return;
            closed = true;
            // Deregister channels
            for(int i=channelOffset; i<totalChannels; i++) {
                SelectionKeyImpl ski = channelArray[i];
                assert(ski.getIndex() != -1);
                ski.setIndex(-1);
                deregister(ski);
                SelectableChannel selch = channelArray[i].channel();
                if (!selch.isOpen() && !selch.isRegistered())
                    ((SelChImpl)selch).kill();
            }
            implCloseInterrupt();
            pollWrapper.free();
            pollWrapper = null;
            selectedKeys = null;
            channelArray = null;
            totalChannels = 0;
        }
    }

    protected abstract void implCloseInterrupt() throws IOException;

    /**
     * Copy the information in the pollfd structs into the opss
     * of the corresponding Channels. Add the ready keys to the
     * ready queue.
     */
    /*
        在select 的时候 轮询 更新 所有的 key

     */
    protected int updateSelectedKeys() {
        int numKeysUpdated = 0;
        // Skip zeroth entry; it is for interrupts only
        /*
            这里的channelArray 数组 与 totalChannels是一一对应的关系
            位置顺序是对应的
         */
        for (int i=channelOffset; i<totalChannels; i++) {
            int rOps = pollWrapper.getReventOps(i);
            if (rOps != 0) {
                SelectionKeyImpl sk = channelArray[i]; //每次都是一个新的循环查找
                /*
                    设置该位置channel，接受到的内核时间

                 */
                pollWrapper.putReventOps(i, 0);

                /*
                    该selector注册的  key是否包含该channel 接受到的事件

                    wangyang impl 如果已经包含
                 */
                if (selectedKeys.contains(sk)) {
                    /*
                        更新进去

                     */
                    if (sk.channel.translateAndSetReadyOps(rOps, sk)) {
                        numKeysUpdated++;
                    }
                } else {
                    /*
                        这里会 把 有事件触发的 加到 selectedKeys

                        但是返回的时候的事件 是所有的事件
                     */
                    sk.channel.translateAndSetReadyOps(rOps, sk);
                    if ((sk.nioReadyOps() & sk.nioInterestOps()) != 0) { //这里是说当 存在 感兴趣的事件的时候 才会添加相应的 selectionKey
                        selectedKeys.add(sk);
                        numKeysUpdated++;
                    }
                }
            }
        }
        return numKeysUpdated;
    }

    /*
        注册selectionkey
        把对应的key，
     */
    protected void implRegister(SelectionKeyImpl ski) {
        synchronized (closeLock) {
            if (closed)
                throw new ClosedSelectorException();

            // Check to see if the array is large enough
            if (channelArray.length == totalChannels) {
                // Make a larger array
                int newSize = pollWrapper.totalChannels * 2; //每次都是 乘以 2 去 扩展
                SelectionKeyImpl temp[] = new SelectionKeyImpl[newSize];
                // Copy over
                for (int i=channelOffset; i<totalChannels; i++)
                    temp[i] = channelArray[i];
                channelArray = temp;
                // Grow the NativeObject poll array
                pollWrapper.grow(newSize);
            }
            channelArray[totalChannels] = ski; //totalChannels表示当前有效的 selectonKey数目
            ski.setIndex(totalChannels); //记录在Selector的索引 位置
            /*
                Selector只轮询 SelectionKey
                相应的channel，在下面
             */


            /*
                把对应的key加进数组里面
                wrapper
             */
            pollWrapper.addEntry(ski.channel);
            totalChannels++;
            keys.add(ski);
        }
    }

    protected void implDereg(SelectionKeyImpl ski) throws IOException {
        // Algorithm: Copy the sc from the end of the list and put it into
        // the location of the sc to be removed (since order doesn't
        // matter). Decrement the sc count. Update the index of the sc
        // that is moved.
        int i = ski.getIndex();
        assert (i >= 0);
        if (i != totalChannels - 1) {
            // Copy end one over it
            SelectionKeyImpl endChannel = channelArray[totalChannels-1];
            channelArray[i] = endChannel;
            endChannel.setIndex(i);
            pollWrapper.release(i);
            PollArrayWrapper.replaceEntry(pollWrapper, totalChannels - 1,
                                          pollWrapper, i);
        } else {
            pollWrapper.release(i);
        }
        // Destroy the last one
        channelArray[totalChannels-1] = null;
        totalChannels--;
        pollWrapper.totalChannels--;
        ski.setIndex(-1);
        // Remove the key from keys and selectedKeys
        keys.remove(ski);
        selectedKeys.remove(ski);
        deregister((AbstractSelectionKey)ski);
        SelectableChannel selch = ski.channel();
        if (!selch.isOpen() && !selch.isRegistered())
            ((SelChImpl)selch).kill();
    }

    static {
        Util.load();
    }

}
