package gov.hhs.onc.phiz.web.ws.workqueue.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metric;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnegative;
import org.apache.cxf.workqueue.AutomaticWorkQueueImpl;
import org.springframework.beans.factory.DisposableBean;

public class PhizWorkQueue extends AutomaticWorkQueueImpl implements DisposableBean {
    private class PhizWorkQueueTask implements Runnable {
        private Runnable task;

        public PhizWorkQueueTask(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            PhizWorkQueue.this.submittedTasksMeter.mark();
            PhizWorkQueue.this.runningTasksCounter.inc();

            Context taskElapsedTimeContext = PhizWorkQueue.this.tasksElapsedTimeTimer.time();

            try {
                this.task.run();
            } catch (RejectedExecutionException e) {
                PhizWorkQueue.this.rejectedTasksMeter.mark();

                throw e;
            } finally {
                taskElapsedTimeContext.stop();

                PhizWorkQueue.this.runningTasksCounter.dec();
                PhizWorkQueue.this.completedTasksMeter.mark();
            }
        }
    }

    @Metric(name = "ws.workqueue.tasks.submitted", absolute = true)
    private Meter submittedTasksMeter = new Meter();

    @Metric(name = "ws.workqueue.tasks.rejected", absolute = true)
    private Meter rejectedTasksMeter = new Meter();

    @Metric(name = "ws.workqueue.tasks.completed", absolute = true)
    private Meter completedTasksMeter = new Meter();

    @Metric(name = "ws.workqueue.tasks.running", absolute = true)
    private Counter runningTasksCounter = new Counter();

    @Metric(name = "ws.workqueue.tasks.elapsed.time", absolute = true)
    private Timer tasksElapsedTimeTimer = new Timer();

    public PhizWorkQueue(@Nonnegative int corePoolSize, @Nonnegative int maxPoolSize, @Nonnegative int maxQueueSize, @Nonnegative long keepAliveTime) {
        super(maxQueueSize, 0, maxPoolSize, corePoolSize, keepAliveTime);
    }

    @Override
    public void execute(Runnable task, long timeout) {
        super.execute(new PhizWorkQueueTask(task), timeout);
    }

    @Override
    public void execute(Runnable task) {
        super.execute(((task instanceof PhizWorkQueueTask) ? task : new PhizWorkQueueTask(task)));
    }

    @Override
    public void destroy() throws Exception {
        this.shutdown(false);
    }

    @Override
    protected synchronized ThreadPoolExecutor getExecutor() {
        ThreadPoolExecutor executor = super.getExecutor();
        executor.allowCoreThreadTimeOut(true);

        return executor;
    }

    @Gauge(name = "ws.workqueue.pool.size.core", absolute = true)
    @Nonnegative
    public int getCorePoolSize() {
        return this.getExecutor().getCorePoolSize();
    }

    @Gauge(name = "ws.workqueue.keep.alive.time", absolute = true)
    @Nonnegative
    public long getKeepAliveTime() {
        return this.getExecutor().getKeepAliveTime(TimeUnit.MILLISECONDS);
    }

    @Gauge(name = "ws.workqueue.pool.size.max", absolute = true)
    @Nonnegative
    public int getMaxPoolSize() {
        return this.getExecutor().getMaximumPoolSize();
    }

    @Gauge(name = "ws.workqueue.queue.size.max", absolute = true)
    @Nonnegative
    public int getMaxQueueSize() {
        BlockingQueue<Runnable> queue = this.getExecutor().getQueue();

        return (queue.size() + queue.remainingCapacity());
    }

    @Gauge(name = "ws.workqueue.pool.size", absolute = true)
    @Nonnegative
    @Override
    public int getPoolSize() {
        return this.getExecutor().getPoolSize();
    }

    @Gauge(name = "ws.workqueue.queue.size", absolute = true)
    @Nonnegative
    public int getQueueSize() {
        return this.getExecutor().getQueue().size();
    }
}
