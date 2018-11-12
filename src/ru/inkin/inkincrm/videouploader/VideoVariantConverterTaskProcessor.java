package ru.inkin.inkincrm.videouploader;

public class VideoVariantConverterTaskProcessor extends SplittingTaskProcessor
        <
            VideoVariantConverterTask,
            VideoSegmentUploaderTaskProcessor,
            VideoSegmentUploaderTask
        >
{
    @Override
    public VideoSegmentUploaderTask createNextTaskObject()
    {
        return new VideoSegmentUploaderTask();
    }
}
