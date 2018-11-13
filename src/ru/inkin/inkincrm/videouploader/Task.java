package ru.inkin.inkincrm.videouploader;

import java.util.List;

/**
 * Abstract class to be extended by generic. We need it because TaskTrace
 * should store TaskGeneric with different parameters.
 * @author Alexey
 */
public abstract class Task
{
    private TaskTrace       trace;
    private boolean         empty = false;

    /**
     * Is of a SplittingTaskProcessor?
     */
    private boolean         splitting = false;

    abstract public void process();

    /**
     * Called before processing and before determining if it isCancelled().
     */
    public void init()
    {
    }

    public boolean isCancelled()
    {
        if (trace == null) return false;

        return trace.getLastLine().task.isCancelled();
    }

    public void setOrigin(Task origin)
    {
        try
        {
            trace = origin.trace == null
                    ? new TaskTrace()
                    : (TaskTrace) origin.trace.clone();

            trace.addLevel(new TaskTraceLine(origin));
        }
        catch (CloneNotSupportedException e) {}
    }

    public void addTraceLevel(TaskTraceLine level)
    {
        if (trace == null) trace = new TaskTrace();
        trace.addLevel(level);
    }

    public void setEmpty(boolean empty)
    {
        this.empty = empty;
    }

    public boolean isEmpty()
    {
        return empty;
    }

    public void setSplitting(boolean splitting)
    {
        this.splitting = splitting;
    }

    public boolean isSplitting()
    {
        return splitting;
    }

//    public boolean isLast()
//    {
//        return trace.isLastInLastLevel();
//    }

    public TaskTrace getTrace()
    {
        return trace;
    }

    public String getDebugString()
    {
        return isEmpty() ? "EMPTY" : getNonEmptyDebugString();
    }

    public String getNonEmptyDebugString()
    {
        return "*";
    }

    public String getDebugStringWithTrace()
    {
        List<String> parts = trace.getDebugStrings();
        parts.add(getDebugString());

        return String.join(" | ", parts) + " (" + getClass().getSimpleName() + ")";
    }
}
