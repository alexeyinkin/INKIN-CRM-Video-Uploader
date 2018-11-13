package ru.inkin.inkincrm.videouploader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class VideoVariantConverter
{
    private String                  videoDirName;
    private String                  variantDirName;
    private VideoVariantSettings    variantSettings;
    private FileTask                fileTask;
    private float                   duration;
    private VideoSegmentInfo        currentSegmentInfo;
    private int                     nextSegmentIndex            = 0;
    private final List<VideoVariantConverterListener> listeners = new ArrayList<>();
    private Process                 ffmpegProcess;

    private String                  encryptionIvAsString;
    private String                  encryptionKeyAsString;

    //  Opening 'crypto:D:\.../temp/.../720/720p_0.ts' for writing
    private final Pattern newSegmentPattern = Pattern.compile("Opening '(crypto:)?(.+\\.ts)' for writing");

    //  frame=   92 fps= 44 q=52.0 size=N/A time=00:00:04.17 bitrate=N/A speed=2.01x
    private final Pattern progressPattern   = Pattern.compile("^frame=.*time=(\\d+):(\\d+):(\\d+\\.\\d+) ");

    public void setVariantSettings(VideoVariantSettings variantSettings)
    {
        this.variantSettings = variantSettings;
    }

    public void setFileTask(FileTask fileTask)
    {
        this.fileTask = fileTask;
        videoDirName = fileTask.getDirName();
    }

    public void start()
    {
        init();
        createPreencryptionKey();
        createKeyInfoFile();
        encode();
    }

    private void init()
    {
        fileTask.log("Starting variant: " + variantSettings.getResolutionString());

        variantDirName = videoDirName + "/" + variantSettings.getHeight();
        new File(variantDirName).mkdirs();

        duration = fileTask.getSourceInfo().duration;
        fileTask.addListener(new PrivateFileTaskListener());
    }

    private void createPreencryptionKey()
    {
        SecureRandom    generator   = new SecureRandom();
        byte[]          bytes       = new byte[16];

        fileTask.log("Creating preencryption key.");
        generator.nextBytes(bytes);

        try
        {
            FileOutputStream fos = new FileOutputStream(getKeyPath());
            fos.write(bytes);
            fos.close();

            StringBuilder   keyBuilder   = new StringBuilder();

            for (byte b : bytes)
            {
                keyBuilder.append(String.format("%02X", b));
            }

            encryptionKeyAsString = keyBuilder.toString();
        }
        catch (Exception e) {}
    }

    private void createKeyInfoFile()
    {
        try
        {
            FileOutputStream    fos     = new FileOutputStream(getKeyInfoPath());
            String[]            lines   = new String[3];

            SecureRandom    generator   = new SecureRandom();
            byte[]          ivBytes     = new byte[16];
            StringBuilder   ivBuilder   = new StringBuilder();

            generator.nextBytes(ivBytes);

            for (byte b : ivBytes)
            {
                ivBuilder.append(String.format("%02X", b));
            }

            encryptionIvAsString = ivBuilder.toString();

            lines[0] = "https://any-domain-because-it-is-not-used.com\n";
            lines[1] = getKeyPath() + "\n";
            lines[2] = encryptionIvAsString + "\n";

            for (String line : lines)
            {
                fos.write(line.getBytes(StandardCharsets.UTF_8));
            }

            fos.close();
        }
        catch (Exception e) {}
    }

    private String getKeyPath()
    {
        return variantDirName + "/enc.key";
    }

    private String getKeyInfoPath()
    {
        return variantDirName + "/key-info.txt";
    }

    private void encode()
    {
        //  TODO: Pack ffmpeg with 'libfdk_aac' audio encoder support
        //  because it is said to give a better quality:
        //  https://trac.ffmpeg.org/wiki/Encode/AAC

        String cmd = "\"" + InkinCrmVideoUploader.getFfmpegPath() + "\" "
                + "-i \"" + fileTask.getFile().getAbsolutePath() + "\" "
                //+ "-acodec copy "
                + "-c:a aac "                                                   //  Audio codec
                + "-b:a 128k "                                                  //  Audio bitrate
                + "-vb " + variantSettings.getBitsPerSecond() + " "             //  Video bitrate
                + "-vf scale=" + variantSettings.getWidth() + ":" + variantSettings.getHeight() + " " //  Video resolution
                //+ "-vf scale=-1:" + variantSettings.getHeight() + " "           //  Video resolution
                + "-c:v h264 "                                                  //  Video codec
                + "-g 48 -keyint_min 48 "
                + "-profile:v main "
                + "-sc_threshold 0 "
                + "-hls_segment_filename \"" + variantDirName + "/" + variantSettings.getHeight() + "p_%d.ts\" "
                + "-hls_time 10 "
                + "-hls_key_info_file \"" + getKeyInfoPath() + "\" "
                + "-hls_playlist_type vod "
                + "\"" + getPlaylistFileName() + "\"";
                //+ "\"" + variantDirName + "/" + variantSettings.getHeight() + ".m3u8\"";

        Runtime runtime = Runtime.getRuntime();

        try
        {
            ffmpegProcess = runtime.exec(cmd);
            new StreamGobbler(ffmpegProcess.getInputStream()).start();

            //pr.waitFor();

            //System.out.println(cmd);
            //System.out.println(pr.exitValue());
            BufferedReader r = new BufferedReader(new InputStreamReader(ffmpegProcess.getErrorStream()));
            String line;// = buf.readLine();

            while ((line = r.readLine()) != null)
            {
                //output += line;
                processLine(line);
            }

            onCurrentSegmentFileReady();    //  Yield the last segment.
            fileTask.log("Variant converted: " + variantSettings.getResolutionString());
            fileTask.flushLogger();
        }
        catch (Exception e)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            fileTask.log("Exception: " + sw.toString());
        }
    }

    private void processLine(String line)
    {
        Matcher matcher;
        fileTask.log("ffmpeg>" + line);
        //System.out.println("ffmpeg>" + line);
        
        matcher = newSegmentPattern.matcher(line);
        if (matcher.find())
        {
            processLine_openSegmentFile(matcher.group(2));
            return;
        }

        matcher = progressPattern.matcher(line);
        if (matcher.find())
        {
            processLine_progress(matcher);
        }
    }

    /**
     * Called when ffmpeg opens a segment file for output.
     * It means that the previous segment (if any) is now sure ready for upload.
     * @param filePath 
     */
    private void processLine_openSegmentFile(String filePath)
    {
        onCurrentSegmentFileReady();

        currentSegmentInfo = new VideoSegmentInfo();
        currentSegmentInfo.localFile = new File(filePath);
    }

    private void processLine_progress(Matcher matcher)
    {
        float position =
                Integer.parseInt(matcher.group(1)) * 60 * 60
                + Integer.parseInt(matcher.group(2)) * 60
                + Float.parseFloat(matcher.group(3));

        float progress = position / duration;

        for (VideoVariantConverterListener listener : listeners)
        {
            listener.onProgress(progress);
        }
    }

    /**
     * Called to gather file info and notify the listeners.
     * Called:
     * -- On previous segment when a new one's file is opened for writing.
     * -- On the last segment when ffmpeg output is over.
     */
    private void onCurrentSegmentFileReady()
    {
        if (currentSegmentInfo != null && currentSegmentInfo.localFile.exists())
        {
            currentSegmentInfo.size = currentSegmentInfo.localFile.length();
            currentSegmentInfo.index = nextSegmentIndex++;

            for (VideoVariantConverterListener listener : listeners)
            {
                listener.onSegmentFileReady(currentSegmentInfo);
            }
        }
    }

    private void abort()
    {
        if (ffmpegProcess != null && ffmpegProcess.isAlive())
        {
            ffmpegProcess.destroy();
        }
    }

    public void addListener(VideoVariantConverterListener listener)
    {
        listeners.add(listener);
    }

    private String getPlaylistFileName()
    {
        return variantDirName + "/" + variantSettings.getHeight() + ".m3u8";
    }

    public File getPlaylistFile()
    {
        return new File(getPlaylistFileName());
    }

    /**
     * Returns the initialization vector as hex string without '0x'.
     * @return 
     */
    public String getEncryptionIvAsString()
    {
        return encryptionIvAsString;
    }

    /**
     * Returns the preencryption key as hex string without '0x'.
     * @return 
     */
    public String getEncryptionKeyAsString()
    {
        return encryptionKeyAsString;
    }

    private class PrivateFileTaskListener implements FileTaskListener
    {
        @Override
        public void onStatusChange(byte status)
        {
            switch (status)
            {
                case FileTask.ABORTED:
                    abort();
            }
        }

        @Override
        public void onProgress(String resolution, float progress)
        {
        }
    }
}
