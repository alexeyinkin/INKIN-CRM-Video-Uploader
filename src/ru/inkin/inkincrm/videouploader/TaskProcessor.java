package ru.inkin.inkincrm.videouploader;

import java.lang.reflect.Constructor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class TaskProcessor
        <
            InTask          extends TaskGeneric,
            NextProcessor   extends TaskProcessor,
            NextTask        extends TaskGeneric
        >
{
    private final BlockingQueue<InTask> tasks = new LinkedBlockingQueue<>();
    private Thread                      thread;
    private NextProcessor   nextProcessor;

    public static final boolean LAST        = true;
    public static final boolean NOT_LAST    = false;

    protected void processTaskIfShould(InTask task)
    {
        if (task.isEmpty())
        {
            relayNoMoreSubTasks(task);
        }
        else
        {
            processTask(task);
        }
    }

    protected void processTask(InTask task)
    {
        task.process();
    }

    public void start()
    {
        new Thread(new TaskProcessorRunnable()).start();
    }

    public void addTask(InTask task) //, boolean isLastInItsParent)
    {
        while (true)
        {
            try
            {
                task.setProcessor(this);
                tasks.put(task);
                return;
            }
            catch (InterruptedException e)
            {
                //  Continue. Normally this should never happen.
            }
        }
    }

    public NextTask createNextTask(InTask task) //, Class<NextTask> cls)
    {
        try
        {
//            Constructor<NextTask> ctor = cls.getConstructor();
//            NextTask nextTask = ctor.newInstance();
            NextTask nextTask = createNextTaskObject();
            nextTask.setOrigin(task);
            return nextTask;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    protected abstract NextTask createNextTaskObject();

    public void setNextProcessor(NextProcessor nextProcessor)
    {
        this.nextProcessor = nextProcessor;
    }

    public NextProcessor getNextProcessor()
    {
        return nextProcessor;
    }

    public void sendTaskToNext(NextTask nextTask) //, boolean isLastInItsParent)
    {
        nextProcessor.addTask(nextTask); //, isLastInItsParent);
    }

    /**
     * Called to relay 'no-mor-sub-tasks' marker received from the
     * previous processor.
     * @param task 
     */
    protected void relayNoMoreSubTasks(InTask task)
    {
        NextTask empty = createNextTask(task);
        empty.setEmpty(true);
        sendTaskToNext(empty);
    }

    private class TaskProcessorRunnable implements Runnable
    {
        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    InTask task = tasks.take();
                    processTaskIfShould(task);
                }
                catch (InterruptedException e)
                {
                    //  No more tasks. Exit.
                    break;
                }
            }
        }
    }
}
