<%@ page import="org.apache.axis2.Constants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.axis2.description.Parameter" %>
<%@ page import="org.apache.axis2.transport.http.AxisServlet" %>
<%@ page import="javax.servlet.ServletContext" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%!
  private String frontendHostUrl;

  public void jspInit() {
    ServletContext context = this.getServletConfig().getServletContext();
    ConfigurationContext configctx = (ConfigurationContext) context.getAttribute(AxisServlet.CONFIGURATION_CONTEXT);
    if (configctx != null){
        Parameter parameter = configctx.getAxisConfiguration().getParameter(Constants.HTTP_FRONTEND_HOST_URL);
        if (parameter != null) {
          frontendHostUrl = (String) parameter.getValue();
        }
    }
  }

  public String calculateHttpBase(HttpServletRequest aRequest) {
    StringBuffer stringBuffer = new StringBuffer();
    if (frontendHostUrl != null) {
      stringBuffer.append(frontendHostUrl);
    } else {
      String scheme = aRequest.getScheme();
      stringBuffer.append(scheme);
      stringBuffer.append("://");
      stringBuffer.append(aRequest.getServerName());
      if (("http".equalsIgnoreCase(scheme) && aRequest.getServerPort() != 80) || "https".equalsIgnoreCase(scheme) && aRequest.getServerPort() != 443)
      {
        stringBuffer.append(":");
        stringBuffer.append(aRequest.getServerPort());
      }
      // I think i saw web containers return null for root web context
      if (aRequest.getContextPath() != null) {
        stringBuffer.append(aRequest.getContextPath());
      }
    }
    // append / char if needed
    if (stringBuffer.charAt(stringBuffer.length() - 1) != '/') {
      stringBuffer.append("/");
    }
    String curentUrl = stringBuffer.toString();
    aRequest.setAttribute("frontendHostUrl", curentUrl);
    return curentUrl;
  }
%><base href="<%= calculateHttpBase(request)%>"/>