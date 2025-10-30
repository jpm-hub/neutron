package neutron;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface JSON {
    final static ObjectMapper mapper = new ObjectMapper();

    static String stringify(JSON j) {
        try {
            var s = mapper.writeValueAsString(j);
            if (Neutron.isVerbose())
                System.out.println("[NEUTRON-VERBOSE-JSON] " + s);
            if (s == null)
                throw new Exception();
            return s;
        } catch (Exception e) {
            System.err.println("failed to parse JSON, class might be empty or non-public fields");
            if (Neutron.isVerbose())
                e.printStackTrace();
            return "{error:\"failed to parse JSON, java class might be empty or non-public fields\"}";
        }
    };

    static <T> T fromString(String s, Class<T> cls) {
        try {
            if (Neutron.isVerbose())
                System.out.println("[NEUTRON-VERBOSE-JSON] " + s);
            return mapper.readValue(s, cls);
        } catch (Exception e) {
            System.err.println("failed to parse JSON " + e.toString());
            return null;
        }
    }

    static String list(Object first, Object... values) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(first);
        for (int i = 0; i < values.length; i++) {
            sb.append(",");
            if (values[i] instanceof String &&
                    !(values[i].toString().startsWith("{\"") && values[i].toString().endsWith("}")) &&
                    !(values[i].toString().startsWith("[") && values[i].toString().endsWith("]"))) {
                sb.append("\"").append(values[i]).append("\"");
            } else {
                sb.append(values[i]);
            }
        }
        sb.append("]");
        var s = sb.toString();
        if (Neutron.isVerbose())
            System.out.println("[NEUTRON-VERBOSE-JSON] " + s);
        return s;
    }

    static String KV(String key, Object value, Object... KV) {
        if (KV.length % 2 != 0)
            throw new IllegalArgumentException("JSON.KV needs even number of arguments (key-value pairs)");
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"").append(key).append("\":");
        if (value instanceof String) {
            sb.append("\"").append(value).append("\"");
        } else {
            sb.append(value);
        }
        for (int i = 0; i < KV.length; i += 2) {
            sb.append(",");
            sb.append("\"").append(KV[i]).append("\":");
            if (KV[i + 1] instanceof String &&
                    !(KV[i + 1].toString().startsWith("{\"") && KV[i + 1].toString().endsWith("}")) &&
                    !(KV[i + 1].toString().startsWith("[") && KV[i + 1].toString().endsWith("]"))) {
                sb.append("\"").append(KV[i + 1]).append("\"");
            } else {
                sb.append(KV[i + 1]);
            }
        }
        sb.append("}");
        var s = sb.toString();
        if (Neutron.isVerbose())
            System.out.println("[NEUTRON-VERBOSE-JSON] " + s);
        return s;
    }
    static void printJSON(JSON j) {
        String s = stringify(j);
        try {
            Object json = mapper.readValue(s, Object.class);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println(prettyJson);
        } catch (Exception e) {
            System.err.println("failed to pretty print JSON");
            if (Neutron.isVerbose())
                e.printStackTrace();
            System.out.println(s);
        }
    }
    static void printJSON(String s) {
        try {
            Object json = mapper.readValue(s, Object.class);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println(prettyJson);
        } catch (Exception e) {
            System.err.println("failed to pretty print JSON");
            if (Neutron.isVerbose())
                e.printStackTrace();
            System.out.println(s);
        }
    }
    //
    // private static void print(String s) {
    //     boolean isKey = true;
    //     boolean inquotes = false;
    //     boolean inArray = false;
    //     int i = 0;
    //     int ind = 0;
    //     for (byte b : s.getBytes()) {
    //         switch (b) {
    //             case '{':
    //                 if (!inquotes) {
    //                     inArray = false;
    //                     isKey = true;
    //                     i = 0;
    //                     inquotes = false;
    //                     System.out.println((char) b);
    //                     System.out.print("\u001B[33m"); // yellow
    //                     ind += 2;
    //                 } else {
    //                     System.out.print((char) b);
    //                 }
    //                 break;
    //             case '}':
    //                 if (!inquotes) {
    //                     isKey = true;
    //                     i = 0;
    //                     inquotes = false;
    //                     System.out.println();
    //                     ind -= 2;
    //                     pindent(ind);
    //                     System.out.print("\u001B[0m"); // reset
    //                     System.out.print((char) b);
    //                     System.out.print("\u001B[33m"); // yellow
    //                 } else {
    //                     System.out.print((char) b);
    //                 }
    //                 break;
    //             case '[':
    //                 inArray = true;
    //                 inquotes = false;
    //                 System.out.print((char) b);
    //                 break;
    //             case ']':
    //                 inArray = false;
    //                 inquotes = false;
    //                 System.out.print((char) b);
    //                 break;
    //             case ',':
    //                 if (!inquotes && !inArray) {
    //                     System.out.print("\u001B[0m"); // reset
    //                     System.out.print((char) b);
    //                     System.out.println();
    //                     isKey = true;
    //                     System.out.print("\u001B[33m"); // yellow
    //                     i = 0;
    //                     break;
    //                 }
    //                 System.out.print((char) b);
    //                 break;
    //             case '"':
    //                 if (isKey && !inquotes) {
    //                     pindent(ind);
    //                 }

    //                 inquotes = true;
    //                 i++;
    //                 if (i == 2) {
    //                     inquotes = false;
    //                     isKey = false;
    //                     System.out.print((char) b);
    //                     System.out.print("\u001B[0m"); // reset
    //                     break;
    //                 }
    //                 if (i == 4 && !inArray) {
    //                     inquotes = false;
    //                     isKey = true;
    //                     System.out.print((char) b);
    //                     System.out.print("\u001B[33m"); // yellow
    //                     i = 0;
    //                     break;
    //                 }
    //                 if (i > 4 && !inArray) {
    //                     inquotes = true;
    //                     isKey = true;
    //                     System.out.print((char) b);
    //                     System.out.print("\u001B[33m"); // yellow
    //                     i = 1;
    //                     break;
    //                 }
    //                 System.out.print((char) b);
    //                 break;
    //             default:
    //                 System.out.print((char) b);
    //                 break;
    //         }
    //     }
    //     System.out.print("\u001B[0m"); // reset
    //     System.out.println();
    // }

    // private static void pindent(int indent) {
    //     for (int i = 0; i < indent; i++)
    //         System.out.print(" ");
    // }
}
