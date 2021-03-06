package com.github.bot.curiosone.api;

import spark.Request;
import spark.Response;

/**
 * Converts Spark Requests and Responses into Audit logs.
 * Provides a public static method to format a Spark request-response interaction.
 * @see  <a href="https://goo.gl/T8LFRm">Spark Request Javadoc</a>
 * @see  <a href="https://goo.gl/nFCekX">Spark Response Javadoc</a>
 */
public class AuditUtil {

  /**
  * Default Constructor.
  */
  private AuditUtil() {}

  /**
   * Converts a Spark request-response interaction into an Audit log record.
   * @param  req
   *         The request object.
   * @param  res
   *         The response object.
   * @return  a String containing the audit log.
   * @see  <a href="https://goo.gl/T8LFRm">Spark Request Javadoc</a>
   * @see  <a href="https://goo.gl/nFCekX">Spark Response Javadoc</a>
   */
  public static String format(Request req, Response res) {
    StringBuilder sb = new StringBuilder();
    sb.append(req.requestMethod());
    sb.append(" " + req.url());
    sb.append(" " + req.body());
    return sb.toString();
  }
}
