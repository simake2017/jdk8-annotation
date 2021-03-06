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

package java.util.concurrent.atomic;
import java.util.function.IntUnaryOperator;
import java.util.function.IntBinaryOperator;
import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * A reflection-based utility that enables atomic updates to
 * designated {@code volatile int} fields of designated classes.
 * This class is designed for use in atomic data structures in which
 * several fields of the same node are independently subject to atomic
 * updates.
 *
 * <p>Note that the guarantees of the {@code compareAndSet}
 * method in this class are weaker than in other atomic classes.
 * Because this class cannot ensure that all uses of the field
 * are appropriate for purposes of atomic access, it can
 * guarantee atomicity only with respect to other invocations of
 * {@code compareAndSet} and {@code set} on the same updater.
 *
 * <p>
 *  一个基于反射的实用程序,可以对指定类的指定{@code volatile int}字段进行原子更新。此类设计用于原子数据结构,其中同一节点的几个字段独立地进行原子更新。
 * 
 *  <p>请注意,此类中的{@code compareAndSet}方法的保证比其他原子类中的弱。
 * 因为这个类不能确保字段的所有使用都适合于原子访问的目的,所以它只能在相同更新器上对{@code compareAndSet}和{@code set}的其他调用保证原子性。
 * 
 * 
 * @since 1.5
 * @author Doug Lea
 * @param <T> The type of the object holding the updatable field
 */
public abstract class AtomicIntegerFieldUpdater<T> {
    /**
     * Creates and returns an updater for objects with the given field.
     * The Class argument is needed to check that reflective types and
     * generic types match.
     *
     * <p>
     *  为给定字段的对象创建并返回更新器。需要Class参数来检查反射类型和通用类型是否匹配。
     * 
     * 
     * @param tclass the class of the objects holding the field
     * @param fieldName the name of the field to be updated
     * @param <U> the type of instances of tclass
     * @return the updater
     * @throws IllegalArgumentException if the field is not a
     * volatile integer type
     * @throws RuntimeException with a nested reflection-based
     * exception if the class does not hold field or is the wrong type,
     * or the field is inaccessible to the caller according to Java language
     * access control
     */
    @CallerSensitive
    public static <U> AtomicIntegerFieldUpdater<U> newUpdater(Class<U> tclass,
                                                              String fieldName) {
        return new AtomicIntegerFieldUpdaterImpl<U>
            (tclass, fieldName, Reflection.getCallerClass());
    }

    /**
     * Protected do-nothing constructor for use by subclasses.
     * <p>
     *  受保护的无效构造函数,供子类使用。
     * 
     */
    protected AtomicIntegerFieldUpdater() {
    }

    /**
     * Atomically sets the field of the given object managed by this updater
     * to the given updated value if the current value {@code ==} the
     * expected value. This method is guaranteed to be atomic with respect to
     * other calls to {@code compareAndSet} and {@code set}, but not
     * necessarily with respect to other changes in the field.
     *
     * <p>
     *  如果当前值{@code ==}是预期值,则将此updater管理的给定对象的字段原子设置为给定的更新值。
     * 这个方法相对于对{@code compareAndSet}和{@code set}的其他调用,保证是原子的,但不一定是相对于字段中的其他变化。
     * 
     * 
     * @param obj An object whose field to conditionally set
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful
     * @throws ClassCastException if {@code obj} is not an instance
     * of the class possessing the field established in the constructor
     */
    public abstract boolean compareAndSet(T obj, int expect, int update);

