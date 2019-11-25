package io.devxchange.obfuscator.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by io.devxchange on 5/2/17.
 */
public class ObfuscatorUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ObfuscatorUtil.class);

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

    public static Map<String,String> createFieldStrategyMap(Map<String, Set<String>> strategyFieldMap) {
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
    public static void processJsonMap(Map<String, Object> jsonMap, Map<String,String> fieldStrategyMap) {
        jsonMap.forEach((fieldName,value) -> {
            if(fieldStrategyMap.containsKey(fieldName)) {
                jsonMap.put(fieldName,processJsonField(fieldName,value,fieldStrategyMap));
            } else if(value instanceof Map) {
                processJsonMap((Map<String,Object>)value,fieldStrategyMap);
            } else if(value instanceof List) {
                ((List)value).forEach(listVal -> {
                    if(listVal instanceof Map) {
                        processJsonMap((Map<String,Object>)listVal,fieldStrategyMap);
                    }
                });
            }
        });
    }

    public static Object processJsonField(String fieldName, Object value, Map<String,String> fieldStrategyMap) {
        if(value==null) {
            return value;
        } else if(value instanceof String) {
            return obfuscate(fieldStrategyMap.get(fieldName), (String) value);
        } else if(value instanceof Number) {
            return obfuscate(fieldStrategyMap.get(fieldName), String.valueOf(value));
        } else if(value instanceof String[]) {
            return Arrays.stream((String[]) value).map(arrayVal -> obfuscate(fieldStrategyMap.get(fieldName), arrayVal)).toArray(String[]::new);
        } else if(value instanceof List) {
            return ((List<Object>) value)
                    .stream()
                    .map(listVal -> processJsonField(fieldName, listVal, fieldStrategyMap))
                    .collect(Collectors.toList());
        } else {
            LOG.warn("Unknown JSON structure encountered.  Fix your code: {}",value.getClass().getName());
            return value;
        }
    }
    public static String extractFieldRegex(Map<String,Set<String>> fieldMap) {
        return fieldMap.entrySet().stream().map(Map.Entry::getValue).distinct().map(value -> String.join("|",value)).collect(Collectors.joining("|"));
    }
}
