package fr.insee.genesis.controller.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
public class ExtendedJsonNormalizer {

    public static final String $_DATE = "$date";

    private ExtendedJsonNormalizer() {}

    /**
     * Recursively converts Mongo Extended JSON objects into simple types expected by the schema:
     *  - {"$date": "..."}  -> TextNode("...")
     * Leaves all other values untouched. Returns a structural copy of the node (does not mutate the original).
     */

    public static JsonNode normalize(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return node;

        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;

            if (obj.size() == 1) {
                if (obj.has($_DATE) && obj.get($_DATE).isTextual()) {
                    return TextNode.valueOf(obj.get($_DATE).asText());
                }
//                if (obj.has("$oid") && obj.get("$oid").isTextual()) {
//                    return TextNode.valueOf(obj.get("$oid").asText());
//                }
            }

            ObjectNode copy = obj.objectNode();
            obj.fields().forEachRemaining(e ->
                    copy.set(e.getKey(), normalize(e.getValue()))
            );
            return copy;
        }

        if (node.isArray()) {
            ArrayNode src = (ArrayNode) node;
            ArrayNode dst = src.arrayNode();
            for (JsonNode child : src) {
                dst.add(normalize(child));
            }
            return dst;
        }
        return node;
    }
}
