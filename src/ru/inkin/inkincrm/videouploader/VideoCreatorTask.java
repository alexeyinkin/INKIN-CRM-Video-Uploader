package ru.inkin.inkincrm.videouploader;

import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;

public class VideoCreatorTask extends TaskGeneric<VideoCreatorTaskProcessor>
{
    private FileTask fileTask;

    public void setFileTask(FileTask fileTask)
    {
        this.fileTask = fileTask;
    }

    public FileTask getFileTask()
    {
        return fileTask;
    }

    @Override
    public void process()
    {
        if (initVideo())
        {
            showInProgress();
            startVariants();
            //finalizeQueue(); -- was for testing.
        }
    }

    private boolean initVideo()
    {
        boolean result = false;

        switch (fileTask.getAction())
        {
            case FileTask.UPLOAD_NEW:
                result = createNew();
                break;

            case FileTask.REPLACE:
                result = emptyExisting();
                break;
        }

        return result;
    }

    private void showInProgress()
    {
        InkinCrmVideoUploader.showFileInProgress(fileTask.getFile().getAbsolutePath());
    }

    private boolean createNew()
    {
        String  fileName    = fileTask.getFile().getName();
        String  tempId      = "new" + fileTask.getIndex();

        JsonObject command = Json.createObjectBuilder()
                .add("action",  "new")
                .add("catalog", "videos")
                .add("return",  "*")
                .add("item",    Json.createObjectBuilder()
                        .add("id",                  tempId)
                        .add("title",               fileName)
                        .add("enabled",             true)
                        .add("segmentLinkUseCount", 1)      //  One-time links, protect from download.
                        .add("encryptionMode",      "preencrypted")
                        .add("sourceFileName",      fileName)
                ).build();

        try
        {
            JsonObject response = InkinCrmVideoUploader.applyServerCommand(command);

            fileTask.setVideoId(
                    response
                        .getJsonObject("data")
                        .getJsonObject("items")
                        .getJsonObject("137")
                        .getJsonObject(tempId)
                        .getJsonNumber("id")
                        .intValue());
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    private boolean emptyExisting()
    {
        long                videoId     = InkinCrmVideoUploader.getReplaceVideoId();
        String              fileName    = fileTask.getFile().getName();
        Map<String, String> params      = new HashMap<>();

        fileTask.setVideoId(videoId);
        params.put("id", String.valueOf(videoId));

        JsonObject response = InkinCrmVideoUploader.callServerMethod(
                "Video",
                "emptyVideo",
                params,
                null);          //  Files.

        if (response == null || !response.getString("status").equals("ok"))
        {
            return false;
        }

        JsonObject command = Json.createObjectBuilder()
                .add("action",  "edit")
                .add("catalog", "videos")
                .add("id",      videoId)
                .add("column",  "sourceFileName")
                .add("value",   fileName)
                .build();

        response = InkinCrmVideoUploader.applyServerCommand(command);
        return response != null && response.getString("status").equals("ok");
    }

    private void startVariants()
    {
        Map<String, Integer>        bitrates    = fileTask.getSelectedBitrates();
        VideoCreatorTaskProcessor   processor   = getProcessor();
        int                         left        = bitrates.size();

        for (String resolution : bitrates.keySet())
        {
            try
            {
                VideoVariantSettings settings = new VideoVariantSettings();
                short   height  = Short.parseShort(resolution);
                short   width   = (short) Math.round((float) height * fileTask.getSourceInfo().ratio);

                if (width % 2 > 0) width++;

                settings.setBitsPerSecond(bitrates.get(resolution));
                settings.setWidth(width);
                settings.setHeight(height);

                VideoVariantCreatorTask task = processor.createNextTask(this);
                    //this, VideoVariantCreatorTask.class);

                task.setVideoVariantSettings(settings);
                processor.sendTaskToNext(task); //, --left == 0);
            }
            catch (Exception e) {}
        }

        processor.sendNoMoreSubTasks(this); //, VideoVariantCreatorTask.class);
    }
}
