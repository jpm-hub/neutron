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
}