    /**
     * Atomically sets the field of the given object managed by this updater
     * to the given updated value if the current value {@code ==} the
     * expected value. This method is guaranteed to be atomic with respect to
     * other calls to {@code compareAndSet} and {@code set}, but not
     * necessarily with respect to other changes in the field.
     *
     * <p><a href="package-summary.html#weakCompareAndSet">May fail
     * spuriously and does not provide ordering guarantees</a>, so is
     * only rarely an appropriate alternative to {@code compareAndSet}.
     *
     * <p>
     * 如果当前值{@code ==}是预期值,则将此updater管理的给定对象的字段原子设置为给定的更新值。
     * 这个方法相对于对{@code compareAndSet}和{@code set}的其他调用,保证是原子的,但不一定是相对于字段中的其他变化。
     * 
     *  <p> <a href="package-summary.html#weakCompareAndSet">可能会失败,并且不提供排序保证</a>,因此很少是{@code compareAndSet}的
     * 适当替代品。
     * 
     * 
     * @param obj An object whose field to conditionally set
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful
     * @throws ClassCastException if {@code obj} is not an instance
     * of the class possessing the field established in the constructor
     */
    public abstract boolean weakCompareAndSet(T obj, int expect, int update);

    /**
     * Sets the field of the given object managed by this updater to the
     * given updated value. This operation is guaranteed to act as a volatile
     * store with respect to subsequent invocations of {@code compareAndSet}.
     *
     * <p>
     *  将此updater管理的给定对象的字段设置为给定的更新值。此操作保证作为对随后调用{@code compareAndSet}的易失性存储。
     * 
     * 
     * @param obj An object whose field to set
     * @param newValue the new value
     */
    public abstract void set(T obj, int newValue);

    /**
     * Eventually sets the field of the given object managed by this
     * updater to the given updated value.
     *
     * <p>
     *  最终将由此更新器管理的给定对象的字段设置为给定的更新值。
     * 
     * 
     * @param obj An object whose field to set
     * @param newValue the new value
     * @since 1.6
     */
    public abstract void lazySet(T obj, int newValue);

    /**
     * Gets the current value held in the field of the given object managed
     * by this updater.
     *
     * <p>
     *  获取由此更新程序管理的给定对象的字段中保存的当前值。
     * 
     * 
     * @param obj An object whose field to get
     * @return the current value
     */
    public abstract int get(T obj);

