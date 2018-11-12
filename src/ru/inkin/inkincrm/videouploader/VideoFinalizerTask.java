package ru.inkin.inkincrm.videouploader;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import javax.json.JsonObject;

public class VideoFinalizerTask extends JoiningTaskGeneric<VideoFinalizerTaskProcessor>
{
    private FileTask    fileTask;

    /**
     * Called when the last VideoVariant has been finalized.
     */
    @Override
    public void processFinalizing()
    {
        init();

        if (uploadThumb())
        {
            showComplete();
            nextStep();
        }
    }

    private void init()
    {
        VideoCreatorTask videoCreatorTask = getVideoCreatorTask();

        fileTask = videoCreatorTask.getFileTask();
    }

    private boolean uploadThumb()
    {
        Map<String, String> params          = new HashMap<>();
        Map<String, File>   filesToUpload   = new HashMap<>();

        params.put("catalog",   "videos");
        params.put("id",        String.valueOf(fileTask.getVideoId()));
        filesToUpload.put("playlist", fileTask.getThumbFile());

        JsonObject obj = InkinCrmVideoUploader.callServerMethod(
                "Image",
                "upload",
                params,
                filesToUpload);

        if (obj == null || !obj.getString("status").equals("ok"))
        {
            return false;
        }

        return true;
    }

    private void showComplete()
    {
        InkinCrmVideoUploader.showFileComplete(fileTask.getFile().getAbsolutePath());
    }

    private VideoCreatorTask getVideoCreatorTask()
    {
        return (VideoCreatorTask) getTrace().getLastLine().task;
    }

    /**
     * Sends a task to the queue finalizer to wait for all videos to finalize.
     */
    private void nextStep()
    {
        VideoFinalizerTaskProcessor processor   = getProcessor();

        QueueFinalizerTask task = processor.createNextTask(this);
                    //this, QueueFinalizerTask.class);

        processor.sendTaskToNext(task); //, TaskProcessor.LAST);
    }
}
