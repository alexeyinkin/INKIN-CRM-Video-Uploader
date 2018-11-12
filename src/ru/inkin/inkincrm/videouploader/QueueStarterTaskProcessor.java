package ru.inkin.inkincrm.videouploader;

public class QueueStarterTaskProcessor extends SplittingTaskProcessor
        <
            QueueStarterTask,
            VideoCreatorTaskProcessor,
            VideoCreatorTask
        >
{
    @Override
    public VideoCreatorTask createNextTaskObject()
    {
        return new VideoCreatorTask();
    }
}