    /**
     * Atomically sets the field of the given object managed by this updater
     * to the given value and returns the old value.
     *
     * <p>
     *  将由此updater管理的给定对象的字段原子设置为给定值,并返回旧值。
     * 
     * 
     * @param obj An object whose field to get and set
     * @param newValue the new value
     * @return the previous value
     */
    /*
        用循环的方式，设置新值
        如果返回false，那么继续循环，重新获取
     */
    public int getAndSet(T obj, int newValue) {
        int prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, newValue));
        return prev;
    }

    /**
     * Atomically increments by one the current value of the field of the
     * given object managed by this updater.
     *
     * <p>
     *  对此更新程序管理的给定对象的字段的当前值进行原子性增加。
     * 
     * 
     * @param obj An object whose field to get and set
     * @return the previous value
     */
    public int getAndIncrement(T obj) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev + 1;
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * Atomically decrements by one the current value of the field of the
     * given object managed by this updater.
     *
     * <p>
     *  将由此更新程序管理的给定对象的字段的当前值减1。
     * 
     * 
     * @param obj An object whose field to get and set
     * @return the previous value
     */
    public int getAndDecrement(T obj) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev - 1;
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * Atomically adds the given value to the current value of the field of
     * the given object managed by this updater.
     *
     * <p>
     *  将给定值原子地添加到此updater管理的给定对象的字段的当前值。
     * 
     * 
     * @param obj An object whose field to get and set
     * @param delta the value to add
     * @return the previous value
     */
    public int getAndAdd(T obj, int delta) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev + delta;
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * Atomically increments by one the current value of the field of the
     * given object managed by this updater.
     *
     * <p>
     *  对此更新程序管理的给定对象的字段的当前值进行原子性增加。
     * 
     * 
     * @param obj An object whose field to get and set
     * @return the updated value
     */
    public int incrementAndGet(T obj) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev + 1;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * Atomically decrements by one the current value of the field of the
     * given object managed by this updater.
     *
     * <p>
     * 将由此更新程序管理的给定对象的字段的当前值减1。
     * 
     * 
     * @param obj An object whose field to get and set
     * @return the updated value
     */
    public int decrementAndGet(T obj) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev - 1;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * Atomically adds the given value to the current value of the field of
     * the given object managed by this updater.
     *
     * <p>
     *  将给定值原子地添加到此updater管理的给定对象的字段的当前值。
     * 
     * 
     * @param obj An object whose field to get and set
     * @param delta the value to add
     * @return the updated value
     */
    public int addAndGet(T obj, int delta) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev + delta;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * Atomically updates the field of the given object managed by this updater
     * with the results of applying the given function, returning the previous
     * value. The function should be side-effect-free, since it may be
     * re-applied when attempted updates fail due to contention among threads.
     *
     * <p>
     *  使用应用给定函数的结果,以原子方式更新此updater管理的给定对象的字段,返回以前的值。该函数应该是无副作用的,因为它可能会在尝试更新失败时重新应用,因为线程之间的争用。
     * 
     * 
     * @param obj An object whose field to get and set
     * @param updateFunction a side-effect-free function
     * @return the previous value
     * @since 1.8
     */
    public final int getAndUpdate(T obj, IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get(obj);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * Atomically updates the field of the given object managed by this updater
     * with the results of applying the given function, returning the updated
     * value. The function should be side-effect-free, since it may be
     * re-applied when attempted updates fail due to contention among threads.
     *
     * <p>
     *  使用应用给定函数的结果,以原子方式更新由此更新程序管理的给定对象的字段,返回更新的值。该函数应该是无副作用的,因为它可能会在尝试更新失败时重新应用,因为线程之间的争用。
     * 
     * 
     * @param obj An object whose field to get and set
     * @param updateFunction a side-effect-free function
     * @return the updated value
     * @since 1.8
     */
    public final int updateAndGet(T obj, IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get(obj);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * Atomically updates the field of the given object managed by this
     * updater with the results of applying the given function to the
     * current and given values, returning the previous value. The
     * function should be side-effect-free, since it may be re-applied
     * when attempted updates fail due to contention among threads.  The
     * function is applied with the current value as its first argument,
     * and the given update as the second argument.
     *
     * <p>
     *  通过将给定函数应用于当前值和给定值,返回前一个值的结果,对由此更新器管理的给定对象的字段进行原子更新。该函数应该是无副作用的,因为它可能会在尝试更新失败时重新应用,因为线程之间的争用。
     * 该函数应用当前值作为其第一个参数,给定的更新作为第二个参数。
     * 
     * 
     * @param obj An object whose field to get and set
     * @param x the update value
     * @param accumulatorFunction a side-effect-free function of two arguments
     * @return the previous value
     * @since 1.8
     */
    public final int getAndAccumulate(T obj, int x,
                                      IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = get(obj);
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * Atomically updates the field of the given object managed by this
     * updater with the results of applying the given function to the
     * current and given values, returning the updated value. The
     * function should be side-effect-free, since it may be re-applied
     * when attempted updates fail due to contention among threads.  The
     * function is applied with the current value as its first argument,
     * and the given update as the second argument.
     *
     * <p>
     * 使用将给定函数应用于当前值和给定值的结果,以原子方式更新由此更新程序管理的给定对象的字段,返回更新的值。该函数应该是无副作用的,因为当尝试的更新由于线程之间的争用而失败时,它可以被重新应用。
     * 该函数应用当前值作为其第一个参数,给定的更新作为第二个参数。
     * 
     * 
     * @param obj An object whose field to get and set
     * @param x the update value
     * @param accumulatorFunction a side-effect-free function of two arguments
     * @return the updated value
     * @since 1.8
     */
    public final int accumulateAndGet(T obj, int x,
                                      IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = get(obj);
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * Standard hotspot implementation using intrinsics
     * <p>
     *  使用内在函数的标准热点实现
     * 
     */
    private static class AtomicIntegerFieldUpdaterImpl<T>
            extends AtomicIntegerFieldUpdater<T> {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private final long offset;
        private final Class<T> tclass;
        private final Class<?> cclass;

        AtomicIntegerFieldUpdaterImpl(final Class<T> tclass,
                                      final String fieldName,
                                      final Class<?> caller) {
            final Field field;
            final int modifiers;
            try {
                field = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Field>() {
                        public Field run() throws NoSuchFieldException {
                            return tclass.getDeclaredField(fieldName);
                        }
                    });
                modifiers = field.getModifiers();
                sun.reflect.misc.ReflectUtil.ensureMemberAccess(
                    caller, tclass, null, modifiers);
                ClassLoader cl = tclass.getClassLoader();
                ClassLoader ccl = caller.getClassLoader();
                if ((ccl != null) && (ccl != cl) &&
                    ((cl == null) || !isAncestor(cl, ccl))) {
                  sun.reflect.misc.ReflectUtil.checkPackageAccess(tclass);
                }
            } catch (PrivilegedActionException pae) {
                throw new RuntimeException(pae.getException());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            Class<?> fieldt = field.getType();
            if (fieldt != int.class)
                throw new IllegalArgumentException("Must be integer type");

            if (!Modifier.isVolatile(modifiers))
                throw new IllegalArgumentException("Must be volatile type");

            this.cclass = (Modifier.isProtected(modifiers) &&
                           caller != tclass) ? caller : null;
            this.tclass = tclass;
            offset = unsafe.objectFieldOffset(field);
        }

        /**
         * Returns true if the second classloader can be found in the first
         * classloader's delegation chain.
         * Equivalent to the inaccessible: first.isAncestor(second).
         * <p>
         *  如果第二个类加载器可以在第一个类加载器的委派链中找到,则返回true。相当于不可访问：first.isAncestor(second)。
         */
        private static boolean isAncestor(ClassLoader first, ClassLoader second) {
            ClassLoader acl = first;
            do {
                acl = acl.getParent();
                if (second == acl) {
                    return true;
                }
            } while (acl != null);
            return false;
        }

        private void fullCheck(T obj) {
            if (!tclass.isInstance(obj))
                throw new ClassCastException();
            if (cclass != null)
                ensureProtectedAccess(obj);
        }

        public boolean compareAndSet(T obj, int expect, int update) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.compareAndSwapInt(obj, offset, expect, update);
        }

        public boolean weakCompareAndSet(T obj, int expect, int update) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.compareAndSwapInt(obj, offset, expect, update);
        }

        /*
            这里是直接进行设置，不需要cas

         */
        public void set(T obj, int newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            unsafe.putIntVolatile(obj, offset, newValue);
        }

        public void lazySet(T obj, int newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            unsafe.putOrderedInt(obj, offset, newValue);
        }

        public final int get(T obj) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.getIntVolatile(obj, offset);
        }

        public int getAndSet(T obj, int newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.getAndSetInt(obj, offset, newValue);
        }

        public int getAndIncrement(T obj) {
            return getAndAdd(obj, 1);
        }

        public int getAndDecrement(T obj) {
            return getAndAdd(obj, -1);
        }

        public int getAndAdd(T obj, int delta) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.getAndAddInt(obj, offset, delta);
        }

        public int incrementAndGet(T obj) {
            return getAndAdd(obj, 1) + 1;
        }

        public int decrementAndGet(T obj) {
             return getAndAdd(obj, -1) - 1;
        }

        public int addAndGet(T obj, int delta) {
            return getAndAdd(obj, delta) + delta;
        }

        private void ensureProtectedAccess(T obj) {
            if (cclass.isInstance(obj)) {
                return;
            }
            throw new RuntimeException(
                new IllegalAccessException("Class " +
                    cclass.getName() +
                    " can not access a protected member of class " +
                    tclass.getName() +
                    " using an instance of " +
                    obj.getClass().getName()
                )
            );
        }
    }
}
