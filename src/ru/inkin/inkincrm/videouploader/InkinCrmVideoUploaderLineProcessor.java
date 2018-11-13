package ru.inkin.inkincrm.videouploader;

public class InkinCrmVideoUploaderLineProcessor extends LineProcessor
{
    private QueueStarterTaskProcessor           queueStarter;
    private VideoCreatorTaskProcessor           videoCreator;
    private VideoVariantCreatorTaskProcessor    videoVariantCreator;
    private VideoVariantConverterTaskProcessor  videoVariantConverter;
    private VideoSegmentUploaderTaskProcessor   videoSegmentUploader;
    private VideoVariantFinalizerTaskProcessor  videoVariantFinalizer;
    private VideoFinalizerTaskProcessor         videoFinalizer;
    private QueueFinalizerTaskProcessor         queueFinalizer;

    private void initIfNot()
    {
        if (queueStarter != null) return;

        queueStarter            = new QueueStarterTaskProcessor();
        videoCreator            = new VideoCreatorTaskProcessor();
        videoVariantCreator     = new VideoVariantCreatorTaskProcessor();
        videoVariantConverter   = new VideoVariantConverterTaskProcessor();
        videoSegmentUploader    = new VideoSegmentUploaderTaskProcessor();
        videoVariantFinalizer   = new VideoVariantFinalizerTaskProcessor();
        videoFinalizer          = new VideoFinalizerTaskProcessor();
        queueFinalizer          = new QueueFinalizerTaskProcessor();

        queueStarter.setNextProcessor(videoCreator);
        videoCreator.setNextProcessor(videoVariantCreator);
        videoVariantCreator.setNextProcessor(videoVariantConverter);
        videoVariantConverter.setNextProcessor(videoSegmentUploader);
        videoSegmentUploader.setNextProcessor(videoVariantFinalizer);
        videoVariantFinalizer.setNextProcessor(videoFinalizer);
        videoFinalizer.setNextProcessor(queueFinalizer);

        queueFinalizer.setLineProcessor(this);

        //  Create and start threads to wait on their queues.
        queueStarter.start();
        videoCreator.start();
        videoVariantCreator.start();
        videoVariantConverter.start();
        videoSegmentUploader.start();
        videoVariantFinalizer.start();
        videoFinalizer.start();
        queueFinalizer.start();
    }

    @Override
    public synchronized void start()
    {
        setStatus(IN_PROGRESS);

        initIfNot();
        QueueStarterTask task = new QueueStarterTask();
        task.setFileTasks(InkinCrmVideoUploader.getFileTasks());

        queueStarter.addTask(task); //, TaskProcessor.LAST);
    }
}
