package org.apache.axis2.extensions.osgi.util;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.Servlet;
import javax.servlet.ServletException;


public class HttpServiceTracker extends ServiceTracker {
    int count;
    Servlet servlet;
    Logger logger;

    public HttpServiceTracker(BundleContext context, Servlet servlet,
        Logger logger) {
        super(context, HttpService.class.getName(), null);
        this.servlet = servlet;
        this.logger = logger;
        open();
    }

    public Object addingService(ServiceReference reference) {
        try {
            HttpService http = (HttpService) super.addingService(reference);
            installServlet(http);
            return http;
        } catch (Exception nse) {
            logger.log(LogService.LOG_ERROR,"Unable to install servlet: ", nse);
        }

        return null;
    }

    private void installServlet(HttpService http) throws ServletException, NamespaceException {
        http.registerServlet("/axis2", servlet, null, null);
        logger.log(LogService.LOG_INFO,"Installed Axis2 Servlet in http service " + http);
    }
}
