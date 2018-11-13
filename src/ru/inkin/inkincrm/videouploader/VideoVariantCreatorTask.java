package ru.inkin.inkincrm.videouploader;

import javax.json.Json;
import javax.json.JsonObject;

public class VideoVariantCreatorTask extends TaskGeneric<VideoVariantCreatorTaskProcessor>
{
    private VideoVariantSettings settings;

    public void setVideoVariantSettings(VideoVariantSettings settings)
    {
        this.settings = settings;
    }

    @Override
    public void process()
    {
        if (createNew())
        {
            nextStep();
        }
    }

    public VideoCreatorTask getVideoCreatorTask()
    {
        return (VideoCreatorTask) getTrace().getLastLine().task;
    }

    private boolean createNew()
    {
        FileTask    fileTask    = getVideoCreatorTask().getFileTask();
        VideoInfo   sourceInfo  = fileTask.getSourceInfo();
        String      tempId      = "new" + fileTask.getIndex();

        JsonObject command = Json.createObjectBuilder()
                .add("action",  "new")
                .add("catalog", "video-variants")
                .add("return",  "*")
                .add("set",     Json.createObjectBuilder()
                        .add("id",                  tempId)
                        .add("id_variantVideo",     fileTask.getVideoId())
                        .add("width",               settings.getWidth())
                        .add("height",              settings.getHeight())
                        .add("codecs",              "mp4a.40.2,avc1.4d0028")
                        .add("videoVariantFlags",   3)  //  UPLOADING | PROCESSING
                ).build();

        try
        {
            JsonObject response = InkinCrmVideoUploader.applyServerCommand(
                    command,
                    fileTask.getLogger());

            settings.setVideoVariantId(
                    response
                        .getJsonObject("data")
                        .getJsonObject("items")
                        .getJsonObject("138")
                        .getJsonObject(tempId)
                        .getJsonNumber("id")
                        .longValue());
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    /**
     * Sends a task to encode this variant.
     */
    private void nextStep()
    {
        VideoVariantCreatorTaskProcessor    processor   = getProcessor();

        VideoVariantConverterTask task = processor.createNextTask(this);
                    //this, VideoVariantConverterTask.class);

        processor.sendTaskToNext(task); //, TaskProcessor.LAST);

        //processor.sendNoMoreSubTasks(this, VideoVariantConverterTask.class);
    }

    public VideoVariantSettings getVideoVariantSettings()
    {
        return settings;
    }

    @Override
    public String getNonEmptyDebugString()
    {
        return String.valueOf(settings.getHeight());
    }
}
