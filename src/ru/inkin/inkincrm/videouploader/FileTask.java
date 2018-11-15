package ru.inkin.inkincrm.videouploader;

import java.io.*;
import java.util.regex.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * This represents a task with File, settings and status.
 * @author Alexey
 */
public class FileTask
{
    private final int   index = nextIndex++;
    private File        file;
    private File        logFile;
    private FileWriter  logger;
    private String      dirName;
    private String      thumbPath;
    private VideoInfo   sourceInfo;
    private long        videoId;
    private byte        status      = READY;
    private final List<FileTaskListener>    listeners   = new ArrayList<>();

    private static int nextIndex    = 0;

    public static final byte    UPLOAD_NEW  = 0;
    public static final byte    REPLACE     = 1;

    public static final byte    READY       = 0;
    public static final byte    WAITING     = 1;
    public static final byte    IN_PROGRESS = 2;
    public static final byte    COMPLETE    = 3;
    public static final byte    ABORTED     = 4;

    public void setFile(File file)
    {
        this.file = file;
    }

    public File getFile()
    {
        return file;
    }

    public boolean init()
    {
        if (!createFolder()) return false;

        try
        {
            logger = new SynchronizedFileWriter(getLogFile());
        }
        catch (IOException e)
        {
            return false;
        }

        if (!saveThumb()) return false;

        loadSourceInfo();

        return true;
    }

    private boolean createFolder()
    {
        java.util.Date dt = new java.util.Date();

        java.text.SimpleDateFormat sdf = 
                new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        dirName = InkinCrmVideoUploader.getWorkingDirName() + "/temp/" + sdf.format(dt) + "_" + index;

        new File(dirName).mkdirs();
        return true;
    }

    public String getDirName()  { return dirName; }

    private boolean saveThumb()
    {
        thumbPath = dirName + "/thumb.jpg";

        List<String> commandList = new ArrayList<>();

        commandList.add(InkinCrmVideoUploader.getFfmpegPath());

        commandList.add("-ss");
        commandList.add("3");

        commandList.add("-i");
        commandList.add(file.getAbsolutePath());

        commandList.add("-vframes");
        commandList.add("1");

        commandList.add("-q:v");
        commandList.add("2");

        commandList.add(thumbPath);

        Runtime runtime = Runtime.getRuntime();

        try
        {
            String[] commandArray = new String[commandList.size()];
            commandList.toArray(commandArray);
            Process process = runtime.exec(commandArray);
            process.waitFor();

            if (process.exitValue() != 0 || !(new File(thumbPath).exists()))
            {
                return false;
            }
        }
        catch (IOException e)
        {
            String message =
                    "The 'ffmpeg' video converter not found.\n"
                    + "Download it and put to:\n"
                    + InkinCrmVideoUploader.getFfmpegPath();

            log(e.getMessage());
            log(message);
            flushLogger();

            JOptionPane.showMessageDialog(null, message);
            return false;
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    public File getThumbFile()
    {
        return new File(thumbPath);
    }

    private boolean loadSourceInfo()
    {
        List<String> commandList = new ArrayList<>();

        commandList.add(InkinCrmVideoUploader.getFfmpegPath());
        commandList.add("-i");
        commandList.add(file.getAbsolutePath());

        Runtime runtime = Runtime.getRuntime();
        String  output  = "";

        try
        {
            String[] commandArray = new String[commandList.size()];
            commandList.toArray(commandArray);
            Process pr = runtime.exec(commandArray);
            pr.waitFor();
            //int n = pr.exitValue();

            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            String line;// = buf.readLine();

            while ((line = buf.readLine()) != null)
            {
                output += line;
                //System.out.println(line);
            }

            sourceInfo = parseFfmpegInfo(output);
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    private VideoInfo parseFfmpegInfo(String str)
    {
        String  test;
        Pattern pattern;
        Matcher matcher;

        int n = str.indexOf("Duration: ");

        if (n == -1) return null;

        test = str.substring(n);
        pattern = Pattern.compile("^Duration: (\\d+):(\\d+):(\\d+\\.\\d+)");
        matcher = pattern.matcher(test);

        if (!matcher.find()) return null;

        VideoInfo info = new VideoInfo();

        info.duration =
                Integer.parseInt(matcher.group(1)) * 60 * 60
                + Integer.parseInt(matcher.group(2)) * 60
                + Float.parseFloat(matcher.group(3));

        pattern = Pattern.compile("(\\d\\d+)x(\\d+)");
        matcher = pattern.matcher(test);

        if (!matcher.find()) return null;
        info.width  = Short.parseShort(matcher.group(1));
        info.height = Short.parseShort(matcher.group(2));
        info.ratio  = (float) info.width / (float) info.height;

        return info;
    }

    public String getThumbPath()
    {
        return thumbPath;
    }

    public int getIndex()
    {
        return index;
    }

    public VideoInfo getSourceInfo()
    {
        return sourceInfo;
    }

    public void cancelIfRunning()
    {
        
    }

    public byte getAction()
    {
        //  TODO: Allow separate action for each task.
        return InkinCrmVideoUploader.getAction();
    }

    public void setVideoId(long videoId)
    {
        this.videoId = videoId;
    }

    public long getVideoId()
    {
        return videoId;
    }

    public Map<String, Integer> getSelectedBitrates()
    {
        //  TODO: Allow per-video settings, return them here.
        return InkinCrmVideoUploader.getSelectedBitrates();
    }

    public void setStatus(byte status)
    {
        log("Setting status: " + status);   //  TODO: Readable statuses.

        this.status = status;

        for (FileTaskListener listener : listeners)
        {
            listener.onStatusChange(status);
        }
    }

    public byte getStatus()
    {
        return status;
    }

    public void setProgress(String resolution, float progress)
    {
        for (FileTaskListener listener : listeners)
        {
            listener.onProgress(resolution, progress);
        }
    }

    public synchronized void abortIfNotComplete()
    {
        if (status != COMPLETE)
        {
            setStatus(ABORTED);
        }
    }

    public void addListener(FileTaskListener listener)
    {
        listeners.add(listener);
    }

    public File getLogFile()
    {
        if (logFile == null)
        {
            logFile = new File(dirName + "/log.txt");
        }
        return logFile;
    }

    public synchronized boolean log(String str)
    {
        try
        {
            logger.write(str + "\n");
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public OutputStreamWriter getLogger()
    {
        return logger;
    }

    public boolean flushLogger()
    {
        try
        {
            logger.flush();
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }
}
