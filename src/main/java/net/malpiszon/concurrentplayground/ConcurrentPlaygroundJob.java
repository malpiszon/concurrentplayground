package net.malpiszon.concurrentplayground;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Job which runs list of IJobs in parallel. Uses observer pattern to pass the exception.
 */
public class ConcurrentPlaygroundJob implements IJob {

    private final List<Callable<Void>> jobsToExecute = new ArrayList<>();
    private ExecutorService executorService;
    private IJobListener listener;


    /**
     * @param jobsToExecute list of IJobs to execute
     * @param numberOfJobs number of jobs to run in parallel
     * @param listener observer to be informed when exception occurs
     */
    public ConcurrentPlaygroundJob(List<IJob> jobsToExecute, int numberOfJobs, IJobListener listener) {
        if (jobsToExecute == null || numberOfJobs < 1 || listener == null) {
            throw new IllegalArgumentException();
        }

        jobsToExecute.forEach(
            job -> this.jobsToExecute.add(new RunnableJob(job))
        );
        this.executorService = new ExceptionReportingThreadPoolExecutor(numberOfJobs, this::exceptionOccurred);
        this.listener = listener;
    }

    @Override
    public void execute() {
        try {
            executorService.invokeAll(jobsToExecute);
        } catch (InterruptedException e) {
            exceptionOccurred(e);
        }
        executorService.shutdown();
    }

    private synchronized void exceptionOccurred(Throwable e) {
        listener.exceptionOccured(e);
        executorService.shutdownNow();
    }

    /**
     * Wraps IJob into Callable
     */
    private class RunnableJob implements Callable {
        private final IJob job;

        /**
         * Job to be run
         * @param job job to be run
         */
        RunnableJob(IJob job) {
            this.job = job;
        }

        @Override
        public Object call() throws Exception {
            job.execute();
            return null;
        }
    }

    /**
     * ThreadPoolExecuror passing the information about occurred exception
     * @see ThreadPoolExecutor
     */
    private class ExceptionReportingThreadPoolExecutor extends ThreadPoolExecutor {
        private final Consumer<Throwable> exceptionListener;

        /**
         * @param corePoolSize the number of threads to keep in the pool
         * @param exceptionListener observer to be informed when exception occurs
         */
        ExceptionReportingThreadPoolExecutor(int corePoolSize, Consumer<Throwable> exceptionListener) {
            super(corePoolSize, corePoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
            this.exceptionListener = exceptionListener;
        }

        @Override
        protected void afterExecute(Runnable task, Throwable thrown) {
            super.afterExecute(task, thrown);

            if (thrown != null) {
                exceptionListener.accept(thrown);
            }

            if (task instanceof Future<?>) {
                try {
                    ((Future<?>) task).get();
                } catch (Exception e) {
                    exceptionListener.accept(e);
                }
            }
        }
    }
}