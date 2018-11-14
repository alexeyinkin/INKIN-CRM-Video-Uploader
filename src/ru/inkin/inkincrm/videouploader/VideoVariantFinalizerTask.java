package ru.inkin.inkincrm.videouploader;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;

public class VideoVariantFinalizerTask extends JoiningTaskGeneric<VideoVariantFinalizerTaskProcessor>
{
    private VideoVariantSettings    videoVariantSettings;
    private VideoVariantConverter   converter;
    private FileTask                fileTask;
    private long                    firstSegmentId;

    /**
     * Called when the last segment file has been uploaded.
     */
    @Override
    public void processFinalizing()
    {
        initPrivate();

        boolean result = uploadPlaylist();

        if (result)
        {
            result = updateFirstVideoSegment();
        }

        if (result)
        {
            result = markVideoVariantProcessed();
        }

        if (!result)
        {
            fileTask.setStatus(FileTask.ABORTED);
        }
        else
        {
            fileTask.log(
                    "Variant uploaded: "
                    + videoVariantSettings.getResolutionString());

            fileTask.flushLogger();

            nextStep();
        }
    }

    private void initPrivate()
    {
        VideoVariantCreatorTask creatorTask = getVideoVariantCreatorTask();

        videoVariantSettings    = creatorTask.getVideoVariantSettings();
        converter               = getVideoVariantConverterTask().getConverter();
        fileTask                = creatorTask.getVideoCreatorTask().getFileTask();
    }

    /**
     * Uploads the playlist file. It creates the segments on the server.
     * Their files are already uploaded by now, and this only adds
     * the segments to the DB.
     */
    private boolean uploadPlaylist()
    {
        Map<String, String> params          = new HashMap<>();
        Map<String, File>   filesToUpload   = new HashMap<>();

        params.put("id", String.valueOf(videoVariantSettings.getVideoVariantId()));
        filesToUpload.put("playlist", converter.getPlaylistFile());

        JsonObject obj = InkinCrmVideoUploader.callServerMethod(
                "Video",
                "uploadVariantPlaylist",
                params,
                filesToUpload,
                fileTask.getLogger());

        if (obj == null || !obj.getString("status").equals("ok"))
        {
            return false;
        }

        firstSegmentId = obj.getJsonObject("data").getInt("firstSegmentId");
        return true;
    }

    /**
     * Sets the encryption key, IV, etc.
     */
    private boolean updateFirstVideoSegment()
    {
        JsonObject segmentParams = Json.createObjectBuilder()
                .add("encryptionMethod",    "AES-128")
                .add("encryptionIV",        converter.getEncryptionIvAsString())
                .add("encryptionKey",       converter.getEncryptionKeyAsString())
                .build();

        ByteArrayOutputStream   baos    = new ByteArrayOutputStream();
        JsonWriter              writer  = Json.createWriter(baos);
        writer.writeObject(segmentParams);
        writer.close();

        JsonObject command = Json.createObjectBuilder()
                .add("action",  "edit")
                .add("catalog", "video-segments")
                //.add("return",  "*")
                .add("id",      firstSegmentId)
                //.add("column",  "params")
                //.add("value",   baos.toString())
                .add("set",     Json.createObjectBuilder()
                        .add("params",  baos.toString())
                )
                .build();

        JsonObject response = InkinCrmVideoUploader.applyServerCommand(
                command,
                fileTask.getLogger());

        return response != null && response.getString("status").equals("ok");
    }

    /**
     * Sets the flags at server marking that the variant is uploaded
     * and processed.
     */
    private boolean markVideoVariantProcessed()
    {
        JsonObject command = Json.createObjectBuilder()
                .add("action",  "edit")
                .add("catalog", "video-variants")
                .add("id",      videoVariantSettings.getVideoVariantId())
                //.add("column",  "videoVariantFlags")
                //.add("value",   0)
                .add("set",    Json.createObjectBuilder()
                        .add("videoVariantFlags",   0)  // Uploaded, processed.
                )
                .build();

        JsonObject response = InkinCrmVideoUploader.applyServerCommand(
                command,
                fileTask.getLogger());

        return response != null && response.getString("status").equals("ok");
    }

    private VideoVariantConverterTask getVideoVariantConverterTask()
    {
        return (VideoVariantConverterTask) getTrace().getLastLine().task;
    }

    private VideoVariantCreatorTask getVideoVariantCreatorTask()
    {
        return getVideoVariantConverterTask().getVideoVariantCreatorTask();
    }

    /**
     * Sends a task to joiner to wait for all variants to finalize.
     */
    private void nextStep()
    {
        VideoVariantFinalizerTaskProcessor  processor   = getProcessor();

        VideoFinalizerTask task = processor.createNextTask(this);
                    //this, VideoFinalizerTask.class);

        processor.sendTaskToNext(task); //, TaskProcessor.LAST);
    }
}
