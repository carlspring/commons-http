package org.carlspring.commons.http.rest.app;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpCommonsApplication extends ResourceConfig
{

    private static final Logger logger = LoggerFactory.getLogger(HttpCommonsApplication.class);


    public HttpCommonsApplication()
    {
        if (logger.isDebugEnabled())
        {
            register(new LoggingFilter());
        }
    }

}
