package me.molybdenum.ambience_mini.engine.shared.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class JobCenter {
    private final ScheduledExecutorService executor;
    private final ArrayList<Job> jobs = new ArrayList<>();


    private JobCenter(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    public static JobCenter singleThreaded() {
        return new JobCenter(Executors.newSingleThreadScheduledExecutor());
    }

    public static JobCenter fixed(int poolSize) {
        return new JobCenter(Executors.newScheduledThreadPool(poolSize));
    }

    public static JobCenter variable(int maxPoolSize) {
        var scheduledExecutor = new ScheduledThreadPoolExecutor(maxPoolSize);
        scheduledExecutor.setKeepAliveTime(10, TimeUnit.SECONDS);
        scheduledExecutor.allowCoreThreadTimeOut(true);
        return new JobCenter(scheduledExecutor);
    }


    public synchronized List<Job> getJobs() {
        return new ArrayList<>(jobs);
    }


    public synchronized <T extends Job> T post(T job) {
        jobs.add(job);
        ((Job)job).bind(this, executor.submit(() -> fireOnce(job)));
        return job;
    }

    public synchronized <T extends Job> T post(T job, long delayMillis) {
        jobs.add(job);
        ((Job)job).bind(this, executor.schedule(() -> fireOnce(job), delayMillis, TimeUnit.MILLISECONDS));
        return job;
    }

    public synchronized <T extends Job> T schedule(T job, long delayMillis, long periodMillis) {
        jobs.add(job);
        ((Job)job).bind(this, executor.scheduleAtFixedRate(() -> fireRepeated(job), delayMillis, periodMillis, TimeUnit.MILLISECONDS));
        return job;
    }

    public synchronized void cancelAll(boolean mayInterruptIfRunning) {
        for (var job : new ArrayList<>(jobs)) // Clone list to avoid concurrent modification exception on cancel
            job.cancel(mayInterruptIfRunning);
    }

    public synchronized void shutdown() {
        cancelAll(true);
        executor.shutdownNow();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public synchronized boolean isShutdown() {
        return executor.isShutdown();
    }

    public boolean shutdownAndAwaitTermination(long millis) {
        try {
            shutdown();
            return executor.awaitTermination(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
            return false;
        }
    }


    private void fireOnce(Job job) {
        try {
            job.body();
        } catch (Exception e) {
            job.onException(e);
        } finally {
            removeJob(job);
        }
    }

    private void fireRepeated(Job job) {
        try {
            job.body();
        } catch (Exception e) {
            job.onException(e);
            removeJob(job); // Only remove recurring jobs when canceled or on exception.
        }
    }

    private synchronized void removeJob(Job job) {
        jobs.remove(job);
    }


    public abstract static class Job {
        private JobCenter jobCenter;
        private Future<?> future;
        private boolean cancelled;


        private synchronized void bind(JobCenter jobCenter, Future<?> future) {
            if (this.future != null)
                throw new RuntimeException("This job is already bound!");
            this.jobCenter = jobCenter;
            this.future = future;
        }


        protected abstract void body();

        protected void onException(Exception e) { }


        public synchronized void cancel(boolean mayInterruptIfRunning) {
            if (!cancelled) {
                jobCenter.removeJob(this);
                future.cancel(mayInterruptIfRunning);
                cancelled = true;
            }
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean isCancelled() {
            return cancelled;
        }


        public static Job of(Runnable body) {
            return new Job() {
                @Override
                protected void body() {
                    body.run();
                }
            };
        }
    }
}
