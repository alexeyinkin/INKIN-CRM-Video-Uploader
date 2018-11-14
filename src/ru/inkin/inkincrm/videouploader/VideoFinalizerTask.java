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
        initPrivate();

        if (!uploadThumb())
        {
            fileTask.setStatus(FileTask.ABORTED);
        }
        else
        {
            fileTask.log("Video complete: " + fileTask.getFile().getAbsolutePath());
            fileTask.flushLogger();

            //showComplete();
            fileTask.setStatus(FileTask.COMPLETE);
            nextStep();
        }
    }

    private void initPrivate()
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

        JsonObject response = InkinCrmVideoUploader.callServerMethod(
                "Image",
                "upload",
                params,
                filesToUpload,
                fileTask.getLogger());

        return response != null && response.getString("status").equals("ok");
    }

//    private void showComplete()
//    {
//        InkinCrmVideoUploader.showFileComplete(fileTask.getFile().getAbsolutePath());
//    }

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
