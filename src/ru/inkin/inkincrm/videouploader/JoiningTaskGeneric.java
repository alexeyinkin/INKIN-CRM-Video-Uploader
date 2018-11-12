package ru.inkin.inkincrm.videouploader;

public abstract class JoiningTaskGeneric
        <ProcessorClass extends JoiningTaskProcessor>
        extends TaskGeneric<ProcessorClass>
{
    /**
     * Called if this task came to the JoiningTaskProcessor that is still
     * accumulating tasks. Most tasks do not do any processing here
     * so they do not need to implement their own 'process()', we make it empty.
     */
    @Override
    public void process()
    {
    }

    /**
     * Called if this task is a 'no-more-sub-tasks' marker so it is
     * finalizing the JoiningTaskProcessor.
     */
    public abstract void processFinalizing();
}
