package neutron;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface JSON {
    static ObjectMapper mapper = new ObjectMapper();

    static String stringValue(JSON j) {
        try {
            var s = mapper.writeValueAsString(j);
            if (Neutron.isVerbose()) System.out.println(s);
            return s;
        } catch (Exception e) {
            System.err.println("failed to parse JSON, class might be empty or non-public fields");
            return "{error:\"failed to parse JSON, java class might be empty or non-public fields\"}";
        }
    };

    static <T> T fromString(String s, Class<T> cls) {
        try {
            if (Neutron.isVerbose()) System.out.println(s);
            return mapper.readValue(s, cls);
        } catch (Exception e) {
            System.err.println("failed to parse JSON " + e.toString());
            return null;
        }
    }

    static String create(String key, String value, String... KV) {
        if (KV.length % 2 != 0)
            throw new IllegalArgumentException("New JSON needs even number of arguments (key-value pairs)");
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"").append(key).append("\":\"").append(value).append("\"");
        for (int i = 0; i < KV.length; i += 2) {
            sb.append(",");
            sb.append("\"").append(KV[i]).append("\":");
            sb.append("\"").append(KV[i + 1]).append("\"");
        }
        sb.append("}");
        var s = sb.toString();
        if (Neutron.isVerbose()) System.out.println(s);
        return s;
    }
}
