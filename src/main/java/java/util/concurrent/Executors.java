/***** Lobxxx Translate Finished ******/
/*
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

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * <p>
 *  由Doug Lea在JCP JSR-166专家组成员的帮助下撰写,并发布到公共领域,如http://creativecommons.org/publicdomain/zero/1.0/
 * 
 */

package java.util.concurrent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.AccessControlException;
import sun.security.util.SecurityConstants;

/**
 * Factory and utility methods for {@link Executor}, {@link
 * ExecutorService}, {@link ScheduledExecutorService}, {@link
 * ThreadFactory}, and {@link Callable} classes defined in this
 * package. This class supports the following kinds of methods:
 *
 * <ul>
 *   <li> Methods that create and return an {@link ExecutorService}
 *        set up with commonly useful configuration settings.
 *   <li> Methods that create and return a {@link ScheduledExecutorService}
 *        set up with commonly useful configuration settings.
 *   <li> Methods that create and return a "wrapped" ExecutorService, that
 *        disables reconfiguration by making implementation-specific methods
 *        inaccessible.
 *   <li> Methods that create and return a {@link ThreadFactory}
 *        that sets newly created threads to a known state.
 *   <li> Methods that create and return a {@link Callable}
 *        out of other closure-like forms, so they can be used
 *        in execution methods requiring {@code Callable}.
 * </ul>
 *
 * <p>
 *  此包中定义的{@link Executor},{@link ExecutorService},{@link ScheduledExecutorService},{@link ThreadFactory}
 * 和{@link Callable}类的工厂和实用程序方法。
 * 此类支持以下类型的方法：。
 * 
 * <ul>
 *  <li>使用常用的配置设置创建和返回{@link ExecutorService}的方法。
 *  <li>使用常用的配置设置创建和返回{@link ScheduledExecutorService}的方法。
 *  <li>创建和返回"封装"ExecutorService的方法,通过使实现特定的方法无法访问来禁用重新配置。
 *  <li>创建并返回{@link ThreadFactory}的方法,将新创建的线程设置为已知状态。
 *  <li>创建并返回{@link Callable}的其他类似闭包形式的方法,因此它们可以在需要{@code Callable}的执行方法中使用。
 * </ul>
 * 
 * 
 * @since 1.5
 * @author Doug Lea
 */
public class Executors {

