package usb;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadPoolManager
 *
 * @author yx
 * @date 2019/5/17 13:57
 */
public class ThreadPoolManager {

    private final static String TAG = "ThreadPoolManager";

    private static final String THREAD_NAME_COMMON = "threadPool-common";

    private static final String THREAD_NAME_SCHEDULE = "threadPool-schedule";
    /**
     * ��ͨ�̳߳�
     */
    private ThreadPoolExecutor mPoolExecutor;

    /**
     * Scheduled�̳߳�
     */
    private ScheduledThreadPoolExecutor mScheduledPoolExecutor;

    /**
     * ���Կ��������������ڵ��������
     */
    private Map<String, List<WeakReference<Future<?>>>> mTaskMap;

    /**
     * ����
     */
    private static ThreadPoolManager instance = null;

    /**
     * ��ͨ�̳߳غ����̳߳ص�������ͬʱ�ܹ�ִ�е��߳�����
     */
    private int mCommonCorePoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;

    /**
     * �����̳߳ص�������ͬʱ�ܹ�ִ�е��߳�����
     */
    private int mScheduleCorePoolSize = 10;
    /**
     * ����̳߳�����
     */
    private int mMaximumPoolSize = mCommonCorePoolSize + 10;

    /**
     * ���ʱ��,���ݸ����߳�����ִ�г�ʱʱ������ �������������� ��ʱ��
     */
    private long mKeepAliveTime = 30;
    /**
     * TimeUnit
     */
    private TimeUnit unit = TimeUnit.SECONDS;

    private final int MAX_QUEUE_LENGTH = 200;

