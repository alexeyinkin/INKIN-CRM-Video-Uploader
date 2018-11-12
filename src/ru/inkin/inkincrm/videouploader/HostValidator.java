package ru.inkin.inkincrm.videouploader;

import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.net.MalformedURLException;

public class HostValidator extends InkinCrmAjaxValidator<String>
{
    @Override
    protected String getValidationUrl(String obj) throws MalformedURLException
    {
        String fixedUrl = InkinCrmVideoUploader.fixServerUrl(obj);

        URL u = new URL(fixedUrl);  // Throws exception if malformed.

        return fixedUrl + "/inkincrm-ajax/Core/probe"; //?inkincrm-no-device=1";
    }

    @Override
    protected Map<String, String> getPostParams(String url)
    {
        Map<String, String> result = new HashMap<>();
        result.put("inkincrm-no-device", "1");
        return result;
    }
}
