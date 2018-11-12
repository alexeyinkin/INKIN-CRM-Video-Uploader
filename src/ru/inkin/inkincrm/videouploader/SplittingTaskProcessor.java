package ru.inkin.inkincrm.videouploader;

/**
 * This TaskProcessor sends multiple tasks to the next one from one source task
 * effectively splitting the upcoming job.
 * It stores all the info to merge subtasks again in some later processor.
 * Use it:
 * -- To split video processing to video variants with different resolution.
 * -- To split video file to segments to later upload each segment.
 * @author Alexey
 * @param <InTask>          Class of tasks this processor is fed by.
 * @param <NextProcessor>   Class of the next processor in chain.
 * @param <NextTask>        Class of the tasks the next processor is fed by.
 */
public abstract class SplittingTaskProcessor
        <
            InTask          extends TaskGeneric,
            NextProcessor   extends TaskProcessor,
            NextTask        extends TaskGeneric
        >
        extends TaskProcessor<InTask, NextProcessor, NextTask>
{
    @Override
    public void addTask(InTask task) //, boolean isLastInItsParent)
    {
        task.setSplitting(true);
        super.addTask(task); //, isLastInItsParent);
    }

    @Override
    public void sendTaskToNext(NextTask task) //, boolean isLastInItsParent)
    {
        //task.setProcessor(getNextProcessor());
        //task.addTraceLevel(new TaskTraceLine(task, 0, isLastInItsParent));
        //  TODO:      Do we need index? Now using ^ zero. Remove?

        super.sendTaskToNext(task); //, isLastInItsParent);
    }

    protected void sendNoMoreSubTasks(InTask task) //, Class<NextTask> cls)
    {
        NextTask empty = createNextTask(task);//, cls);
        empty.setEmpty(true);
        sendTaskToNext(empty); //, LAST);
    }
}