    /**
     * ����һ���µ�ʵ�� ThreadPoolManager
     */
    private ThreadPoolManager() {
        mPoolExecutor = new ThreadPoolExecutor(
                mCommonCorePoolSize,
                mMaximumPoolSize,
                mKeepAliveTime,
                unit,
                //������У����ڴ�ŵȴ�����Linked���Ƚ��ȳ�
                new LinkedBlockingQueue<Runnable>(MAX_QUEUE_LENGTH),
                //�����̵߳Ĺ���
                new ThreadFactory() {
                    private final AtomicInteger mCount = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, THREAD_NAME_COMMON + "#" + mCount.getAndIncrement());
                    }
                }
        );
        // �����Գ���maximumPoolSize������Ĵ������
        mPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                System.out.println("rejectedExecution happaned ");
                try {
                    super.rejectedExecution(r, e);
                } catch (RejectedExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        });

        mScheduledPoolExecutor =
                new ScheduledThreadPoolExecutor(mScheduleCorePoolSize, new ThreadFactory() {
                    private final AtomicInteger mCount = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, THREAD_NAME_SCHEDULE + "#" + mCount.getAndIncrement());
                    }
                });
        mScheduledPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                System.out.println("rejectedExecution happaned ");
                try {
                    super.rejectedExecution(r, e);
                } catch (RejectedExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        });
        mTaskMap = new WeakHashMap<String, List<WeakReference<Future<?>>>>();
    }

    /**
     * ��ȡThreadPoolManager��������
     */
    public static ThreadPoolManager getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolManager.class) {
                if (instance == null) {
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }

    /**
     * �ͷ�MinaThreadPoolManager���߳���Դ
     * ��Ҫ�����˳�Ӧ�ý���ǰ�������̳߳�
     */
    public void release() {
        synchronized (ThreadPoolManager.class) {
            if (instance != null) {
                instance.cancelAllTaskThreads();
            }
            mPoolExecutor.shutdownNow();
            mScheduledPoolExecutor.shutdownNow();
            instance = null;
        }
    }

    /**
     * �����߳�
     * ����߳��������̳߳����Ч��startTaskThread
     *
     * @param task ���������߳�
     */
    public void restartTaskThread(Thread task) {
        if (task != null) {
            stopTaskThread(task);
            startTaskThread(task);
        }
    }

    /**
     * �����߳�
     *
     * @param task ����
     * @param name ��������
     */
    public void restartTaskThread(FutureTask task, String name) {
        if (task != null && name != null) {
            stopTaskThread(name);
            startTaskThread(task, name);
        }
    }

    /**
     * �����߳�
     *
     * @param task ����
     * @param name ��������
     */
    public void restartTaskThread(Runnable task, String name) {
        if (task != null && name != null) {
            stopTaskThread(name);
            startTaskThread(task, name);
        }
    }

    /**
     * �����߳�
     *
     * @param task �����߳�
     */
    public void startTaskThread(Thread task) {
        Future<?> request = mPoolExecutor.submit(task);
        String taskName = task.getName();
        addTask(request, taskName);
        System.out.println("add Thread name = " + taskName);
        printPoolExecutorInfo();
    }

    /**
     * �����߳�
     *
     * @param task �����߳�
     * @param name ��������
     */
    public void startTaskThread(FutureTask task, String name) {
        if (task != null && name != null) {
            Future<?> request = mPoolExecutor.submit(task);
            addTask(request, name);
            System.out.println("add FutureTask name = " + name);
            printPoolExecutorInfo();
        }
    }

    /**
     * �����߳�
     *
     * @param task �����߳�
     * @param name ��������
     */
    public void startTaskThread(Runnable task, String name) {
        if (task != null && name != null) {
            Future<?> request = mPoolExecutor.submit(task);
            addTask(request, name);
            System.out.println("add Runnable name = " + name);
            printPoolExecutorInfo();
        }
    }

    /**
     * �����߳�
     *
     * @param task �����߳�
     */
    public void executeTaskThread(Thread task) {
        String taskName = task.getName();
        System.out.println("executeTaskThread task name = " + taskName);
        printPoolExecutorInfo();
        mPoolExecutor.execute(task);
    }

    /**
     * �����߳�
     *
     * @param task �����߳�
     */
    public void stopTaskThread(Thread task) {
        stopTaskThread(task.getName());
    }

    /**
     * �����߳�
     *
     * @param taskTag �����߳�
     */
    public void stopTaskThread(String taskTag) {
        cancelTaskThreads(taskTag);
    }

    /**
     * ���ִ�����񵽶�����
     *
     * @param request
     */
    private void addTask(Future<?> request, String taskTag) {
        synchronized (ThreadPoolManager.class) {
            if (taskTag != null) {
                List<WeakReference<Future<?>>> requestList = mTaskMap.get(taskTag);
                if (requestList == null) {
                    requestList = new LinkedList<WeakReference<Future<?>>>();
                    mTaskMap.put(taskTag, requestList);
                }
                requestList.add(new WeakReference<Future<?>>(request));
            }
        }
    }

    /**
     * ȡ�����е�����
     */
    public void cancelAllTaskThreads() {
        for (String clsName : mTaskMap.keySet()) {
            List<WeakReference<Future<?>>> requestList = mTaskMap.get(clsName);
            if (requestList != null) {
                Iterator<WeakReference<Future<?>>> iterator = requestList.iterator();
                while (iterator.hasNext()) {
                    Future<?> request = iterator.next().get();
                    if (request != null) {
                        request.cancel(true);
                    }
                }
            }
        }
        mTaskMap.clear();
    }

    /**
     * �����ض���������ȡ������
     */
    private void cancelTaskThreads(String taskName) {
        System.out.println("cancelTaskThreads task name = " + taskName);
        List<WeakReference<Future<?>>> requestList = mTaskMap.get(taskName);
        if (requestList != null) {
            Iterator<WeakReference<Future<?>>> iterator = requestList.iterator();
            while (iterator.hasNext()) {
                Future<?> request = iterator.next().get();
                if (request != null) {
                    request.cancel(true);
                }
            }
            mTaskMap.remove(taskName);
        }
        printPoolExecutorInfo();
    }

    public ThreadPoolExecutor getPoolExecutor() {
        return mPoolExecutor;
    }

    private void printPoolExecutorInfo() {
        if (mPoolExecutor != null) {
            System.out.println("mPoolExecutor info:[poolSize:" + mPoolExecutor.getPoolSize()
                    + "��activeCount:" + mPoolExecutor.getActiveCount()
                    + "��taskQueueCount:" + mPoolExecutor.getQueue().size()
                    + "��completeTaskCount��" + mPoolExecutor.getCompletedTaskCount() + "]");
        }
        if (mScheduledPoolExecutor != null) {
            System.out.println(
                    "mScheduledPoolExecutor info:[poolSize:" + mScheduledPoolExecutor.getPoolSize()
                            + "��activeCount:" + mScheduledPoolExecutor.getActiveCount()
                            + "��taskQueueCount:" + mScheduledPoolExecutor.getQueue().size()
                            + "��completeTaskCount��" +
                            mScheduledPoolExecutor.getCompletedTaskCount() + "]");
        }
    }

    /**
     * ִ���ڸ����ӳٺ����õ�һ���Բ���
     *
     * @param task  Ҫִ�е�����
     * @param delay �����ڿ�ʼ�ӳ�ִ�е�ʱ��
     * @param unit  �ӳٲ�����ʱ�䵥λ
     */
    public void scheduleTaskThread(Thread task, long delay, TimeUnit unit) {
        Future<?> request = mScheduledPoolExecutor.schedule(task, delay, unit);
        addTask(request, task.getName());
    }

    /**
     * ִ���ڸ����ӳٺ����õ�һ���Բ���
     *
     * @param task     Ҫִ�е�����
     * @param taskName ��������
     * @param delay    �����ڿ�ʼ�ӳ�ִ�е�ʱ��
     * @param unit     �ӳٲ�����ʱ�䵥λ
     */
    public void scheduleTaskThread(Runnable task, String taskName, long delay,
            TimeUnit unit) {
        if (task != null && taskName != null) {
            Future<?> request = mScheduledPoolExecutor.schedule(task, delay, unit);
            addTask(request, taskName);
        }
    }

    /**
     * ������ִ������
     *
     * @param task         Ҫִ�е�����
     * @param initialDelay �����ڿ�ʼ�ӳ�ִ�е�ʱ��
     * @param period       ִ�м��
     * @param unit         �ӳٲ�����ʱ�䵥λ
     */
    public void scheduleAtFixedRate(Thread task, long initialDelay, long period, TimeUnit unit) {
        Future<?> request =
                mScheduledPoolExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
        addTask(request, task.getName());
    }

    /**
     * ������ִ������
     *
     * @param task         Ҫִ�е�����
     * @param taskName     ��������
     * @param initialDelay �����ڿ�ʼ�ӳ�ִ�е�ʱ��
     * @param period       ִ�м��
     * @param unit         �ӳٲ�����ʱ�䵥λ
     */
    public void scheduleAtFixedRate(Runnable task, String taskName, long initialDelay,
            long period,
            TimeUnit unit) {
        if (task != null && taskName != null) {
            Future<?> request =
                    mScheduledPoolExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
            addTask(request, taskName);
        }
    }

    /**
     * ����������Ƿ���
     *
     * @param task ����
     * @return �Ƿ��б�ǩ���� true-��
     */
    public boolean hasTask(Thread task) {
        if (task == null) {
            return false;
        }
        return hasTask(task.getName());
    }

    /**
     * ����������Ƿ���
     *
     * @param taskTag �����ǩ
     * @return �Ƿ��б�ǩ���� true-��
     */
    public boolean hasTask(String taskTag) {
        if (taskTag == null) {
            return false;
        }
        List<WeakReference<Future<?>>> requestList = mTaskMap.get(taskTag);
        if (requestList != null) {
            Iterator<WeakReference<Future<?>>> iterator = requestList.iterator();
            while (iterator.hasNext()) {
                Future<?> request = iterator.next().get();
                if (request != null) {
                    if (request.isCancelled()) {
                        System.out.println("taskTag: " + taskTag + " has canceled ");
                        continue;
                    }
                    if (!request.isDone()) {
                        System.out.println("hasTask " + taskTag);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
