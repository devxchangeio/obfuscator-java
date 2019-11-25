package io.devxchange.obfuscator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devxchange.obfuscator.util.ObfuscatorUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by io.devxchange on 5/2/17.
 */
public class Obfuscator {

    private static final Logger LOG = LoggerFactory.getLogger(Obfuscator.class);
    private static final TypeReference defaultTypeRef = new TypeReference<Map<String, Object>>(){};
    private static final ObjectMapper jsonObjectMapper = new ObjectMapper();
    public static final String XML_PATTERN = "(<(\\w*:)?({0})(\\s.*?)?>)(.*?)(</(\\2?)(\\3)>)";

    public static String obfuscateXmlString(String payload, Map<String,Set<String>> strategyMap) {
        if(StringUtils.isBlank(payload) || strategyMap==null || strategyMap.isEmpty()) {
            return payload;
        } else {
            String fieldNameRegex = ObfuscatorUtil.extractFieldRegex(strategyMap);
            Map<String,String> fieldStrategyMap = ObfuscatorUtil.createFieldStrategyMap(strategyMap);
            String regex = MessageFormat.format(XML_PATTERN, new Object[]{fieldNameRegex});
            Matcher matcher = Pattern.compile(regex).matcher(payload);
            StringBuffer processedPayload = new StringBuffer();
            while(matcher.find()) {
                String startTag = matcher.group(1);
                String namespace = matcher.group(2); // Are we really stripping out the namespacing???
                String fieldName = matcher.group(3);
                String bodyText = matcher.group(5);
                String endTag = matcher.group(6);
                matcher.appendReplacement(processedPayload, startTag + ObfuscatorUtil.obfuscate(fieldStrategyMap.get(fieldName),bodyText) + endTag);
            }
            matcher.appendTail(processedPayload);
            return processedPayload.toString();
        }
    }
    public static String obfuscateJsonString(String payload, Map<String, Set<String>> strategyMap) {
        if(StringUtils.isBlank(payload) || strategyMap==null || strategyMap.isEmpty()) {
            return payload;
        } else {
            Map<String, Object> jsonMap;
            try {
                jsonMap = jsonObjectMapper.readValue(payload, defaultTypeRef);
            } catch(IOException ioEx) {
                LOG.error("Failed to parse json payload. Returning payload unaltered.",ioEx);
                return payload;
            }
            if(jsonMap!=null) {
                Map<String,String> fieldStrategyMap = ObfuscatorUtil.createFieldStrategyMap(strategyMap);
                ObfuscatorUtil.processJsonMap(jsonMap,fieldStrategyMap);
                // Convert back map to json
                String jsonResult = "";
                try {
                    jsonResult = jsonObjectMapper.writeValueAsString(jsonMap);
                } catch (IOException e) {
                    LOG.warn("cannot create json from Map" + e.getMessage());
                }
                return jsonResult;
            } else {
                LOG.error("JSON object mapping of payload returned null. Returning original payload.");
                return payload;
            }
        }
    }
}
