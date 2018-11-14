package ru.inkin.inkincrm.videouploader;

import java.awt.Image;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.json.*;
import javax.json.stream.JsonParsingException;

public class InkinCrmVideoUploader
{
    public final static String AUDIO_ONLY_STRING = "🔊 Only";

    private static final String[] RESOLUTIONS = {
        "1080",
        "720",
        "480",
        "360",
        "240",
        "120"//,
        //audioOnlyString
    };

    private static File         workingDir;
    private static String       workingDirName;
    private static MainWindow   mainWindow;
    private static Map<String, String> config;

    private static final SortedMap<String, BitratePreset> bitratePresets = new TreeMap<>();
    private static final SortedMap<String, FileTask>      fileTasks = new TreeMap<>();
    //private static JsonObject   config;

    private static Image        appIconImage;

    private static HostValidator    hostValidator;
    private static TokenValidator   tokenValidator;

    private static InkinCrmVideoUploaderLineProcessor    lineProcessor;

    public static final byte IDLE           = 0;
    public static final byte IN_PROGRESS    = 1;

    private static void createAndShowGUI()
    {
        mainWindow = new MainWindow();
        mainWindow.setResolutions(RESOLUTIONS);
        mainWindow.setBitratePresets(bitratePresets);
        mainWindow.initAndShow();
        mainWindow.setVisible(true);
    }

    private static void testVideoConverter()
    {
        FileTask fileTask = new FileTask();
        fileTask.setFile(new File("d:/test short video.mov"));
        fileTask.init();

        VideoVariantSettings vs = new VideoVariantSettings();
        //vs.setWidth((short) 1280);
        vs.setHeight((short) 720);
        vs.setBitsPerSecond(100000);

        VideoVariantConverter vc = new VideoVariantConverter();
        vc.setFileTask(fileTask);
        vc.setVariantSettings(vs);
        vc.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        initWorkingDir();
        loadConfig();
        initBitratePresets();
        //JOptionPane.showMessageDialog(null, workingDirName);

        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                //testVideoConverter();
                createAndShowGUI();
            }
        } );
    }

    private static void initWorkingDir()
    {
        try
        {
            URL     jarUrl  = InkinCrmVideoUploader.class.getProtectionDomain().getCodeSource().getLocation();
            File    jarFile = new File(jarUrl.toURI());

            workingDir = jarFile.getParentFile();
            workingDirName = workingDir.getAbsolutePath();
        }
        catch (Exception e) {}
    }

    private static void initBitratePresets()
    {
        String          title;
        BitratePreset   preset;
        long            base;

        title = "Sports Video";
        preset = new BitratePreset(title);
        base = 3240000;
        preset.addBitrate("1080",       (int) base);
        preset.addBitrate("720",        (int) (base * 720 / 1080));
        preset.addBitrate("480",        (int) (base * 480 / 1080));
        preset.addBitrate("360",        (int) (base * 360 / 1080));
        preset.addBitrate("240",        (int) (base * 240 / 1080));
        preset.addBitrate("120",        (int) (base * 120 / 1080));
        preset.addBitrate(AUDIO_ONLY_STRING, 0);
        bitratePresets.put(title, preset);

        title = "Slides + Speaker";
        preset = new BitratePreset(title);
        base = 405000;
        preset.addBitrate("1080",       (int) base);
        preset.addBitrate("720",        (int) (base * 720 / 1080));
        preset.addBitrate("480",        (int) (base * 480 / 1080));
        preset.addBitrate("360",        (int) (base * 360 / 1080));
        preset.addBitrate("240",        (int) (base * 240 / 1080));
        preset.addBitrate("120",        (int) (base * 120 / 1080));
        preset.addBitrate(AUDIO_ONLY_STRING, 0);
        bitratePresets.put(title, preset);
    }

    public static String getWorkingDirName()
    {
        if (workingDir == null)
        {
            initWorkingDir();
        }

        return workingDirName;
    }

    public static String getFfmpegPath()
    {
        return workingDirName + "/tools/ffmpeg";
    }

    public static boolean isFileTaskDuplicate(FileTask fileTask)
    {
        return fileTasks.containsKey(fileTask.getFile().getAbsolutePath());
    }

    public static boolean addFileTask(FileTask fileTask)
    {
        if (!isFileTaskDuplicate(fileTask) && fileTask.init())
        {
            fileTasks.put(fileTask.getFile().getAbsolutePath(), fileTask);
            mainWindow.addFileTask(fileTask);
            return true;
        }

        return false;
    }

    public static void removeFileTask(FileTask fileTask)
    {
        fileTask.cancelIfRunning();
        fileTasks.remove(fileTask.getFile().getAbsolutePath());
        mainWindow.removeFileTask(fileTask);
    }

    public static SortedMap<String, FileTask> getFileTasks()
    {
        return fileTasks;
    }

    public static String getDisplayFileSize(long bytes)
    {
        String  units[] = {"TB", "GB", "MB", "kB", "B"};
        double  n       = bytes;

        for (char i = (char) units.length; --i >= 0; )
        {
            if (n > 1024 && i > 0)
            {
                n /= 1024;
            }
            else
            {
                if (n - Math.floor(n) == 0)
                {
                    return n + " " + units[i];
                }

                if (n - Math.floor(n) > 0.9)
                {
                    return Math.ceil(n) + " " + units[i];
                }

                return (int) Math.floor(n) + "." + (int) Math.ceil((n - Math.floor(n)) * 10) + " " + units[i];
            }
        }

        return "";  //  Never happens.
    }

    public static String getDisplayDuration(float seconds)
    {
        String result;

        if (seconds < 3600)
        {
            result = String.valueOf((int) Math.floor(seconds / 60)); //  min
        }
        else
        {
            result = (short) Math.floor(seconds / 3600) + ":" +                                 //  h
                String.format("%2s", (short) Math.floor(seconds / 60) % 60).replace(' ', '0');  //  min
        }

        result += ":" + String.format("%2s", (short) Math.floor(seconds % 60)).replace(' ', '0');
        return result;
    }

    public static String fixServerUrl(String sourceUrl)
    {
        if (sourceUrl.equals("")) return "";

        Pattern pattern = Pattern.compile("^(.*://)");
        Matcher matcher = pattern.matcher(sourceUrl);

        if (!matcher.find())
        {
            sourceUrl = "https://" + sourceUrl;
        }
        else
        {
            sourceUrl = "https://" + sourceUrl.substring(matcher.group(0).length());
        }

        sourceUrl = sourceUrl.replaceAll("/$", "");
        return sourceUrl;
    }

    public static Image getAppIcon()
    {
        if (appIconImage == null)
        {
            java.net.URL iconUrl = InkinCrmVideoUploader.class.getResource("/resources/icon.png");
            appIconImage = new ImageIcon(iconUrl).getImage();
        }

        return appIconImage;
    }

    public static HostValidator getHostValidator()
    {
        if (hostValidator == null) hostValidator = new HostValidator();
        return hostValidator;
    }

    public static TokenValidator getTokenValidator()
    {
        if (tokenValidator == null)
        {
            tokenValidator = new TokenValidator();
        }

        return tokenValidator;
    }

    private static void loadConfig()
    {
        JsonObject obj;

        try
        {
            InputStream is      = new FileInputStream(workingDirName + "/config.json");
            JsonReader  reader  = Json.createReader(is);

            obj = reader.readObject();
            reader.close();
        }
        catch (Exception e)
        {
            obj = Json.createObjectBuilder().build();
        }

        config = new HashMap<>();
        config.put("host",  obj.getString("host", ""));
        config.put("token", obj.getString("token", ""));
    }

