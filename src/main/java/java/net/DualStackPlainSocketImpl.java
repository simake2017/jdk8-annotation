/***** Lobxxx Translate Finished ******/
/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.net;

import java.io.IOException;
import java.io.FileDescriptor;
import sun.misc.SharedSecrets;
import sun.misc.JavaIOFileDescriptorAccess;

/**
 * This class defines the plain SocketImpl that is used on Windows platforms
 * greater or equal to Windows Vista. These platforms have a dual
 * layer TCP/IP stack and can handle both IPv4 and IPV6 through a
 * single file descriptor.
 *
 * <p>
 *  此类定义了在大于或等于Windows Vista的Windows平台上使用的普通SocketImpl。这些平台具有双层TCP / IP堆栈,并且可以通过单个文件描述符处理IPv4和IPV6。
 * 
 * 
 * @author Chris Hegarty
 */

class DualStackPlainSocketImpl extends AbstractPlainSocketImpl
{
    static JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();


    // true if this socket is exclusively bound
    private final boolean exclusiveBind;

    // emulates SO_REUSEADDR when exclusiveBind is true
    private boolean isReuseAddress;

    public DualStackPlainSocketImpl(boolean exclBind) {
        exclusiveBind = exclBind;
    }

    public DualStackPlainSocketImpl(FileDescriptor fd, boolean exclBind) {
        this.fd = fd;
        exclusiveBind = exclBind;
    }

    void socketCreate(boolean stream) throws IOException {
        if (fd == null)
            throw new SocketException("Socket closed");

        int newfd = socket0(stream, false /*v6 Only*/);

        fdAccess.set(fd, newfd);
    }

    void socketConnect(InetAddress address, int port, int timeout)
        throws IOException {
        int nativefd = checkAndReturnNativeFD();

        if (address == null)
            throw new NullPointerException("inet address argument is null.");

        int connectResult;
        if (timeout <= 0) {
            connectResult = connect0(nativefd, address, port);
        } else {
            configureBlocking(nativefd, false);
            try {
                connectResult = connect0(nativefd, address, port);
                if (connectResult == WOULDBLOCK) {
                    waitForConnect(nativefd, timeout);
                }
            } finally {
                configureBlocking(nativefd, true);
            }
        }
        /*
         * We need to set the local port field. If bind was called
         * previous to the connect (by the client) then localport field
         * will already be set.
         * <p>
         *  fdAccess.set(fd,newfd); }}
         * 
         *  void socketConnect(InetAddress address,int port,int timeout)throws IOException {int nativefd = checkAndReturnNativeFD();。
         * 
         *  if(address == null)throw new NullPointerException("inet address argument is null。");
         * 
         *  int connectResult; if(timeout <= 0){connectResult = connect0(nativefd,address,port); } else {configureBlocking(nativefd,false); try {connectResult = connect0(nativefd,address,port); if(connectResult == WOULDBLOCK){waitForConnect(nativefd,timeout); }} finally {configureBlocking(nativefd,true);} }} / *我们需要设置本地端口字段。
         * 如果绑定在connect(由客户端)之前调用,那么localport字段将已经设置。
         * 
         */
        if (localport == 0)
            localport = localPort0(nativefd);
    }


    /*
        绑定 Socket

     */
    void socketBind(InetAddress address, int port) throws IOException {
        int nativefd = checkAndReturnNativeFD();

        if (address == null)
            throw new NullPointerException("inet address argument is null.");

        bind0(nativefd, address, port, exclusiveBind); //调用native方法 绑定
        if (port == 0) {
            localport = localPort0(nativefd);
        } else {
            localport = port;
        }

        this.address = address;
    }

    /*
        监听 backlog是 监听 连接 数量

     */
    void socketListen(int backlog) throws IOException {
        int nativefd = checkAndReturnNativeFD();

        listen0(nativefd, backlog);
    }

    /*
        这里的ScoketImpl 是具体 socketImpl DualStackSocketImpl这种
     */
    void socketAccept(SocketImpl s) throws IOException {
        int nativefd = checkAndReturnNativeFD();

        if (s == null)
            throw new NullPointerException("socket is null");

        int newfd = -1;
        InetSocketAddress[] isaa = new InetSocketAddress[1];
        if (timeout <= 0) {
            newfd = accept0(nativefd, isaa);
        } else {
            configureBlocking(nativefd, false);
            try {
                waitForNewConnection(nativefd, timeout);
                newfd = accept0(nativefd, isaa);
                if (newfd != -1) {
                    configureBlocking(newfd, true);
                }
            } finally {
                configureBlocking(nativefd, true);
            }
        }
        /* Update (SocketImpl)s' fd */
        fdAccess.set(s.fd, newfd);
        /* Update socketImpls remote port, address and localport */
        InetSocketAddress isa = isaa[0];
        s.port = isa.getPort();
        s.address = isa.getAddress();
        s.localport = localport;
    }

    int socketAvailable() throws IOException {
        int nativefd = checkAndReturnNativeFD();
        return available0(nativefd);
    }

