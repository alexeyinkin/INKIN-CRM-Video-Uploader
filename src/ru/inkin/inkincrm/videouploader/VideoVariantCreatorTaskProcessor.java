package ru.inkin.inkincrm.videouploader;

public class VideoVariantCreatorTaskProcessor extends RelayingTaskProcessor
        <
            VideoVariantCreatorTask,
            VideoVariantConverterTaskProcessor,
            VideoVariantConverterTask
        >
{
    @Override
    public VideoVariantConverterTask createNextTaskObject()
    {
        return new VideoVariantConverterTask();
    }
}
