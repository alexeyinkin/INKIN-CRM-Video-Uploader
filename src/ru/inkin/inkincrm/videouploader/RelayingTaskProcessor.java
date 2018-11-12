package ru.inkin.inkincrm.videouploader;

/**
 * This TaskProcessor sends one task to the next one from one source task.
 * Use it for everything that:
 * -- does not split tasks to subtasks, and
 * -- does not join previous subtasks to one.
 * @author Alexey
 * @param <InTask>          Class of tasks this processor is fed by.
 * @param <NextProcessor>   Class of the next processor in chain.
 * @param <NextTask>        Class of the tasks the next processor is fed by.
 */
public abstract class RelayingTaskProcessor
        <
            InTask          extends TaskGeneric,
            NextProcessor   extends TaskProcessor,
            NextTask        extends TaskGeneric
        >
        extends TaskProcessor<InTask, NextProcessor, NextTask>
{
    @Override
    public void sendTaskToNext(NextTask task)//, boolean isLastInItsParent)
    {
        //NextProcessor nextProcessor = getNextProcessor();
        //task.setProcessor(nextProcessor);

//        if (nextProcessor instanceof JoiningTaskProcessor)
//        {
//            //  TODO: Remove traces up till the last splitter, keeping that splitter.
//        }
//        else
//        {
            //task.addTraceLevel(new TaskTraceLine(task, 0, isLastInItsParent));
            //  TODO:      Do we need index? Now using ^ zero. Remove?
//        }

        super.sendTaskToNext(task);//, isLastInItsParent);
    }

}
