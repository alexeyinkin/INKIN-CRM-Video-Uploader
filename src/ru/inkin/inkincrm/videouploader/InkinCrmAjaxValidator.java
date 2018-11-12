package ru.inkin.inkincrm.videouploader;

import java.io.InputStream;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonObject;

public abstract class InkinCrmAjaxValidator<T> extends NetworkValidator<T>
{
    @Override
    protected boolean isInputStreamValid(InputStream is)
    {
        JsonReader  reader  = Json.createReader(is);
        JsonObject  obj     = reader.readObject();

        reader.close();

        return obj != null && obj.getString("status").equals("ok");
    }
}