//    private static JsonObject getConfig()
//    {
//        return config;
//    }

    public static boolean isConfigOk()
    {
        //return getHostValidator().validateSync(config.getString("host", ""));
        return getTokenValidator().validateSync(getApiToken());
    }

    public static void setServerUrl(String url)
    {
        config.put("host", fixServerUrl(url));
        mainWindow.updateServerLabel();
    }

    public static String getServerUrl()
    {
        return config.get("host");
    }

    public static void setApiToken(String token)
    {
        config.put("token", token);
    }

    public static String getApiToken()
    {
        return config.get("token");
    }

    private static JsonObject configToJson()
    {
        return Json.createObjectBuilder()
                .add("host", getServerUrl())
                .add("token", getApiToken())
                .build();
    }

    public static void saveConfig()
    {
        try
        {            
            OutputStream    os      = new FileOutputStream(workingDirName + "/config.json");
            JsonWriter      writer  = Json.createWriter(os);

            writer.writeObject(configToJson());
            writer.close();
        }
        catch (Exception e) {}
    }

    public static void startQueue()
    {
        //mainWindow.setInProgressMode();

        //JOptionPane.showMessageDialog(null, "Start!");
        
        getLineProcessor().start();
    }

    public static void stopQueue()
    {
        //mainWindow.setIdleMode();
        for (String filePath : fileTasks.keySet())
        {
            FileTask fileTask = fileTasks.get(filePath);
            fileTask.abortIfNotComplete();
        }

        getLineProcessor().stop();
    }

    public static LineProcessor getLineProcessor()
    {
        if (lineProcessor == null)
        {
            lineProcessor = new InkinCrmVideoUploaderLineProcessor();
            mainWindow.setLineProcessor(lineProcessor);
        }

        return lineProcessor;
    }

    public static byte getAction()
    {
        return mainWindow.getAction();
    }

    public static long getReplaceVideoId()
    {
        return mainWindow.getReplaceVideoId();
    }

    public static JsonObject applyServerCommand(
            JsonObject          command,
            OutputStreamWriter  logger)
    {
        return applyServerCommands(
                Json.createArrayBuilder().add(command).build(),
                logger);
    }

    public static JsonObject applyServerCommands(
            JsonArray           commands,
            OutputStreamWriter  logger)
    {
        try
        {
            String          charset     = java.nio.charset.StandardCharsets.UTF_8.name();
            Map<String, String> params  = new HashMap<>();

            ByteArrayOutputStream   baos    = new ByteArrayOutputStream();
            JsonWriter              writer  = Json.createWriter(baos);
            writer.writeArray(commands);
            writer.close();

            params.put("commands",  baos.toString(charset));
            params.put("return",    "*");

            return callServerMethod(
                    "Core", "applyCommands",
                    params,
                    null,
                    logger);
        }
        catch (UnsupportedEncodingException e)
        {
            return Json.createObjectBuilder().add("status", "error").build();
        }
        
//        try
//        {
//            String          charset     = java.nio.charset.StandardCharsets.UTF_8.name();
//            URL             url         = new URL(getServerUrl() + "/inkincrm-ajax/Core/applyCommands");
//            URLConnection   connection  = url.openConnection();
//            Map<String, String> params  = new HashMap<>();
//
//            ByteArrayOutputStream   baos    = new ByteArrayOutputStream();
//            JsonWriter              writer  = Json.createWriter(baos);
//            writer.writeArray(commands);
//            writer.close();
//
//            params.put("commands",              baos.toString(charset));
//            params.put("token",                 getApiToken());
//            params.put("inkincrm-no-device",    "1");
//            params.put("return",                "*");
//
//            connection.setDoOutput(true); // Triggers POST.
//            connection.setRequestProperty("Accept-Charset", charset);
//            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
//            connection.getOutputStream().write(getQueryString(params).getBytes(charset));
//
//            JsonReader reader = Json.createReader(connection.getInputStream());
//            return reader.readObject();
//        }
//        catch (Exception e)
//        {
//            return Json.createObjectBuilder().add("status", "error").build();
//        }
    }

    public static String getQueryString(Map<String, String> params)
    {
        String          charset         = java.nio.charset.StandardCharsets.UTF_8.name();
        List<String>    encodedPairs    = new ArrayList<>();

        for (String key : params.keySet())
        {
            try
            {
                encodedPairs.add(
                        URLEncoder.encode(key, charset)
                        + "="
                        + URLEncoder.encode(params.get(key), charset));
            }
            catch (Exception e) {}
        }

        return String.join("&", encodedPairs);
    }

    public static JsonObject callServerMethod(
            String              className,
            String              method,
            Map<String, String> params,
            Map<String, File>   filesToUpload,
            OutputStreamWriter  logger)
    {
        if (logger == null)
        {
            logger = new OutputStreamWriter(System.out);
        }

        try
        {
            String charset = java.nio.charset.StandardCharsets.UTF_8.name();
            URL url = new URL(
                    getServerUrl() +
                    "/inkincrm-ajax/" + className + "/" + method);

            logger.write("Calling " + url.toString() + "\n");

            params.put("inkincrm-no-device",    "1");
            params.put("token",                 getApiToken());

            URLConnection   connection  = url.openConnection();
            String          CRLF        = "\r\n"; // Line separator required by multipart/form-data.
            String          boundary    = "inkincrm-upload-boundary-that-will-not-appear-in-the-files-to-upload";
            //String          boundaryLine= "--" + boundary + "--" + CRLF;

            connection.setDoOutput(true);   //  Triggers POST.
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=\"" + boundary + "\"");

            //  Based on the example:
            //  https://stackoverflow.com/questions/2469451/upload-files-from-java-client-to-a-http-server

            try (
                OutputStream output = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
            ) {
                if (params != null)
                {
                    for (String key : params.keySet())  // Send normal params.
                    {
                        writer.append("--" + boundary + CRLF);
                        writer.append("Content-Disposition: form-data; name=\"" + key + "\"").append(CRLF);
                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                        writer.append(CRLF).append(params.get(key)).append(CRLF).flush();
                    }
                }

                if (filesToUpload != null)
                {
                    for (String key : filesToUpload.keySet())  // Send binary files.
                    {
                        File file = filesToUpload.get(key);

                        writer.append("--" + boundary + CRLF);
                        writer.append("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + file.getName() + "\"").append(CRLF);
                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName())).append(CRLF);
                        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                        writer.append(CRLF).flush();
                        Files.copy(file.toPath(), output);
                        output.flush(); // Important before continuing with writer!
                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
                    }
                }

                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append(CRLF).flush();
            }

            Scanner s = new Scanner(connection.getInputStream()).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";

            JsonReader reader = Json.createReader(new StringReader(response));
            JsonObject jsonResponse = null;

            try
            {
                jsonResponse = reader.readObject();

                if (!jsonResponse.getString("status").equals("ok"))
                {
                    StringWriter writer = new StringWriter();
                    Json.createWriter(writer).write(jsonResponse);
                    String toLog = "Received not OK: " + writer.toString();

                    logger.write(toLog + "\n");
                }
            }
            catch (JsonParsingException e)
            {
                logger.write("Error parsing response JSON: " + response + "\n");
            }
            finally
            {
                reader.close();
            }

            return jsonResponse;
            //JsonReader reader = Json.createReader(connection.getInputStream());
            //return reader.readObject();
        }
        catch (Exception e)
        {
            return Json.createObjectBuilder().add("status", "error").build();
        }
    }

    public static Map<String, Integer> getSelectedBitrates()
    {
        return mainWindow.getSelectedBitrates();
    }
}
