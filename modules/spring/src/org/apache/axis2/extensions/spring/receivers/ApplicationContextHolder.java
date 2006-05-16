package org.apache.axis2.extensions.spring.receivers;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/** Implementation of a Spring interface who is configured in Spring's 
 *  applicationContext.xml or some other Spring type of way. The 
 *  ApplicationContext value is injected on Spring startup, and therefore, 
 *  this class must be present at least in WEB-INF/lib. 
 *  This ApplicationContext object has all the Spring beans which can be accessed
 *  by bean name, ie, appCtx.getBean(beanName) . This is the main glue between axis2
 *  and Spring.
 */
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext appCtx;

    public ApplicationContextHolder() {}

    /** Spring supplied interface method for injecting app context. */
    public void setApplicationContext(ApplicationContext applicationContext) 
        throws BeansException {
        appCtx = applicationContext;
    }

    /** Access to spring wired beans. */    
    public static ApplicationContext getContext() {
        return appCtx;
    }

}
