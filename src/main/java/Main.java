import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Main {
    public static void main(String[] args) throws IOException {
        cleanGehölze();
        cleanWeitereBäume();
        joinAllTrees();
    }

    private static void joinAllTrees() throws IOException {
        List<Tree> treeList = new ArrayList<>();
        treeList.addAll(generateTreeListFromGehölze(getJsonFeatures("gehölze.json")));
        treeList.addAll(generateTreeListFromWeitereBäume(getJsonFeatures("weitere_bäume.json")));
        List<Tree> orderedTreeList = rewriteIDs(treeList);
        convertToJson(orderedTreeList, "all_trees.json");
    }

    private static List<Tree> rewriteIDs(List<Tree> treeList) {
        int index = 1;
        for (Tree tree : treeList) {
            tree.setId(index);
            index++;
        }
        return treeList;
    }

    private static void cleanGehölze() throws IOException {
        JsonNode features = getJsonFeatures("gehölze.json");
        convertToJson(generateTreeListFromGehölze(features), "gehölze.json");
    }

    private static void cleanWeitereBäume() throws IOException {
        JsonNode features = getJsonFeatures("weitere_bäume.json");
        convertToJson(generateTreeListFromWeitereBäume(features), "weitere_bäume.json");
    }

    private static JsonNode getJsonFeatures(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(path);

        JsonNode jsonNode = objectMapper.readTree(inputStream);
        JsonNode features = jsonNode.get("features");
        return features;
    }

    private static List<Tree> generateTreeListFromGehölze(JsonNode features) {
        List<Tree> treeList = new ArrayList<>();
        int index = 1;
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

            tree.setId(index);
            treeList.add(tree);
            index++;
        }

        return treeList;
    }

    private static List<Tree> generateTreeListFromWeitereBäume(JsonNode features) {
        List<Tree> treeList = new ArrayList<>();
        int index = 1;
        for (JsonNode feature : features) {
            Tree tree = new Tree();
            ObjectNode object = (ObjectNode) feature;

            JsonNode coordinates = object.get("geometry").get("coordinates");
                double longitude = coordinates.get(0).doubleValue();
                double latitude = coordinates.get(1).doubleValue();
                tree.setCoordinates(new Point(longitude, latitude));

            JsonNode properties = object.get("properties");
            ObjectNode propertiesObj = (ObjectNode) properties;

            List<String> searchStrings = Arrays.asList("name", "genus", "genus:de", "genus:en", "species:de", "species:en", "species:wikidata", "species:wikipedia");
            StringBuilder stringBuilder = new StringBuilder();

            for (String searchString : searchStrings) {
                if (propertiesObj.has(searchString) && propertiesObj.get(searchString).textValue() != null) {
                    stringBuilder.append(propertiesObj.get(searchString).textValue());
                }
            }
            tree.setSpecies(stringBuilder.toString());

            if (propertiesObj.get("circumference") != null) {
                tree.setCircumference(propertiesObj.get("circumference").intValue());
            } else {
                tree.setCircumference(0);
            }

            tree.setId(index);
            treeList.add(tree);
            index++;
        }

        return treeList;
    }

    private static void convertToJson(List<Tree> treeList, String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(Paths.get(path).toFile(), treeList);
    }
}
