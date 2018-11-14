package ru.inkin.inkincrm.videouploader;

public class VideoVariantConverterTask
        extends TaskGeneric<VideoVariantConverterTaskProcessor>
        implements VideoVariantConverterListener
{
    private VideoVariantConverter   videoVariantConverter;
    private FileTask                fileTask;
    private String                  filePath;
    private VideoVariantSettings    videoVariantSettings;

    @Override
    public void init()
    {
        VideoVariantCreatorTask parent1     = getVideoVariantCreatorTask();
        VideoCreatorTask        parent2     = parent1.getVideoCreatorTask();

        fileTask                = parent2.getFileTask();
        videoVariantSettings    = parent1.getVideoVariantSettings();
    }

    @Override
    public void process()
    {
        start();
    }

    private void start()
    {
        filePath                = fileTask.getFile().getAbsolutePath();        
        videoVariantConverter   = new VideoVariantConverter();

        videoVariantConverter.setFileTask(fileTask);
        videoVariantConverter.setVariantSettings(videoVariantSettings);
        videoVariantConverter.addListener(this);
        videoVariantConverter.start();

        getProcessor().sendNoMoreSubTasks(this);
    }

    public VideoVariantCreatorTask getVideoVariantCreatorTask()
    {
        return (VideoVariantCreatorTask) getTrace().getLastLine().task;
    }

    @Override
    public void onSegmentFileReady(VideoSegmentInfo videoSegmentInfo)
    {
        VideoVariantConverterTaskProcessor  processor = getProcessor();
        VideoSegmentUploaderTask nextTask = processor.createNextTask(this);

        nextTask.setVideoSegmentInfo(videoSegmentInfo);
        processor.sendTaskToNext(nextTask);
    }

    @Override
    public void onProgress(float progress)
    {
        fileTask.setProgress(
                videoVariantSettings.getResolutionString(),
                progress);
    }

    public VideoVariantConverter getConverter()
    {
        return videoVariantConverter;
    }

    @Override
    public boolean isCancelled()
    {
        return fileTask != null && fileTask.getStatus() == FileTask.ABORTED;
    }
}
