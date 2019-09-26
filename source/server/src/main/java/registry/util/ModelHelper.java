package registry.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import registry.dataBase.DataBase;
import registry.entity.model.ModelEntity;
import registry.entity.model.ModelItemEntity;

import java.io.IOException;
import java.util.Map;

public class ModelHelper {

    public static byte[] getItem(String modelName, String itemName) throws IOException {
        ModelEntity model = AutowiredForHelper.getModelRepository().findModelByName(modelName);
        return JsonHelper.getAsBytes(getItem(model.getItems(), itemName));
    }

    private static JsonNode getItem(Map<String, ModelItemEntity> modelItemMap, String itemName) throws IOException {
        ModelItemEntity modelItem = modelItemMap.get(itemName);
        if (modelItem != null) {
            switch (modelItem.getType()) {
                case SET:
                    ObjectNode result = JsonHelper.createNode();
                    for (Map.Entry<String, String> entry : JsonHelper.getMapFromString(modelItem.getContent()).entrySet()) {
                        JsonNode node = getItem(modelItemMap, entry.getValue());
                        if (node == null) {
                            result.put(entry.getKey(), entry.getValue());
                        } else {
                            result.set(entry.getKey(), node);
                        }
                    }
                    return result;
                case FORM:
                case ACTION:
                    return JsonHelper.createNode().rawValueNode(new RawValue(modelItem.getContent()));
                case REQUEST:
                    return JsonHelper.createNode().rawValueNode(new RawValue(DataBase.getJsonFromSQL(AutowiredForHelper.getDataSource(), modelItem.getContent()).toString()));
            }
        }

        return null;
    }
}