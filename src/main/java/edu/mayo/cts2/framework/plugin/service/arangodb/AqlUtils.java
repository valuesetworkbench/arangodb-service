package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.model.command.Page;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;
import java.util.Map;

public final class AqlUtils {

    public static final String getLimitOffset(Page page, Map<String,Object> parameters) {
        addLimitOffsetParams(page, parameters);

        return getLimitOffsetAql();
    }

    public static final String getLimitOffsetAql() {
        return " LIMIT @offset,@limit ";
    }

    public static final void addLimitOffsetParams(int limit, int offset, Map<String,Object> parameters) {
        parameters.put("limit", limit);
        parameters.put("offset", offset);
    }

    public static final void addLimitOffsetParams(Page page, Map<String,Object> parameters) {
        parameters.put("limit", page.getMaxToReturn());
        parameters.put("offset", page.getStart());
    }

    public static final AqlQuery getEffectiveDateFilterAql(Date effectiveDateTime, String variable, String resourcePath, String uriPath, String collection) {
        if(StringUtils.isNotBlank(resourcePath)) {
            resourcePath = "." + resourcePath;
        } else {
            resourcePath = "";
        }

        String aql =  "\n" +
                "FILTER " + variable + resourcePath + ".changeableElementGroup.changeDescription.changeDate ==\n" +
                "MAX(\n" +
                "FOR idoc IN " + collection + "\n" +
                "FILTER idoc." + uriPath + " == " + variable + "." + uriPath + "\n" +
                "FILTER idoc" + resourcePath + ".changeableElementGroup.changeDescription.changeDate <= @effectiveDateTime\n" +
                "RETURN idoc" + resourcePath + ".changeableElementGroup.changeDescription.changeDate)\n";

        Map<String,Object> params = Maps.newHashMap();
        params.put("effectiveDateTime", ISODateTimeFormat.dateTime().print(new DateTime(effectiveDateTime)));

        return new AqlQuery(aql, params);
    }

}
