package gov.hhs.onc.phiz.web.listener.impl;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

public abstract class AbstractPhizServletRequestListener implements ServletRequestListener {
    @Override
    public void requestDestroyed(ServletRequestEvent servletReqEvent) {
    }

    @Override
    public void requestInitialized(ServletRequestEvent servletReqEvent) {
    }
}
