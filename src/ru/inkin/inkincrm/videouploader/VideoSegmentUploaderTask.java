package ru.inkin.inkincrm.videouploader;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import javax.json.JsonObject;

public class VideoSegmentUploaderTask extends TaskGeneric<VideoSegmentUploaderTaskProcessor>
{
    private VideoVariantSettings    videoVariantSettings;
    private VideoSegmentInfo        videoSegmentInfo;
    private FileTask                fileTask;

    @Override
    public void process()
    {
        initPrivate();

        if (upload())
        {
            nextStep();
        }
    }

    private void initPrivate()
    {
        VideoVariantCreatorTask parent2 = getVideoVariantConverterTask().getVideoVariantCreatorTask();

        videoVariantSettings    = parent2.getVideoVariantSettings();
        fileTask                = parent2.getVideoCreatorTask().getFileTask();

        fileTask.log("Uploading segment: " + videoSegmentInfo.localFile.getAbsolutePath());
    }

    private boolean upload()
    {
        Map<String, String> params          = new HashMap<>();
        Map<String, File>   filesToUpload   = new HashMap<>();

        params.put(
                "variantId",
                String.valueOf(videoVariantSettings.getVideoVariantId()));

        filesToUpload.put("file", videoSegmentInfo.localFile);

        JsonObject obj = InkinCrmVideoUploader.callServerMethod(
                "Video",
                "uploadSegmentFile",
                params,
                filesToUpload,
                fileTask.getLogger());

        fileTask.flushLogger();
        return obj != null && obj.getString("status").equals("ok");
    }

    /**
     * Sends a task to joiner to wait for all segments to upload.
     */
    private void nextStep()
    {
        VideoSegmentUploaderTaskProcessor   processor   = getProcessor();

        VideoVariantFinalizerTask task = processor.createNextTask(this);
                    //this, VideoVariantFinalizerTask.class);

        processor.sendTaskToNext(task); //, TaskProcessor.LAST);
    }

    public void setVideoSegmentInfo(VideoSegmentInfo videoSegmentInfo)
    {
        this.videoSegmentInfo = videoSegmentInfo;
    }

    public VideoSegmentInfo getVideoSegmentInfo()
    {
        return videoSegmentInfo;
    }

    public VideoVariantConverterTask getVideoVariantConverterTask()
    {
        return (VideoVariantConverterTask) getTrace().getLastLine().task;
    }

    @Override
    public String getNonEmptyDebugString()
    {
        return String.valueOf(videoSegmentInfo.index);
    }
}