    /**
     * Creates a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue.  At any point, at most
     * {@code nThreads} threads will be active processing tasks.
     * If additional tasks are submitted when all threads are active,
     * they will wait in the queue until a thread is available.
     * If any thread terminates due to a failure during execution
     * prior to shutdown, a new one will take its place if needed to
     * execute subsequent tasks.  The threads in the pool will exist
     * until it is explicitly {@link ExecutorService#shutdown shutdown}.
     *
     * <p>
     * 创建重用固定数量的线程操作一个共享无界队列的线程池。在任何时候,至多{@code nThreads}线程将是活动的处理任务。
     * 如果在所有线程都处于活动状态时提交其他任务,则它们将在队列中等待,直到线程可用。如果任何线程由于在关闭之前的执行期间的故障而终止,则如果需要执行后续任务,则新的线程将占据它的位置。
     * 池中的线程将存在,直到它显式{@link ExecutorService#shutdown shutdown}。
     * 
     * 
     * @param nThreads the number of threads in the pool
     * @return the newly created thread pool
     * @throws IllegalArgumentException if {@code nThreads <= 0}
     */
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }

    /**
     * Creates a thread pool that maintains enough threads to support
     * the given parallelism level, and may use multiple queues to
     * reduce contention. The parallelism level corresponds to the
     * maximum number of threads actively engaged in, or available to
     * engage in, task processing. The actual number of threads may
     * grow and shrink dynamically. A work-stealing pool makes no
     * guarantees about the order in which submitted tasks are
     * executed.
     *
     * <p>
     *  创建维护足够线程以支持给定并行性级别的线程池,并且可以使用多个队列来减少争用。并行性级别对应于主动参与任务处理或可用于参与任务处理的线程的最大数目。实际的线程数可以动态增长和收缩。
     * 工作窃取池不能保证执行提交的任务的顺序。
     * 
     * 
     * @param parallelism the targeted parallelism level
     * @return the newly created thread pool
     * @throws IllegalArgumentException if {@code parallelism <= 0}
     * @since 1.8
     */
    public static ExecutorService newWorkStealingPool(int parallelism) {
        return new ForkJoinPool
            (parallelism,
             ForkJoinPool.defaultForkJoinWorkerThreadFactory,
             null, true);
    }

    /**
     * Creates a work-stealing thread pool using all
     * {@link Runtime#availableProcessors available processors}
     * as its target parallelism level.
     * <p>
     *  使用所有{@link Runtime#availableProcessors可用处理器}创建工作窃取线程池作为其目标并行性级别。
     * 
     * 
     * @return the newly created thread pool
     * @see #newWorkStealingPool(int)
     * @since 1.8
     */
    public static ExecutorService newWorkStealingPool() {
        return new ForkJoinPool
            (Runtime.getRuntime().availableProcessors(),
             ForkJoinPool.defaultForkJoinWorkerThreadFactory,
             null, true);
    }

    /**
     * Creates a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue, using the provided
     * ThreadFactory to create new threads when needed.  At any point,
     * at most {@code nThreads} threads will be active processing
     * tasks.  If additional tasks are submitted when all threads are
     * active, they will wait in the queue until a thread is
     * available.  If any thread terminates due to a failure during
     * execution prior to shutdown, a new one will take its place if
     * needed to execute subsequent tasks.  The threads in the pool will
     * exist until it is explicitly {@link ExecutorService#shutdown
     * shutdown}.
     *
     * <p>
     * 创建一个线程池,重用固定数量的线程操作一个共享的无界队列,使用提供的ThreadFactory在需要时创建新线程。在任何时候,至多{@code nThreads}线程将是活动的处理任务。
     * 如果在所有线程都处于活动状态时提交其他任务,则它们将在队列中等待,直到线程可用。如果任何线程由于在关闭之前的执行期间的故障而终止,则如果需要执行后续任务,则新的线程将占据它的位置。
     * 池中的线程将存在,直到它显式{@link ExecutorService#shutdown shutdown}。
     * 
     * 
     * @param nThreads the number of threads in the pool
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created thread pool
     * @throws NullPointerException if threadFactory is null
     * @throws IllegalArgumentException if {@code nThreads <= 0}
     */
    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>(),
                                      threadFactory);
    }

    /**
     * Creates an Executor that uses a single worker thread operating
     * off an unbounded queue. (Note however that if this single
     * thread terminates due to a failure during execution prior to
     * shutdown, a new one will take its place if needed to execute
     * subsequent tasks.)  Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time. Unlike the otherwise equivalent
     * {@code newFixedThreadPool(1)} the returned executor is
     * guaranteed not to be reconfigurable to use additional threads.
     *
     * <p>
     *  创建一个Executor,它使用一个工作线程操作一个无界队列。 (但是,如果这个单个线程由于在关闭之前的执行期间的故障而终止,则如果需要执行后续任务,则新的线程将占据它的位置。
     * )任务被保证顺序地执行,并且不多于一个任务将是活动的在任何给定的时间。与其他等同的{@code newFixedThreadPool(1)}不同,返回的执行器不能重新配置为使用额外的线程。
     * 
     * 
     * @return the newly created single-threaded Executor
     */
    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }

    /**
     * Creates an Executor that uses a single worker thread operating
     * off an unbounded queue, and uses the provided ThreadFactory to
     * create a new thread when needed. Unlike the otherwise
     * equivalent {@code newFixedThreadPool(1, threadFactory)} the
     * returned executor is guaranteed not to be reconfigurable to use
     * additional threads.
     *
     * <p>
     *  创建一个Executor,它使用单个工作线程操作一个无界队列,并在需要时使用提供的ThreadFactory创建一个新线程。
     * 与其他等效的{@code newFixedThreadPool(1,threadFactory)}不同,返回的执行器不能重新配置以使用额外的线程。
     * 
     * 
     * @param threadFactory the factory to use when creating new
     * threads
     *
     * @return the newly created single-threaded Executor
     * @throws NullPointerException if threadFactory is null
     */
    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>(),
                                    threadFactory));
    }

    /**
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to {@code execute} will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool. Threads that have
     * not been used for sixty seconds are terminated and removed from
     * the cache. Thus, a pool that remains idle for long enough will
     * not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPoolExecutor} constructors.
     *
     * <p>
     * 创建一个根据需要创建新线程的线程池,但会在以前构造的线程可用时重用它。这些池通常会提高执行许多短暂异步任务的程序的性能。对{@code execute}的调用将重用以前构造的线程(如果可用)。
     * 如果没有现有线程可用,将创建一个新线程并将其添加到池中。未使用60秒的线程被终止并从高速缓存中删除。因此,保持空闲足够长时间的池不会消耗任何资源。
     * 注意,可以使用{@link ThreadPoolExecutor}构造函数创建具有相似属性但不同细节(例如,超时参数)的池。
     * 
     * 
     * @return the newly created thread pool
     */
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }

    /**
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available, and uses the provided
     * ThreadFactory to create new threads when needed.
     * <p>
     *  创建一个根据需要创建新线程的线程池,但会在以前构造的线程可用时重用它,并在需要时使用提供的ThreadFactory创建新线程。
     * 
     * 
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created thread pool
     * @throws NullPointerException if threadFactory is null
     */
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>(),
                                      threadFactory);
    }

    /**
     * Creates a single-threaded executor that can schedule commands
     * to run after a given delay, or to execute periodically.
     * (Note however that if this single
     * thread terminates due to a failure during execution prior to
     * shutdown, a new one will take its place if needed to execute
     * subsequent tasks.)  Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time. Unlike the otherwise equivalent
     * {@code newScheduledThreadPool(1)} the returned executor is
     * guaranteed not to be reconfigurable to use additional threads.
     * <p>
     *  创建单线程执行程序,可以调度命令在给定延迟后运行,或定期执行。 (但是,如果这个单个线程由于在关闭之前的执行期间的故障而终止,则如果需要执行后续任务,则新的线程将占据它的位置。
     * )任务被保证顺序地执行,并且不多于一个任务将是活动的在任何给定的时间。与其他等效的{@code newScheduledThreadPool(1)}不同,返回的执行器不能重新配置以使用额外的线程。
     * 
     * 
     * @return the newly created scheduled executor
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new DelegatedScheduledExecutorService
            (new ScheduledThreadPoolExecutor(1));
    }

    /**
     * Creates a single-threaded executor that can schedule commands
     * to run after a given delay, or to execute periodically.  (Note
     * however that if this single thread terminates due to a failure
     * during execution prior to shutdown, a new one will take its
     * place if needed to execute subsequent tasks.)  Tasks are
     * guaranteed to execute sequentially, and no more than one task
     * will be active at any given time. Unlike the otherwise
     * equivalent {@code newScheduledThreadPool(1, threadFactory)}
     * the returned executor is guaranteed not to be reconfigurable to
     * use additional threads.
     * <p>
     * 创建单线程执行程序,可以调度命令在给定延迟后运行,或定期执行。 (但是,如果这个单个线程由于在关闭之前的执行期间的故障而终止,则如果需要执行后续任务,则新的线程将占据它的位置。
     * )任务被保证顺序地执行,并且不多于一个任务将是活动的在任何给定的时间。
     * 与其他等效的{@code newScheduledThreadPool(1,threadFactory)}不同,返回的执行器不能重新配置为使用额外的线程。
     * 
     * 
     * @param threadFactory the factory to use when creating new
     * threads
     * @return a newly created scheduled executor
     * @throws NullPointerException if threadFactory is null
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return new DelegatedScheduledExecutorService
            (new ScheduledThreadPoolExecutor(1, threadFactory));
    }

    /**
     * Creates a thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     * <p>
     *  创建可以调度命令以在给定延迟后运行或者定期执行的线程池。
     * 
     * 
     * @param corePoolSize the number of threads to keep in the pool,
     * even if they are idle
     * @return a newly created scheduled thread pool
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    /**
     * Creates a thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     * <p>
     *  创建可以调度命令以在给定延迟后运行或者定期执行的线程池。
     * 
     * 
     * @param corePoolSize the number of threads to keep in the pool,
     * even if they are idle
     * @param threadFactory the factory to use when the executor
     * creates a new thread
     * @return a newly created scheduled thread pool
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException if threadFactory is null
     */
    public static ScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory) {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }

    /**
     * Returns an object that delegates all defined {@link
     * ExecutorService} methods to the given executor, but not any
     * other methods that might otherwise be accessible using
     * casts. This provides a way to safely "freeze" configuration and
     * disallow tuning of a given concrete implementation.
     * <p>
     *  返回一个对象,该对象将所有定义的{@link ExecutorService}方法委托给给定的执行器,而不是任何其他方法,否则可能使用casts访问。
     * 这提供了一种安全地"冻结"配置和不允许调整给定的具体实现的方式。
     * 
     * 
     * @param executor the underlying implementation
     * @return an {@code ExecutorService} instance
     * @throws NullPointerException if executor null
     */
    public static ExecutorService unconfigurableExecutorService(ExecutorService executor) {
        if (executor == null)
            throw new NullPointerException();
        return new DelegatedExecutorService(executor);
    }

    /**
     * Returns an object that delegates all defined {@link
     * ScheduledExecutorService} methods to the given executor, but
     * not any other methods that might otherwise be accessible using
     * casts. This provides a way to safely "freeze" configuration and
     * disallow tuning of a given concrete implementation.
     * <p>
     *  返回一个对象,它将所有定义的{@link ScheduledExecutorService}方法委托给给定的执行器,而不是任何其他方法,否则可以使用casts访问。
     * 这提供了一种安全地"冻结"配置和不允许调整给定的具体实现的方式。
     * 
     * 
     * @param executor the underlying implementation
     * @return a {@code ScheduledExecutorService} instance
     * @throws NullPointerException if executor null
     */
    public static ScheduledExecutorService unconfigurableScheduledExecutorService(ScheduledExecutorService executor) {
        if (executor == null)
            throw new NullPointerException();
        return new DelegatedScheduledExecutorService(executor);
    }

    /**
     * Returns a default thread factory used to create new threads.
     * This factory creates all new threads used by an Executor in the
     * same {@link ThreadGroup}. If there is a {@link
     * java.lang.SecurityManager}, it uses the group of {@link
     * System#getSecurityManager}, else the group of the thread
     * invoking this {@code defaultThreadFactory} method. Each new
     * thread is created as a non-daemon thread with priority set to
     * the smaller of {@code Thread.NORM_PRIORITY} and the maximum
     * priority permitted in the thread group.  New threads have names
     * accessible via {@link Thread#getName} of
     * <em>pool-N-thread-M</em>, where <em>N</em> is the sequence
     * number of this factory, and <em>M</em> is the sequence number
     * of the thread created by this factory.
     * <p>
     * 返回用于创建新线程的默认线程工厂。这个工厂在同一个{@link ThreadGroup}中创建一个Executor使用的所有新线程。
     * 如果有一个{@link java.lang.SecurityManager},它使用{@link System#getSecurityManager}组,否则调用此{@code defaultThreadFactory}
     * 方法的线程组。
     * 返回用于创建新线程的默认线程工厂。这个工厂在同一个{@link ThreadGroup}中创建一个Executor使用的所有新线程。
     * 每个新线程都创建为非守护线程,优先级设置为{@code Thread.NORM_PRIORITY}中的较小值和线程组中允许的最大优先级。
     * 新线程具有可通过pool-N-thread-M </em>的{@link Thread#getName}访问的名称,其中<em> N </em>是此工厂的序列号, M </em>是此工厂创建的主题的序号
     * 。
     * 每个新线程都创建为非守护线程,优先级设置为{@code Thread.NORM_PRIORITY}中的较小值和线程组中允许的最大优先级。
     * 
     * 
     * @return a thread factory
     */
    public static ThreadFactory defaultThreadFactory() {
        return new DefaultThreadFactory();
    }

    /**
     * Returns a thread factory used to create new threads that
     * have the same permissions as the current thread.
     * This factory creates threads with the same settings as {@link
     * Executors#defaultThreadFactory}, additionally setting the
     * AccessControlContext and contextClassLoader of new threads to
     * be the same as the thread invoking this
     * {@code privilegedThreadFactory} method.  A new
     * {@code privilegedThreadFactory} can be created within an
     * {@link AccessController#doPrivileged AccessController.doPrivileged}
     * action setting the current thread's access control context to
     * create threads with the selected permission settings holding
     * within that action.
     *
     * <p>Note that while tasks running within such threads will have
     * the same access control and class loader settings as the
     * current thread, they need not have the same {@link
     * java.lang.ThreadLocal} or {@link
     * java.lang.InheritableThreadLocal} values. If necessary,
     * particular values of thread locals can be set or reset before
     * any task runs in {@link ThreadPoolExecutor} subclasses using
     * {@link ThreadPoolExecutor#beforeExecute(Thread, Runnable)}.
     * Also, if it is necessary to initialize worker threads to have
     * the same InheritableThreadLocal settings as some other
     * designated thread, you can create a custom ThreadFactory in
     * which that thread waits for and services requests to create
     * others that will inherit its values.
     *
     * <p>
     *  返回一个线程工厂,用于创建与当前线程具有相同权限的新线程。
     * 此工厂创建与{@link Executors#defaultThreadFactory}相同设置的线程,另外将新线程的AccessControlContext和contextClassLoader设置为
     * 与调用此{@code privilegedThreadFactory}方法的线程相同。
     *  返回一个线程工厂,用于创建与当前线程具有相同权限的新线程。
     * 可以在{@link AccessController#doPrivileged AccessController.doPrivileged}操作中创建一个新的{@code privilegedThreadFactory}
     * ,设置当前线程的访问控制上下文以创建具有在该操作中保留的所选权限设置的线程。
     *  返回一个线程工厂,用于创建与当前线程具有相同权限的新线程。
     * 
     * <p>请注意,虽然在此类线程中运行的任务将具有与当前线程相同的访问控制和类加载器设置,但它们不需要具有相同的{@link java.lang.ThreadLocal}或{@link java.lang.InheritableThreadLocal }
     * 值。
     * 如果需要,可以在任何任务在{@link ThreadPoolExecutor}子类中使用{@link ThreadPoolExecutor#beforeExecute(Thread,Runnable)}
     * 运行之前设置或重置线程局部变量的特定值。
     * 此外,如果需要初始化工作线程以具有与其他指定线程相同的InheritableThreadLocal设置,则可以创建自定义ThreadFactory,该线程在其中等待并提供服务请求以创建将继承其值的其他请
     * 求。
     * 
     * 
     * @return a thread factory
     * @throws AccessControlException if the current access control
     * context does not have permission to both get and set context
     * class loader
     */
    public static ThreadFactory privilegedThreadFactory() {
        return new PrivilegedThreadFactory();
    }

    /**
     * Returns a {@link Callable} object that, when
     * called, runs the given task and returns the given result.  This
     * can be useful when applying methods requiring a
     * {@code Callable} to an otherwise resultless action.
     * <p>
     *  返回一个{@link Callable}对象,当被调用时,运行给定的任务并返回给定的结果。当应用需要{@code Callable}的方法时,这可能非常有用。
     * 
     * 
     * @param task the task to run
     * @param result the result to return
     * @param <T> the type of the result
     * @return a callable object
     * @throws NullPointerException if task null
     */

    /*
        根据 Runnable 返回一个  Callable  RunnableAdapter

     */
    public static <T> Callable<T> callable(Runnable task, T result) {
        if (task == null)
            throw new NullPointerException();
        return new RunnableAdapter<T>(task, result);
    }

    /**
     * Returns a {@link Callable} object that, when
     * called, runs the given task and returns {@code null}.
     * <p>
     *  返回一个{@link Callable}对象,当被调用时,运行给定的任务并返回{@code null}。
     * 
     * 
     * @param task the task to run
     * @return a callable object
     * @throws NullPointerException if task null
     */
    public static Callable<Object> callable(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        return new RunnableAdapter<Object>(task, null);
    }

    /**
     * Returns a {@link Callable} object that, when
     * called, runs the given privileged action and returns its result.
     * <p>
     *  返回一个{@link Callable}对象,当被调用时,运行给定的特权操作并返回其结果。
     * 
     * 
     * @param action the privileged action to run
     * @return a callable object
     * @throws NullPointerException if action null
     */
    public static Callable<Object> callable(final PrivilegedAction<?> action) {
        if (action == null)
            throw new NullPointerException();
        return new Callable<Object>() {
            public Object call() { return action.run(); }};
    }

    /**
     * Returns a {@link Callable} object that, when
     * called, runs the given privileged exception action and returns
     * its result.
     * <p>
     *  返回一个{@link Callable}对象,该对象在调用时运行给定的特权异常操作并返回其结果。
     * 
     * 
     * @param action the privileged exception action to run
     * @return a callable object
     * @throws NullPointerException if action null
     */
    public static Callable<Object> callable(final PrivilegedExceptionAction<?> action) {
        if (action == null)
            throw new NullPointerException();
        return new Callable<Object>() {
            public Object call() throws Exception { return action.run(); }};
    }

    /**
     * Returns a {@link Callable} object that will, when called,
     * execute the given {@code callable} under the current access
     * control context. This method should normally be invoked within
     * an {@link AccessController#doPrivileged AccessController.doPrivileged}
     * action to create callables that will, if possible, execute
     * under the selected permission settings holding within that
     * action; or if not possible, throw an associated {@link
     * AccessControlException}.
     * <p>
     * 返回一个{@link Callable}对象,当被调用时,将在当前访问控制上下文下执行给定的{@code callable}。
     * 此方法通常应在{@link AccessController#doPrivileged AccessController.doPrivileged}操作中调用,以创建可调用项,如果可能,该调用项将在该操
     * 作中保留的所选权限设置下执行;或者如果不可能,则抛出相关的{@link AccessControlException}。
     * 返回一个{@link Callable}对象,当被调用时,将在当前访问控制上下文下执行给定的{@code callable}。
     * 
     * 
     * @param callable the underlying task
     * @param <T> the type of the callable's result
     * @return a callable object
     * @throws NullPointerException if callable null
     */
    public static <T> Callable<T> privilegedCallable(Callable<T> callable) {
        if (callable == null)
            throw new NullPointerException();
        return new PrivilegedCallable<T>(callable);
    }

    /**
     * Returns a {@link Callable} object that will, when called,
     * execute the given {@code callable} under the current access
     * control context, with the current context class loader as the
     * context class loader. This method should normally be invoked
     * within an
     * {@link AccessController#doPrivileged AccessController.doPrivileged}
     * action to create callables that will, if possible, execute
     * under the selected permission settings holding within that
     * action; or if not possible, throw an associated {@link
     * AccessControlException}.
     *
     * <p>
     *  返回一个{@link Callable}对象,当被调用时,将在当前访问控制上下文下执行给定的{@code callable},当前上下文类加载器作为上下文类加载器。
     * 此方法通常应在{@link AccessController#doPrivileged AccessController.doPrivileged}操作中调用,以创建可调用项,如果可能,该调用项将在该操
     * 作中保留的所选权限设置下执行;或者如果不可能,则抛出相关的{@link AccessControlException}。
     *  返回一个{@link Callable}对象,当被调用时,将在当前访问控制上下文下执行给定的{@code callable},当前上下文类加载器作为上下文类加载器。
     * 
     * 
     * @param callable the underlying task
     * @param <T> the type of the callable's result
     * @return a callable object
     * @throws NullPointerException if callable null
     * @throws AccessControlException if the current access control
     * context does not have permission to both set and get context
     * class loader
     */
    public static <T> Callable<T> privilegedCallableUsingCurrentClassLoader(Callable<T> callable) {
        if (callable == null)
            throw new NullPointerException();
        return new PrivilegedCallableUsingCurrentClassLoader<T>(callable);
    }

    // Non-public classes supporting the public methods

    /**
     * A callable that runs given task and returns given result
     * <p>
     *  运行给定任务并返回给定结果的可调用项
     * 
     */

    /*
        call方法里面 包含  run 方法

     */
    static final class RunnableAdapter<T> implements Callable<T> {
        final Runnable task;
        final T result;
        RunnableAdapter(Runnable task, T result) {
            this.task = task;
            this.result = result;
        }
        public T call() {
            task.run();
            return result;
        }
    }

    /**
     * A callable that runs under established access control settings
     * <p>
     *  在已建立的访问控制设置下运行的可调用项
     * 
     */
    static final class PrivilegedCallable<T> implements Callable<T> {
        private final Callable<T> task;
        private final AccessControlContext acc;

        PrivilegedCallable(Callable<T> task) {
            this.task = task;
            this.acc = AccessController.getContext();
        }

        public T call() throws Exception {
            try {
                return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<T>() {
                        public T run() throws Exception {
                            return task.call();
                        }
                    }, acc);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        }
    }

    /**
     * A callable that runs under established access control settings and
     * current ClassLoader
     * <p>
     *  在已建立的访问控制设置和当前ClassLoader下运行的可调用
     * 
     */
    static final class PrivilegedCallableUsingCurrentClassLoader<T> implements Callable<T> {
        private final Callable<T> task;
        private final AccessControlContext acc;
        private final ClassLoader ccl;

        PrivilegedCallableUsingCurrentClassLoader(Callable<T> task) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                // Calls to getContextClassLoader from this class
                // never trigger a security check, but we check
                // whether our callers have this permission anyways.
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);

                // Whether setContextClassLoader turns out to be necessary
                // or not, we fail fast if permission is not available.
                sm.checkPermission(new RuntimePermission("setContextClassLoader"));
            }
            this.task = task;
            this.acc = AccessController.getContext();
            this.ccl = Thread.currentThread().getContextClassLoader();
        }

        public T call() throws Exception {
            try {
                return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<T>() {
                        public T run() throws Exception {
                            Thread t = Thread.currentThread();
                            ClassLoader cl = t.getContextClassLoader();
                            if (ccl == cl) {
                                return task.call();
                            } else {
                                t.setContextClassLoader(ccl);
                                try {
                                    return task.call();
                                } finally {
                                    t.setContextClassLoader(cl);
                                }
                            }
                        }
                    }, acc);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        }
    }

    /**
     * The default thread factory
     * <p>
     *  默认线程工厂
     * 
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                                  Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                          poolNumber.getAndIncrement() +
                         "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    /**
     * Thread factory capturing access control context and class loader
     * <p>
     *  线程工厂捕获访问控制上下文和类装载器
     * 
     */
    static class PrivilegedThreadFactory extends DefaultThreadFactory {
        private final AccessControlContext acc;
        private final ClassLoader ccl;

        PrivilegedThreadFactory() {
            super();
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                // Calls to getContextClassLoader from this class
                // never trigger a security check, but we check
                // whether our callers have this permission anyways.
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);

                // Fail fast
                sm.checkPermission(new RuntimePermission("setContextClassLoader"));
            }
            this.acc = AccessController.getContext();
            this.ccl = Thread.currentThread().getContextClassLoader();
        }

        public Thread newThread(final Runnable r) {
            return super.newThread(new Runnable() {
                public void run() {
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        public Void run() {
                            Thread.currentThread().setContextClassLoader(ccl);
                            r.run();
                            return null;
                        }
                    }, acc);
                }
            });
        }
    }

    /**
     * A wrapper class that exposes only the ExecutorService methods
     * of an ExecutorService implementation.
     * <p>
     *  一个包装类,只公开ExecutorService实现的ExecutorService方法。
     * 
     */
    static class DelegatedExecutorService extends AbstractExecutorService {
        private final ExecutorService e;
        DelegatedExecutorService(ExecutorService executor) { e = executor; }
        public void execute(Runnable command) { e.execute(command); }
        public void shutdown() { e.shutdown(); }
        public List<Runnable> shutdownNow() { return e.shutdownNow(); }
        public boolean isShutdown() { return e.isShutdown(); }
        public boolean isTerminated() { return e.isTerminated(); }
        public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
            return e.awaitTermination(timeout, unit);
        }
        public Future<?> submit(Runnable task) {
            return e.submit(task);
        }
        public <T> Future<T> submit(Callable<T> task) {
            return e.submit(task);
        }
        public <T> Future<T> submit(Runnable task, T result) {
            return e.submit(task, result);
        }
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
            return e.invokeAll(tasks);
        }
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                             long timeout, TimeUnit unit)
            throws InterruptedException {
            return e.invokeAll(tasks, timeout, unit);
        }
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
            return e.invokeAny(tasks);
        }
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                               long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            return e.invokeAny(tasks, timeout, unit);
        }
    }

    static class FinalizableDelegatedExecutorService
        extends DelegatedExecutorService {
        FinalizableDelegatedExecutorService(ExecutorService executor) {
            super(executor);
        }
        protected void finalize() {
            super.shutdown();
        }
    }

    /**
     * A wrapper class that exposes only the ScheduledExecutorService
     * methods of a ScheduledExecutorService implementation.
     * <p>
     *  一个包装类,只公开一个ScheduledExecutorService实现的ScheduledExecutorService方法。
     */
    static class DelegatedScheduledExecutorService
            extends DelegatedExecutorService
            implements ScheduledExecutorService {
        private final ScheduledExecutorService e;
        DelegatedScheduledExecutorService(ScheduledExecutorService executor) {
            super(executor);
            e = executor;
        }
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return e.schedule(command, delay, unit);
        }
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            return e.schedule(callable, delay, unit);
        }
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            return e.scheduleAtFixedRate(command, initialDelay, period, unit);
        }
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            return e.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }
    }

    /** Cannot instantiate. */
    private Executors() {}
}
