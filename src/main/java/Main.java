import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Main {
    public static void main(String[] args) throws IOException {
        cleanGehölze();
    }

    private static void cleanGehölze() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream is = Main.class.getClassLoader().getResourceAsStream("gehölze.json");

        JsonNode jsonNode = objectMapper.readTree(is);
        JsonNode features = jsonNode.get("features");

        List<Tree> treeList = new ArrayList<>();

        for (JsonNode feature : features) {
            Tree tree = new Tree();
            ObjectNode object = (ObjectNode) feature;

            JsonNode coordinates = object.get("geometry").get("coordinates").get(0);
            for (JsonNode point : coordinates) {
                double longitude = point.get(0).doubleValue();
                double latitude = point.get(1).doubleValue();
                tree.setCoordinates(new Point(longitude, latitude));
            }

            JsonNode properties = object.get("properties");
            ObjectNode propertiesObj = (ObjectNode) properties;

            if (propertiesObj.get("baumart").textValue() != null) {
                tree.setSpecies((propertiesObj.get("baumart").textValue()));
            } else {
                tree.setSpecies("");
            }

            if (propertiesObj.get("stammumfang_cm") != null) {
                tree.setCircumference(propertiesObj.get("stammumfang_cm").intValue());
            } else {
                tree.setCircumference(0);
            }

            treeList.add(tree);
        }

        
    }
}
