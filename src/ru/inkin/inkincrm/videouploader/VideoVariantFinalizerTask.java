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
    private long                    firstSegmentId;

    /**
     * Called when the last segment file has been uploaded.
     */
    @Override
    public void processFinalizing()
    {
        initPrivate();

        if (uploadPlaylist())
        {
            updateFirstVideoSegment();
            markVideoVariantProcessed();

            nextStep();
        }
    }

    private void initPrivate()
    {
        videoVariantSettings    = getVideoVariantCreatorTask().getVideoVariantSettings();
        converter               = getVideoVariantConverterTask().getConverter();
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
                filesToUpload);

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
    private void updateFirstVideoSegment()
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
                .add("column",  "params")
                .add("value",   baos.toString())
//                .add("item",    Json.createObjectBuilder()
//                        .add("params",  baos.toString())
//                )
                .build();

        //  TODO: Handle errors.
        InkinCrmVideoUploader.applyServerCommand(command);
    }

    /**
     * Sets the flags at server marking that the variant is uploaded
     * and processed.
     */
    private void markVideoVariantProcessed()
    {
        JsonObject command = Json.createObjectBuilder()
                .add("action",  "edit")
                .add("catalog", "video-variants")
                .add("id",      videoVariantSettings.getVideoVariantId())
                .add("column",  "videoVariantFlags")
                .add("value",   0)
//                .add("item",    Json.createObjectBuilder()
//                        .add("videoVariantFlags",   0)  // Uploaded, processed.
//                )
                .build();

        //  TODO: Handle errors.
        InkinCrmVideoUploader.applyServerCommand(command);
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
