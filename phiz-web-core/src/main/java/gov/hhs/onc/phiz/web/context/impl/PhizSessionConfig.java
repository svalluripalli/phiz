package gov.hhs.onc.phiz.web.context.impl;

import java.util.EnumSet;
import java.util.Set;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;

public class PhizSessionConfig implements SessionCookieConfig {
    private int cacheSize;
    private String comment;
    private String domain;
    private boolean httpOnly;
    private int maxAge = -1;
    private String name;
    private String path;
    private boolean secure;
    private int timeout;
    private Set<SessionTrackingMode> trackingModes = EnumSet.noneOf(SessionTrackingMode.class);

    public int getCacheSize() {
        return this.cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getDomain() {
        return this.domain;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public boolean isHttpOnly() {
        return this.httpOnly;
    }

    @Override
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    @Override
    public int getMaxAge() {
        return this.maxAge;
    }

    @Override
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean isSecure() {
        return this.secure;
    }

    @Override
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Set<SessionTrackingMode> getTrackingModes() {
        return this.trackingModes;
    }

    public void setTrackingModes(Set<SessionTrackingMode> trackingModes) {
        this.trackingModes = trackingModes;
    }
}
