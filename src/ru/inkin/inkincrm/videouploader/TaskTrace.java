package ru.inkin.inkincrm.videouploader;

import java.util.ArrayList;
import java.util.List;

/**
 * A trace of the Task queue positions as the Task was splitted to sub-tasks.
 * Contains a TaskQueuePosition for each step of sub-task divisions.
 * Allows to track completion of above-tasks.
 * @author Alexey
 */
public class TaskTrace implements Cloneable
{
    private ArrayList<TaskTraceLine> trace = new ArrayList<>();

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        TaskTrace obj = (TaskTrace) super.clone();
        obj.trace = (ArrayList<TaskTraceLine>) trace.clone();
        return obj;
    }

    public void addLevel(TaskTraceLine level)
    {
        trace.add(level);
    }

    /**
     * Removes all trailing levels until encountered a task that was created
     * by a SplittingTaskProcessor.
     * In particular it does nothing if such a task is the last one.
     * It is called when a RelayingTaskProcessor sends a task to
     * a JoiningTaskProcessor so that the latter could join subtasks by
     * only checking the last trace.
     */
    public void removeLevelsTillSplitting()
    {
        for (int i = trace.size(); --i >= 0; )
        {
            if (trace.get(i).task.isSplitting())
            {
                break;
            }
            else
            {
                trace.remove(i);
            }
        }
    }

    /**
     * Removes the last trace line of a SplittingTaskProcessor.
     * Called when a JoiningTaskProcessor adds task to any next TaskProcessor
     * to actually join the subtasks together again.
     */
    public void removeLastSplitting()
    {
        removeLevelsTillSplitting();
        trace.remove(trace.size() - 1);
    }

//    public boolean isLastInLastLevel()
//    {
//        return trace.get(trace.size() - 1).isLast;
//    }

    public TaskTraceLine getLastLine()
    {
        return trace.size() > 0 ? trace.get(trace.size() - 1) : null;
    }

    public List<String> getDebugStrings()
    {
        List<String> parts = new ArrayList<>();

        for (int i = 0; i < trace.size(); i++)
        {
            parts.add(trace.get(i).task.getDebugString());
        }

        return parts;
    }
}
