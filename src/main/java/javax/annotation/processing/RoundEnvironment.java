/***** Lobxxx Translate Finished ******/
/*
 * Copyright (c) 2005, 2007, Oracle and/or its affiliates. All rights reserved.
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

package javax.annotation.processing;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.lang.annotation.Annotation;

/**
 * An annotation processing tool framework will {@linkplain
 * Processor#process provide an annotation processor with an object
 * implementing this interface} so that the processor can query for
 * information about a round of annotation processing.
 *
 * <p>
 *  注释处理工具框架将{@linkplain Processor#进程向注释处理器提供实现此接口的对象},使得处理器可以查询关于一轮注释处理的信息。
 * 
 * 
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @author Peter von der Ah&eacute;
 * @since 1.6
 */
public interface RoundEnvironment {
    /**
     * Returns {@code true} if types generated by this round will not
     * be subject to a subsequent round of annotation processing;
     * returns {@code false} otherwise.
     *
     * <p>
     *  如果此轮生成的类型不会经过后续轮次的注释处理,则返回{@code true};否则返回{@code false}。
     * 
     * 
     * @return {@code true} if types generated by this round will not
     * be subject to a subsequent round of annotation processing;
     * returns {@code false} otherwise
     */
    boolean processingOver();

    /**
     * Returns {@code true} if an error was raised in the prior round
     * of processing; returns {@code false} otherwise.
     *
     * <p>
     *  如果在前一轮处理中出现错误,则返回{@code true};否则返回{@code false}。
     * 
     * 
     * @return {@code true} if an error was raised in the prior round
     * of processing; returns {@code false} otherwise
     */
    boolean errorRaised();

    /**
     * Returns the root elements for annotation processing generated
     * by the prior round.
     *
     * <p>
     *  返回由前一轮生成的注记处理的根元素。
     * 
     * 
     * @return the root elements for annotation processing generated
     * by the prior round, or an empty set if there were none
     */
    Set<? extends Element> getRootElements();

    /**
     * Returns the elements annotated with the given annotation type.
     * The annotation may appear directly or be inherited.  Only
     * package elements and type elements <i>included</i> in this
     * round of annotation processing, or declarations of members,
     * constructors, parameters, or type parameters declared within
     * those, are returned.  Included type elements are {@linkplain
     * #getRootElements root types} and any member types nested within
     * them.  Elements in a package are not considered included simply
     * because a {@code package-info} file for that package was
     * created.
     *
     * <p>
     *  返回使用给定注记类型注释的元素。注释可以直接出现或继承。在此轮注释处理中只包含包含元素和类型元素<i> </i>,或返回成员,构造函数,参数或在其中声明的类型参数的声明。
     * 包含的类型元素是{@linkplain #getRootElements根类型}以及嵌套在其中的任何成员类型。包中的元素不会被认为包含,因为创建了该包的{@code package-info}文件。
     * 
     * 
     * @param a  annotation type being requested
     * @return the elements annotated with the given annotation type,
     * or an empty set if there are none
     * @throws IllegalArgumentException if the argument does not
     * represent an annotation type
     */
    Set<? extends Element> getElementsAnnotatedWith(TypeElement a);

    /**
     * Returns the elements annotated with the given annotation type.
     * The annotation may appear directly or be inherited.  Only
     * package elements and type elements <i>included</i> in this
     * round of annotation processing, or declarations of members,
     * constructors, parameters, or type parameters declared within
     * those, are returned.  Included type elements are {@linkplain
     * #getRootElements root types} and any member types nested within
     * them.  Elements in a package are not considered included simply
     * because a {@code package-info} file for that package was
     * created.
     *
     * <p>
     * 返回使用给定注记类型注释的元素。注释可以直接出现或继承。在此轮注释处理中只包含包含元素和类型元素<i> </i>,或返回成员,构造函数,参数或在其中声明的类型参数的声明。
     * 包含的类型元素是{@linkplain #getRootElements根类型}以及嵌套在其中的任何成员类型。包中的元素不会被认为包含,因为创建了该包的{@code package-info}文件。
     * 
     * @param a  annotation type being requested
     * @return the elements annotated with the given annotation type,
     * or an empty set if there are none
     * @throws IllegalArgumentException if the argument does not
     * represent an annotation type
     */
    Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a);
}
