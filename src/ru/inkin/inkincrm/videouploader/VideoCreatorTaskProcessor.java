package ru.inkin.inkincrm.videouploader;

/**
 * Creates a new Video at the server or empties an existing Video
 * so that VideoVariant's could be uploaded.
 * @author Alexey
 */
public class VideoCreatorTaskProcessor extends SplittingTaskProcessor
        <
            VideoCreatorTask,
            VideoVariantCreatorTaskProcessor,
            VideoVariantCreatorTask
        >
{
    @Override
    public VideoVariantCreatorTask createNextTaskObject()
    {
        return new VideoVariantCreatorTask();
    }
}
