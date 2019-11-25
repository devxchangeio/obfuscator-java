package io.devxchange.obfuscator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObfuscatorTest {

    @Test
    public void testSimpleJson() {
        Map<String, Set<String>> obfuscatorMap= new HashMap<>();
        Set<String> items = new HashSet<>();
        items.add("name");
        items.add("salary");
        items.add("age");
        obfuscatorMap.put("Obfuscate", items);
        String response = Obfuscator.obfuscateJsonString(simpleJson,obfuscatorMap);
        System.out.println(response);
    }
    @Test
    public void testJsonList() {
        Map<String, Set<String>> obfuscatorMap= new HashMap<>();
        Set<String> items = new HashSet<>();
        items.add("name");
        items.add("salary");
        items.add("age");
        obfuscatorMap.put("Obfuscate", items);
        String response = Obfuscator.obfuscateJsonString(jsonList,obfuscatorMap);
        System.out.println(response);
    }

    @Test
    public void testSimpleXML() {
        Map<String, Set<String>> obfuscatorMap= new HashMap<>();
        Set<String> items = new HashSet<>();
        items.add("name");
        items.add("salary");
        items.add("age");
        obfuscatorMap.put("Obfuscate", items);
        String response = Obfuscator.obfuscateXmlString(simpleXML,obfuscatorMap);
        System.out.println(response);
    }

    @Test
    public void testComplexXML() {
        Map<String, Set<String>> obfuscatorMap= new HashMap<>();
        Set<String> items = new HashSet<>();
        items.add("name");
        items.add("salary");
        items.add("age");
        obfuscatorMap.put("Obfuscate", items);
        String response = Obfuscator.obfuscateXmlString(complexXML,obfuscatorMap);
        System.out.println(response);
    }

    private String simpleJson = "{\"id\":1,\"name\":\"karthik\",\"age\":29,\"salary\":115000}";
    private String jsonList = "{\"user\":[{\"id\":1,\"name\":\"karthik\",\"age\":29,\"salary\":115000},{\"id\":2,\"name\":\"Karthikeyan Sadayamuthu\",\"age\":30,\"salary\":500000},{\"id\":3,\"name\":\"Sadayamuthu\",\"age\":30,\"salary\":500000},{\"id\":4,\"name\":\"Vinodh Kannan Sadayamuthu\",\"age\":30,\"salary\":500000}]}";
    private String simpleXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root>\n" +
            "  <id>1</id>\n" +
            "  <name>karthik</name>\n" +
            "  <age>29</age>\n" +
            "  <salary>115000</salary>\n" +
            "</root>";
    private String complexXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root>\n" +
            "  <user>\n" +
            "    <id>1</id>\n" +
            "    <name>karthik</name>\n" +
            "    <age>29</age>\n" +
            "    <salary>115000</salary>\n" +
            "  </user>\n" +
            "  <user>\n" +
            "    <id>2</id>\n" +
            "    <name>Karthikeyan Sadayamuthu</name>\n" +
            "    <age>30</age>\n" +
            "    <salary>500000</salary>\n" +
            "  </user>\n" +
            "  <user>\n" +
            "    <id>3</id>\n" +
            "    <name>Sadayamuthu</name>\n" +
            "    <age>30</age>\n" +
            "    <salary>500000</salary>\n" +
            "  </user>\n" +
            "  <user>\n" +
            "    <id>4</id>\n" +
            "    <name>Vinodh Kannan Sadayamuthu</name>\n" +
            "    <age>30</age>\n" +
            "    <salary>500000</salary>\n" +
            "  </user>\n" +
            "</root>";

}
