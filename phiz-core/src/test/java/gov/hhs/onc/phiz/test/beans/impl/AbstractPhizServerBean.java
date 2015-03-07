package gov.hhs.onc.phiz.test.beans.impl;

import gov.hhs.onc.phiz.test.beans.PhizServerBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;

public abstract class AbstractPhizServerBean implements PhizServerBean {
    protected int phase;

    @Override
    public void stop(Runnable stopCallback) {
        this.stop();

        stopCallback.run();
    }

    @Override
    public void stop() {
        if (this.isRunning()) {
            try {
                this.stopInternal();
            } catch (BeansException e) {
                throw e;
            } catch (Exception e) {
                throw new FatalBeanException(String.format("Unable to stop server (host=%s, port=%d).", this.getHost(), this.getPort()), e);
            }
        }
    }

    @Override
    public void start() {
        if (!this.isRunning()) {
            try {
                this.startInternal();
            } catch (BeansException e) {
                throw e;
            } catch (Exception e) {
                throw new FatalBeanException(String.format("Unable to start server (host=%s, port=%d).", this.getHost(), this.getPort()), e);
            }
        }
    }

    protected abstract void stopInternal() throws Exception;

    protected abstract void startInternal() throws Exception;

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return this.phase;
    }

    @Override
    public void setPhase(int phase) {
        this.phase = phase;
    }
}
