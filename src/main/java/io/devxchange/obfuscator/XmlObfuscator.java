package io.devxchange.obfuscator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by io.devxchange on 5/2/17.
 */
public class XmlObfuscator {

    private static final Logger LOG = LoggerFactory.getLogger(XmlObfuscator.class);


    public static final String XML_PATTERN = "(<(\\w*:)?({0})(\\s.*?)?>)(.*?)(</(\\2?)(\\3)>)";
    public static final String MASK_FIXED = "********";

    public static final String STRATEGY_MD5 = "md5";
    public static final String STRATEGY_SHA256 = "sha256";
    public static final String STRATEGY_MASK_FIXED = "fixedMask";
    public static final String STRATEGY_MASK_MATCHED = "matchedMask";
    public static final String STRATEGY_NONE = "none";

    public static final List<String> STRATEGIES = Collections.unmodifiableList(new ArrayList<String>() {{
        add(STRATEGY_MD5);
        add(STRATEGY_SHA256);
        add(STRATEGY_MASK_FIXED);
        add(STRATEGY_MASK_MATCHED);
        add(STRATEGY_NONE);
    }});

    public static String obfuscate(String strategy, String value) {
        if(StringUtils.isBlank(strategy)) {
            return value;
        } else if(STRATEGIES.contains(strategy)) {
            switch(strategy) {
                case STRATEGY_MD5:
                    return applyHashMd5(value);
                case STRATEGY_SHA256:
                    return applyHashSha256(value);
                case STRATEGY_MASK_MATCHED:
                    return  applyMatchedMask(value);
                case  STRATEGY_NONE:
                    return value;
                default:
                    return applyFixedMask(value);
            }
        } else {
           LOG.warn("Unknown obfuscation strategy: {}",strategy);
            return applyFixedMask(value);
        }
    }

    public static String extractFieldRegex(Map<String,Set<String>> fieldMap) {
        return fieldMap.entrySet().stream().map(Map.Entry::getValue).distinct().map(value -> String.join("|",value)).collect(Collectors.joining("|"));
    }

    public static Map<String,String> createFieldStrategyMap(Map<String,Set<String>> strategyFieldMap) {
        Map<String,String> obfuscateMap = new HashMap<>();
        strategyFieldMap.forEach((key,values) -> {
            values.forEach(value -> {
                if(obfuscateMap.containsKey(value)) {
                    LOG.warn("Obfuscation strategy already declared for field '{}'.",value);
                }
                obfuscateMap.put(value,key);
            });
        });
        return obfuscateMap;
    }

    public static String applyFixedMask(String target) {
        return MASK_FIXED;
    }

    public static String applyMatchedMask(String target) {
        return StringUtils.isBlank(target) ? target : StringUtils.leftPad("",target.length(),"*");
    }

    public static String applyHashMd5(String target) {
        if(StringUtils.isBlank(target)) {
            return target;
        } else {
            try {
                return DigestUtils.md5Hex(target.getBytes(StandardCharsets.UTF_8));
            } catch(RuntimeException rtEx) {
                LOG.error("Unknown error while trying to hash text. Returning default fixed mask string.", rtEx);
                return applyFixedMask(target);
            }
        }
    }

    public static String applyHashSha256(String target) {
        if(StringUtils.isBlank(target)) {
            return target;
        } else {
            try {
                return DigestUtils.sha256Hex(target.getBytes(StandardCharsets.UTF_8));
            } catch(RuntimeException rtEx) {
                LOG.error("Unknown error while trying to hash text. Returning default fixed mask string.", rtEx);
                return applyFixedMask(target);
            }
        }
    }

    public static String obfuscateXmlString(String payload, Map<String,Set<String>> strategyMap) {
        if(StringUtils.isBlank(payload) || strategyMap==null || strategyMap.isEmpty()) {
            return payload;
        } else {
            String fieldNameRegex = extractFieldRegex(strategyMap);
            Map<String,String> fieldStrategyMap = createFieldStrategyMap(strategyMap);
            String regex = MessageFormat.format(XML_PATTERN, new Object[]{fieldNameRegex});
            Matcher matcher = Pattern.compile(regex).matcher(payload);
            StringBuffer processedPayload = new StringBuffer();
            while(matcher.find()) {
                String startTag = matcher.group(1);
                String namespace = matcher.group(2); // Are we really stripping out the namespacing???
                String fieldName = matcher.group(3);
                String bodyText = matcher.group(5);
                String endTag = matcher.group(6);
                matcher.appendReplacement(processedPayload, startTag + obfuscate(fieldStrategyMap.get(fieldName),bodyText) + endTag);
            }
            matcher.appendTail(processedPayload);
            return processedPayload.toString();
        }
    }

}
