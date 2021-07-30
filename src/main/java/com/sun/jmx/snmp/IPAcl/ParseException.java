/***** Lobxxx Translate Finished ******/
/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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

/* Generated By:JavaCC: Do not edit this line. ParseException.java Version 0.7pre6 */
package com.sun.jmx.snmp.IPAcl;

/**
 * This exception is thrown when parse errors are encountered.
 * You can explicitly create objects of this exception type by
 * calling the method generateParseException in the generated
 * parser.
 *
 * You can modify this class to customize your error reporting
 * mechanisms so long as you retain the public fields.
 * <p>
 *  遇到解析错误时抛出此异常。您可以通过在生成的解析器中调用generateParseException方法显式创建此异常类型的对象。
 * 
 *  您可以修改此类以自定义错误报告机制,只要保留公共字段即可。
 * 
 */
class ParseException extends Exception {
  private static final long serialVersionUID = -3695190720704845876L;

  /**
   * This constructor is used by the method "generateParseException"
   * in the generated parser.  Calling this constructor generates
   * a new object of this type with the fields "currentToken",
   * "expectedTokenSequences", and "tokenImage" set.  The boolean
   * flag "specialConstructor" is also set to true to indicate that
   * this constructor was used to create this object.
   * This constructor calls its super class with the empty string
   * to force the "toString" method of parent class "Throwable" to
   * print the error message in the form:
   *     ParseException: <result of getMessage>
   * <p>
   *  此构造函数在生成的解析器中由方法"generateParseException"使用。
   * 调用此构造函数生成此类型的新对象,其中包含字段"currentToken","expectedTokenSequences"和"tokenImage"。
   * 布尔标志"specialConstructor"也设置为true,表示此构造函数用于创建此对象。
   * 这个构造函数使用空字符串调用其超类强制父类"Throwable"的"toString"方法以以下形式打印错误消息：ParseException：<result of getMessage>。
   * 
   */
  public ParseException(Token currentTokenVal,
                        int[][] expectedTokenSequencesVal,
                        String[] tokenImageVal
                       )
  {
    super("");
    specialConstructor = true;
    currentToken = currentTokenVal;
    expectedTokenSequences = expectedTokenSequencesVal;
    tokenImage = tokenImageVal;
  }

  /**
   * The following constructors are for use by you for whatever
   * purpose you can think of.  Constructing the exception in this
   * manner makes the exception behave in the normal way - i.e., as
   * documented in the class "Throwable".  The fields "errorToken",
   * "expectedTokenSequences", and "tokenImage" do not contain
   * relevant information.  The JavaCC generated code does not use
   * these constructors.
   * <p>
   *  以下构造函数供您使用,用于您可以想到的任何目的。以这种方式构造异常使得异常以正常方式运行 - 即,类"Throwable"中记录的异常。
   * 字段"errorToken","expectedTokenSequences"和"tokenImage"不包含相关信息。 JavaCC生成的代码不使用这些构造函数。
   * 
   */

  public ParseException() {
    super();
    specialConstructor = false;
  }

  public ParseException(String message) {
    super(message);
    specialConstructor = false;
  }

  /**
   * This variable determines which constructor was used to create
   * this object and thereby affects the semantics of the
   * "getMessage" method (see below).
   * <p>
   * 此变量确定哪个构造函数用于创建此对象,从而影响"getMessage"方法的语义(请参见下文)。
   * 
   */
  protected boolean specialConstructor;

  /**
   * This is the last token that has been consumed successfully.  If
   * this object has been created due to a parse error, the token
   * followng this token will (therefore) be the first error token.
   * <p>
   *  这是已成功使用的最后一个令牌。如果此对象由于解析错误而创建,则跟随此令牌的令牌将(因此)是第一个错误令牌。
   * 
   */
  public Token currentToken;

  /**
   * Each entry in this array is an array of integers.  Each array
   * of integers represents a sequence of tokens (by their ordinal
   * values) that is expected at this point of the parse.
   * <p>
   *  此数组中的每个条目都是一个整数数组。每个整数数组表示在解析的这一点上期望的一系列令牌(通过它们的序数值)。
   * 
   */
  public int[][] expectedTokenSequences;

