package ru.inkin.inkincrm.videouploader;

public class VideoVariantFinalizerTaskProcessor extends JoiningTaskProcessor
        <
            VideoVariantFinalizerTask,
            VideoFinalizerTaskProcessor,
            VideoFinalizerTask
        >
{
    @Override
    public VideoFinalizerTask createNextTaskObject()
    {
        return new VideoFinalizerTask();
    }
}