    void socketClose0(boolean useDeferredClose/*unused*/) throws IOException {
        if (fd == null)
            throw new SocketException("Socket closed");

        if (!fd.valid())
            return;

        final int nativefd = fdAccess.get(fd);
        fdAccess.set(fd, -1);
        close0(nativefd);
    }

    void socketShutdown(int howto) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        shutdown0(nativefd, howto);
    }

    // Intentional fallthrough after SO_REUSEADDR
    @SuppressWarnings("fallthrough")
    void socketSetOption(int opt, boolean on, Object value)
        throws SocketException {
        int nativefd = checkAndReturnNativeFD();

        if (opt == SO_TIMEOUT) {  // timeout implemented through select.
            return;
        }

        int optionValue = 0;

        switch(opt) {
            case SO_REUSEADDR :
                if (exclusiveBind) {
                    // SO_REUSEADDR emulated when using exclusive bind
                    isReuseAddress = on;
                    return;
                }
                // intentional fallthrough
            case TCP_NODELAY :
            case SO_OOBINLINE :
            case SO_KEEPALIVE :
                optionValue = on ? 1 : 0;
                break;
            case SO_SNDBUF :
            case SO_RCVBUF :
            case IP_TOS :
                optionValue = ((Integer)value).intValue();
                break;
            case SO_LINGER :
                if (on) {
                    optionValue =  ((Integer)value).intValue();
                } else {
                    optionValue = -1;
                }
                break;
    void socketClose0(boolean useDeferredClose/* <p>
    void socketClose0(boolean useDeferredClose/*  if(fd == null)throw new SocketException("Socket closed");
    void socketClose0(boolean useDeferredClose/* 
    void socketClose0(boolean useDeferredClose/*  if(！fd.valid())return;
    void socketClose0(boolean useDeferredClose/* 
    void socketClose0(boolean useDeferredClose/*  final int nativefd = fdAccess.get(fd); fdAccess.set(fd,-1); close0(nativefd); }}
    void socketClose0(boolean useDeferredClose/* 
    void socketClose0(boolean useDeferredClose/*  void socketShutdown(int howto)throws IOException {int nativefd = checkAndReturnNativeFD(); shutdown0(nativefd,howto); }
    void socketClose0(boolean useDeferredClose/* }。
    void socketClose0(boolean useDeferredClose/* 
    void socketClose0(boolean useDeferredClose/* //在SO_REUSEADDR之后有意逃脱@SuppressWarnings("fallthrough")void socketSetOption(int opt,boolean on,Object v
    void socketClose0(boolean useDeferredClose/* alue)throws SocketException {int nativefd = checkAndReturnNativeFD();。
    void socketClose0(boolean useDeferredClose/* 
            default :/* shouldn't get here */
                throw new SocketException("Option not supported");
        }

        setIntOption(nativefd, opt, optionValue);
    }

    int socketGetOption(int opt, Object iaContainerObj) throws SocketException {
        int nativefd = checkAndReturnNativeFD();

        // SO_BINDADDR is not a socket option.
        if (opt == SO_BINDADDR) {
            localAddress(nativefd, (InetAddressContainer)iaContainerObj);
            return 0;  // return value doesn't matter.
        }

        // SO_REUSEADDR emulated when using exclusive bind
        if (opt == SO_REUSEADDR && exclusiveBind)
            return isReuseAddress? 1 : -1;

        int value = getIntOption(nativefd, opt);

        switch (opt) {
            case TCP_NODELAY :
            case SO_OOBINLINE :
            case SO_KEEPALIVE :
            case SO_REUSEADDR :
                return (value == 0) ? -1 : 1;
        }
        return value;
    }

    void socketSendUrgentData(int data) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        sendOOB(nativefd, data);
    }

    private int checkAndReturnNativeFD() throws SocketException {
        if (fd == null || !fd.valid())
            throw new SocketException("Socket closed");

        return fdAccess.get(fd);
    }

    static final int WOULDBLOCK = -2;       // Nothing available (non-blocking)

    static {
        initIDs();
    }

    /* Native methods */

    static native void initIDs();

    static native int socket0(boolean stream, boolean v6Only) throws IOException;

    static native void bind0(int fd, InetAddress localAddress, int localport,
                             boolean exclBind)
        throws IOException;

    static native int connect0(int fd, InetAddress remote, int remotePort)
        throws IOException;

    static native void waitForConnect(int fd, int timeout) throws IOException;

    static native int localPort0(int fd) throws IOException;

    static native void localAddress(int fd, InetAddressContainer in) throws SocketException;

    static native void listen0(int fd, int backlog) throws IOException;

    static native int accept0(int fd, InetSocketAddress[] isaa) throws IOException;

    static native void waitForNewConnection(int fd, int timeout) throws IOException;

    static native int available0(int fd) throws IOException;

    static native void close0(int fd) throws IOException;

    static native void shutdown0(int fd, int howto) throws IOException;

    static native void setIntOption(int fd, int cmd, int optionValue) throws SocketException;

    static native int getIntOption(int fd, int cmd) throws SocketException;

    static native void sendOOB(int fd, int data) throws IOException;

    static native void configureBlocking(int fd, boolean blocking) throws IOException;
}