  /**
   * This is a reference to the "tokenImage" array of the generated
   * parser within which the parse error occurred.  This array is
   * defined in the generated ...Constants interface.
   * <p>
   *  这是对生成的解析器的"tokenImage"数组的引用,其中发生解析错误。此数组在生成的...常量接口中定义。
   * 
   */
  public String[] tokenImage;

  /**
   * This method has the standard behavior when this object has been
   * created using the standard constructors.  Otherwise, it uses
   * "currentToken" and "expectedTokenSequences" to generate a parse
   * error message and returns it.  If this object has been created
   * due to a parse error, and you do not catch it (it gets thrown
   * from the parser), then this method is called during the printing
   * of the final stack trace, and hence the correct error message
   * gets displayed.
   * <p>
   *  当使用标准构造函数创建此对象时,此方法具有标准行为。否则,它使用"currentToken"和"expectedTokenSequences"来生成解析错误消息并返回。
   * 如果由于解析错误而创建了此对象,并且您没有捕获它(它从解析器抛出),则在打印最终堆栈跟踪期间调用此方法,因此显示正确的错误消息。
   * 
   */
  public String getMessage() {
    if (!specialConstructor) {
      return super.getMessage();
    }
    String expected = "";
    int maxSize = 0;
    for (int i = 0; i < expectedTokenSequences.length; i++) {
      if (maxSize < expectedTokenSequences[i].length) {
        maxSize = expectedTokenSequences[i].length;
      }
      for (int j = 0; j < expectedTokenSequences[i].length; j++) {
        expected += tokenImage[expectedTokenSequences[i][j]] + " ";
      }
      if (expectedTokenSequences[i][expectedTokenSequences[i].length - 1] != 0) {
        expected += "...";
      }
      expected += eol + "    ";
    }
    String retval = "Encountered \"";
    Token tok = currentToken.next;
    for (int i = 0; i < maxSize; i++) {
      if (i != 0) retval += " ";
      if (tok.kind == 0) {
        retval += tokenImage[0];
        break;
      }
      retval += add_escapes(tok.image);
      tok = tok.next;
    }
    retval += "\" at line " + currentToken.next.beginLine + ", column " + currentToken.next.beginColumn + "." + eol;
    if (expectedTokenSequences.length == 1) {
      retval += "Was expecting:" + eol + "    ";
    } else {
      retval += "Was expecting one of:" + eol + "    ";
    }
    retval += expected;
    return retval;
  }

  /**
   * The end of line string for this machine.
   * <p>
   *  此机器的行末字符串。
   * 
   */
  protected String eol = System.getProperty("line.separator", "\n");

  /**
   * Used to convert raw characters to their escaped version
   * when these raw version cannot be used as part of an ASCII
   * string literal.
   * <p>
   *  用于将原始字符转换为其转义版本,这些原始版本不能用作ASCII字符串文字的一部分。
   */
  protected String add_escapes(String str) {
      StringBuffer retval = new StringBuffer();
      char ch;
      for (int i = 0; i < str.length(); i++) {
        switch (str.charAt(i))
        {
           case 0 :
              continue;
           case '\b':
              retval.append("\\b");
              continue;
           case '\t':
              retval.append("\\t");
              continue;
           case '\n':
              retval.append("\\n");
              continue;
           case '\f':
              retval.append("\\f");
              continue;
           case '\r':
              retval.append("\\r");
              continue;
           case '\"':
              retval.append("\\\"");
              continue;
           case '\'':
              retval.append("\\\'");
              continue;
           case '\\':
              retval.append("\\\\");
              continue;
           default:
              if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
                 String s = "0000" + Integer.toString(ch, 16);
                 retval.append("\\u" + s.substring(s.length() - 4, s.length()));
              } else {
                 retval.append(ch);
              }
              continue;
        }
      }
      return retval.toString();
   }

}