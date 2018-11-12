package ru.inkin.inkincrm.videouploader;

import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.function.Supplier;

public class TokenValidator extends InkinCrmAjaxValidator<String>
{
    Supplier<String>    hostSupplier;

    public TokenValidator()
    {
        hostSupplier = () -> InkinCrmVideoUploader.getServerUrl();
    }

    public TokenValidator(Supplier<String> hostSupplier)
    {
        this.hostSupplier = hostSupplier;
    }


    @Override
    protected String getValidationUrl(String token) throws Exception
    {
        if (token.equals("")) throw new Exception();

        //String fixedUrl = InkinCrmVideoUploader.getServerUrl();
        String fixedUrl = hostSupplier.get();

        URL u = new URL(fixedUrl);  // Throws exception if malformed.

        return fixedUrl + "/inkincrm-ajax/Core/validateOperationToken";
    }

    @Override
    protected Map<String, String> getPostParams(String token)
    {
        Map<String, String> result = new HashMap<>();

        result.put("inkincrm-no-device", "1");
        result.put("key", token);
        return result;
    }
}
