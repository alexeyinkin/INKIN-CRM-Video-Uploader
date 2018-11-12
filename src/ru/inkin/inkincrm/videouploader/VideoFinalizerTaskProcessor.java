package ru.inkin.inkincrm.videouploader;

public class VideoFinalizerTaskProcessor extends JoiningTaskProcessor
        <
            VideoFinalizerTask,
            QueueFinalizerTaskProcessor,
            QueueFinalizerTask
        >
{
    @Override
    public QueueFinalizerTask createNextTaskObject()
    {
        return new QueueFinalizerTask();
    }
}
