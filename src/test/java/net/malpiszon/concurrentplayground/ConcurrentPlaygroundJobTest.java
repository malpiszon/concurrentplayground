package net.malpiszon.concurrentplayground;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ConcurrentPlaygroundJobTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TestListener testListener;

    @Before
    public void setUp() throws Exception {
        testListener = new TestListener();
    }

    @Test
    public void textExecute_withEmptyJobsList_returnsSuccess() {
        ConcurrentPlaygroundJob sut = new ConcurrentPlaygroundJob(new ArrayList<>(), 1, testListener);

        sut.execute();

        Assert.assertNull("Exception thrown", testListener.getE());
    }

    @Test
    public void textExecute_withCompletedJobsList_returnsSuccess() {
        ConcurrentPlaygroundJob sut = new ConcurrentPlaygroundJob(Arrays.asList(new TestJob(), new TestJob(), new TestJob()), 2, testListener);

        sut.execute();

        Assert.assertNull("Exception thrown", testListener.getE());
    }

    @Test
    public void textExecute_withCompletedJobsListAndsOneInvalidJob_returnsException() {
        ConcurrentPlaygroundJob sut = new ConcurrentPlaygroundJob(Arrays.asList(new TestJob(), new InvalidTestJob(), new TestJob(), new TestJob()), 2, testListener);

        sut.execute();

        Assert.assertNotNull("Exception not thrown", testListener.getE());
    }

    @Test
    public void textExecute_withNullJobsList_returnsException() {
        thrown.expect(IllegalArgumentException.class);
        ConcurrentPlaygroundJob sut = new ConcurrentPlaygroundJob(null, 1, testListener);

        sut.execute();
    }

    @Test
    public void textExecute_withInvalidNumberOfJobs_returnsException() {
        thrown.expect(IllegalArgumentException.class);
        ConcurrentPlaygroundJob sut = new ConcurrentPlaygroundJob(new ArrayList<>(), 0, testListener);

        sut.execute();
    }

    @Test
    public void textExecute_withNullListener_returnsException() {
        thrown.expect(IllegalArgumentException.class);
        ConcurrentPlaygroundJob sut = new ConcurrentPlaygroundJob(new ArrayList<>(), 1, null);

        sut.execute();
    }

    private class TestListener implements IJobListener {
        private Throwable e;

        @Override
        public void exceptionOccured(Throwable e) {
            this.e = e;
        }

        public Throwable getE() {
            return e;
        }
    }

    private class TestJob implements IJob {
        @Override
        public void execute() {
        }
    }

    private class InvalidTestJob implements IJob {
        @Override
        public void execute() {
            int result = 2 / 0;
        }
    }
}