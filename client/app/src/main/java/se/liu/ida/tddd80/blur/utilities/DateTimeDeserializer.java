package se.liu.ida.tddd80.blur.utilities;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

/**
 * Used by Gson to serialize/deserialize Joda DateTime objects according to the format returned by
 * server. E.g. time_created would look like:
 * "time_created": {"datetime: "2015-11-27T00:29:06.839600+02:00"} where datetime follows ISO 8601
 */
public final class DateTimeDeserializer implements JsonDeserializer<DateTime>, JsonSerializer<DateTime>
{
    static final org.joda.time.format.DateTimeFormatter DATE_TIME_FORMATTER =
            ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    @Override
    public DateTime deserialize(final JsonElement je, final Type type,
                                final JsonDeserializationContext jdc) throws JsonParseException
    {
        return je.toString().length() == 0 ? null :DATE_TIME_FORMATTER
                .parseDateTime(je.getAsJsonObject().get("datetime").getAsString());
    }

    @Override
    public JsonElement serialize(final DateTime src, final Type typeOfSrc,
                                 final JsonSerializationContext context)
    {
        return new JsonPrimitive(src == null ? StringUtils.EMPTY :DATE_TIME_FORMATTER.print(src));
    }
}