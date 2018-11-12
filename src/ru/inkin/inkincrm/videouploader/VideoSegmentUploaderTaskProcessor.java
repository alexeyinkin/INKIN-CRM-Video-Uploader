package ru.inkin.inkincrm.videouploader;

public class VideoSegmentUploaderTaskProcessor extends RelayingTaskProcessor
        <
            VideoSegmentUploaderTask,
            VideoVariantFinalizerTaskProcessor,
            VideoVariantFinalizerTask
        >
{
    @Override
    public VideoVariantFinalizerTask createNextTaskObject()
    {
        return new VideoVariantFinalizerTask();
    }
}
