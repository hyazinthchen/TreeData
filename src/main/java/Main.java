import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Main {
    public static void main(String[] args) throws IOException {
        //cleanTreesFromOpenData();
        //cleanTreesFromOpenStreetMaps();
        //joinAllTrees();
        //getAllSpecies();
        convertToJson(addSpeciesId(), "TreesWithSpeciesId.json");
    }

    private static void joinAllTrees() throws IOException {
        List<Tree> treeList = new ArrayList<>();
        treeList.addAll(generateTreeListFromOpenData(getJsonFeatures("TreesOpenData.json")));
        treeList.addAll(generateTreeListFromOpenStreetMaps(getJsonFeatures("TreesOpenStreetMaps.json")));
        List<Tree> orderedTreeList = rewriteIDs(treeList);
        convertToJson(orderedTreeList, "AllTrees.json");
    }

    private static List<Tree> rewriteIDs(List<Tree> treeList) {
        int index = 1;
        for (Tree tree : treeList) {
            tree.setId(index);
            index++;
        }
        return treeList;
    }

    private static void cleanTreesFromOpenData() throws IOException {
        JsonNode features = getJsonFeatures("TreesOpenData.json");
        convertToJson(generateTreeListFromOpenData(features), "TreesOpenData.json");
    }

    private static void cleanTreesFromOpenStreetMaps() throws IOException {
        JsonNode features = getJsonFeatures("TreesOpenStreetMaps.json");
        convertToJson(generateTreeListFromOpenStreetMaps(features), "TreesOpenStreetMaps.json");
    }

    private static JsonNode getJsonFeatures(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(path);

        JsonNode jsonNode = objectMapper.readTree(inputStream);
        JsonNode features = jsonNode.get("features");
        return features;
    }

    private static List<Tree> generateTreeListFromOpenData(JsonNode features) {
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

    private static List<Tree> generateTreeListFromOpenStreetMaps(JsonNode features) {
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

    private static void getAllSpecies() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("CleanTrees.json");
        JsonNode rootNode = objectMapper.readTree(inputStream);
        List<String> speciesList = new ArrayList<>();

        for (JsonNode treeNode : rootNode) {
            ObjectNode object = (ObjectNode) treeNode;
            if (!speciesList.contains(object.get("species").textValue())) {
                speciesList.add(object.get("species").textValue());
            }
        }
        FileWriter writer = new FileWriter("species.txt");
        for(String str : speciesList) {
            writer.write(str + System.lineSeparator());
        }
        writer.close();
    }

    private static List<Tree> addSpeciesId() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("CleanTrees.json");
        JsonNode rootNode = objectMapper.readTree(inputStream);

        List<Tree> treeList = new ArrayList<>();

        for (JsonNode treeNode : rootNode) {
            ObjectNode object = (ObjectNode) treeNode;
            Tree tree = new Tree();

            tree.setId(object.get("id").intValue());
            tree.setSpecies(object.get("species").textValue());
            double longitude = object.get("coordinates").get("longitude").doubleValue();
            double latitude = object.get("coordinates").get("latitude").doubleValue();
            tree.setCoordinates(new Point(longitude, latitude));
            tree.setCircumference(object.get("circumference").intValue());

            SpeciesStorage speciesStorage = new SpeciesStorage();
            for (Map.Entry<Integer, String> entry : speciesStorage.species.entrySet()) {
                if(entry.getValue().equals(tree.getSpecies())){
                    tree.setSpeciesId(entry.getKey());
                }
            }
            treeList.add(tree);
        }
        return treeList;
    }
}
