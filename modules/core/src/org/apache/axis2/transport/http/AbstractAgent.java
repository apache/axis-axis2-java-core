package org.apache.axis2.transport.http;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jens Schumann - OpenKnowledge GmbH
 * @version $Id$
 */
public class AbstractAgent {
  private static final Log log = LogFactory.getLog(AbstractAgent.class);

  protected Map operationCache = new HashMap();
  protected ConfigurationContext configContext;

  public AbstractAgent(ConfigurationContext aConfigContext) {
    configContext = aConfigContext;
    preloadMethods();
  }


  public void handle(HttpServletRequest httpServletRequest,
                     HttpServletResponse httpServletResponse)
    throws IOException, Exception {


    String requestURI = httpServletRequest.getRequestURI();

    String operation;
    int i = requestURI.lastIndexOf('/');
    if (i < 0) {
      processUnknown(httpServletRequest, httpServletResponse);
      return;
    } else if (i == requestURI.length()-1) {
      processIndex(httpServletRequest, httpServletResponse);
      return;
    } else {
      operation = requestURI.substring(i + 1);
    }


    Method method = (Method) operationCache.get(operation.toLowerCase());
    if (method != null) {
      method.invoke(this, new Object[]{httpServletRequest, httpServletResponse});
    } else {
      processUnknown(httpServletRequest, httpServletResponse);
    }
  }

  protected void processIndex(HttpServletRequest httpServletRequest,
                              HttpServletResponse httpServletResponse) throws IOException, ServletException {
    renderView("index.jsp", httpServletRequest, httpServletResponse);
  }

  protected void processUnknown(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse) throws IOException, ServletException {
    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, httpServletRequest.getRequestURI());
  }


  protected void renderView(String jspName,
                            HttpServletRequest httpServletRequest,
                            HttpServletResponse httpServletResponse) throws IOException, ServletException {
    httpServletRequest.getRequestDispatcher("/axis2-web/" + jspName).include(httpServletRequest, httpServletResponse);

  }

  private void preloadMethods() {
    List allMethods = new ArrayList();
    allMethods.addAll(Arrays.asList(getClass().getDeclaredMethods()));
    Class clazz = getClass().getSuperclass();
    while (clazz != null && !clazz.equals(Object.class)) {
      allMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
      clazz = clazz.getSuperclass();
    }

    for (int i = 0; i < allMethods.size(); i++) {
      Method method = (Method) allMethods.get(i);

      Class[] parameterTypes = method.getParameterTypes();
      if (method.getName().startsWith("process") &&
        parameterTypes.length == 2 &&
        parameterTypes[0].equals(HttpServletRequest.class) &&
        parameterTypes[1].equals(HttpServletResponse.class)) {
        String key = method.getName().substring(7).toLowerCase();

        if (!operationCache.containsKey(key)) {
          operationCache.put(key, method);
        }
      }
    }
  }
}
