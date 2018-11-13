package ru.inkin.inkincrm.videouploader;

public class QueueFinalizerTaskProcessor extends JoiningTaskProcessor
        <
            QueueFinalizerTask,
            QueueFinalizerTaskProcessor,    //  Would never call next,
            QueueFinalizerTask              //  so any classes could be here.
        >
{
    private LineProcessor lineProcessor;

    @Override
    public QueueFinalizerTask createNextTaskObject()
    {
        return null;
    }

    public void setLineProcessor(LineProcessor lineProcessor)
    {
        this.lineProcessor = lineProcessor;
    }

    public void onComplete()
    {
        lineProcessor.onComplete();
    }
}
