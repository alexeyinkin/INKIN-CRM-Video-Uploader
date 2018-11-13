package ru.inkin.inkincrm.videouploader;

/**
 *
 * @author Alexey
 */
public abstract class JoiningTaskProcessor
        <
            InTask          extends JoiningTaskGeneric,
            NextProcessor   extends TaskProcessor,
            NextTask        extends TaskGeneric
        >
        extends TaskProcessor<InTask, NextProcessor, NextTask>
{
    @Override
    public void addTask(InTask task) //, boolean isLastInItsParent)
    {
        //System.out.println(task.getClass().getSimpleName() + ", empty = " + (task.isEmpty() ? "true" : "false"));
        System.out.println(task.getDebugStringWithTrace());
        task.getTrace().removeLevelsTillSplitting();
        super.addTask(task); //, isLastInItsParent);
    }

    @Override
    public void sendTaskToNext(NextTask task)//, boolean isLastInItsParent)
    {
        //NextProcessor nextProcessor = getNextProcessor();
        //task.setProcessor(nextProcessor);

        task.getTrace().removeLastSplitting();

        super.sendTaskToNext(task); //, isLastInItsParent);
    }

    @Override
    public void processTask(InTask task)
    {
        if (!task.isEmpty())
        {
            //  A sub-task has completed. The joiner is not complete yet.
            task.process();
        }
        else
        {
            //  This is a 'no-more-sub-tasks' marker.

            if (!task.getTrace().getLastLine().task.isEmpty())
            {
                //  ... and it was created by our corresponding splitter.
                //  This joiner is now completting.
                task.processFinalizing();
            }
            else
            {
                //  ... and it was created BEFORE our corresponding splitter.
                //  So pass it on.
                relayNoMoreSubTasks(task);
            }
        }
    }
}
