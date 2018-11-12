package ru.inkin.inkincrm.videouploader;

public class QueueFinalizerTaskProcessor extends JoiningTaskProcessor
        <
            QueueFinalizerTask,
            QueueFinalizerTaskProcessor,    //  Would never call next,
            QueueFinalizerTask              //  so any classes could be here.
        >
{
    @Override
    public QueueFinalizerTask createNextTaskObject()
    {
        return null;
    }
}
