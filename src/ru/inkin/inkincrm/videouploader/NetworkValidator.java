package ru.inkin.inkincrm.videouploader;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

//  TODO: Fix: Why validation of host takes seconds, but validation of token
//  takes fractions of a second?

public abstract class NetworkValidator<T>
{
    public final byte VALID         = 0;
    public final byte PENDING       = 1;
    public final byte INVALID       = 2;

    private T       obj;
    private byte    state = INVALID;

    private List<NetworkValidatorListener<T>> listeners = new ArrayList<NetworkValidatorListener<T>>();

    /**
     * Validates the downloaded content itself.
     * @param is
     * @return 
     */
    abstract protected boolean isInputStreamValid(InputStream is);
    abstract protected String getValidationUrl(T obj) throws Exception;

    public void addListener(NetworkValidatorListener listener)
    {
        listeners.add(listener);
    }

    public void validate(T obj)
    {
        if (!tryToUseCacheOrPrepareValidation(obj))
        {
            spawnValidation();
        }
    }

    public boolean validateSync(T obj)
    {
        if (tryToUseCacheOrPrepareValidation(obj))
        {
            return state == VALID;
        }

        return isValidSync(obj);
    }

    protected boolean canReturnCached(T obj)
    {
        return this.obj != null && this.obj.equals(obj);
    }

    private boolean tryToUseCacheOrPrepareValidation(T obj)
    {
        if (state != PENDING && canReturnCached(obj)) return true;

        this.obj = obj;
        state = PENDING;

        for (NetworkValidatorListener listener : listeners)
        {
            listener.enteredPending(obj);
        }

        return false;
    }

    private void spawnValidation()
    {
        Runnable runnable = new Runnable() {
            public void run() {
                if (isValidSync(obj))
                {
                    resolveValid();
                }
                else
                {
                    resolveInvalid();
                }
            }
        };

        new Thread(runnable).start();
    }

    protected Map<String, String> getPostParams(T obj)
    {
        return new HashMap<>();
    }

    private String getQueryString(T obj)
    {
        Map<String, String> params = getPostParams(obj);
        return InkinCrmVideoUploader.getQueryString(params);
//        String              charset         = java.nio.charset.StandardCharsets.UTF_8.name();
//        Map<String, String> params          = getPostParams(obj);
//        List<String>        encodedPairs    = new ArrayList<>();
//
//        for (String key : params.keySet())
//        {
//            try
//            {
//                encodedPairs.add(
//                        URLEncoder.encode(key, charset)
//                        + "="
//                        + URLEncoder.encode(params.get(key), charset));
//            }
//            catch (Exception e) {}
//        }
//
//        return String.join("&", encodedPairs);
    }

    /**
     * Synchronous call to check if the url is valid. Hangs until returned.
     * @param obj
     * @return 
     */
    public boolean isValidSync(T obj)
    {
        try
        {
            String          charset     = java.nio.charset.StandardCharsets.UTF_8.name();
            URL             url         = new URL(getValidationUrl(obj));
            URLConnection   connection  = url.openConnection();

            connection.setDoOutput(true); // Triggers POST.
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
            connection.getOutputStream().write(getQueryString(obj).getBytes(charset));

            return isInputStreamValid(connection.getInputStream());
            //...
            //return isInputStreamValid(new URL(fixUrlIfNot(obj)).openStream());
        }
        catch (Exception e)
        {
            return false;
        }
    }

//    protected String fixUrlIfNot(String url) throws MalformedURLException
//    {
//        return url;
//    }

    private void resolveValid()
    {
        state = VALID;

        for (NetworkValidatorListener listener : listeners)
        {
            listener.resolvedValid(obj);
        }
    }

    private void resolveInvalid()
    {
        state = INVALID;

        for (NetworkValidatorListener listener : listeners)
        {
            listener.resolvedInvalid(obj);
        }
    }
}
