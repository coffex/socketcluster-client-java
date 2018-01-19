package io.github.sac;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Converts JSON into MsgPack format and vice-versa.
 */
public class MinBinPacketPacker {
  public JSONObject unpackJMap(MapValue mapValue, JSONObject object) throws JSONException {
    for(Map.Entry<Value, Value> entry : mapValue.entrySet()) {
      if(!entry.getKey().isStringValue()){
        throw new IllegalArgumentException("Keys must be strings, got: " + entry.getKey().getValueType());
      }
      String key = entry.getKey().asStringValue().asString();
      Value value = entry.getValue();

      switch(value.getValueType()){
        case MAP:
          object.put(key, unpackJMap(value.asMapValue(), new JSONObject()));
          break;
        case NIL:
          object.put(key, JSONObject.NULL);
          break;
        case INTEGER:
          if(value.asIntegerValue().isInIntRange())
            object.put(key, value.asIntegerValue().asInt());
          else
            object.put(key, value.asIntegerValue().asLong());
          break;
        case STRING:
          object.put(key, value.asStringValue().asString());
          break;
        case FLOAT:
          object.put(key, value.asFloatValue().toDouble());
          break;
        case BOOLEAN:
          object.put(key, value.asBooleanValue().getBoolean());
          break;
        case ARRAY:
          object.put(key, unpackJList(value.asArrayValue(), new JSONArray()));
          break;
        default:
          object.put(key, JSONObject.NULL);
      }
    }
    return object;
  }

  public JSONArray unpackJList(ArrayValue listValue, JSONArray array) throws JSONException {
    for(Value value : listValue.list()){
      switch(value.getValueType()){
        case MAP:
          array.put(unpackJMap(value.asMapValue(), new JSONObject()));
          break;
        case NIL:
          array.put(JSONObject.NULL);
          break;
        case INTEGER:
          if(value.asIntegerValue().isInIntRange())
            array.put(value.asIntegerValue().asInt());
          else
            array.put(value.asIntegerValue().asLong());
          break;
        case STRING:
          array.put(value.asStringValue().asString());
          break;
        case FLOAT:
          array.put(value.asFloatValue().toDouble());
          break;
        case BOOLEAN:
          array.put(value.asBooleanValue().getBoolean());
          break;
        case ARRAY:
          array.put(unpackJList(value.asArrayValue(), new JSONArray()));
          break;
        default:
          array.put(JSONObject.NULL);
      }
    }
    return array;
  }

  public void packJMap(MessageBufferPacker packer, JSONObject data) throws IOException, JSONException {
    packer.packMapHeader(data.length());
    Iterator<String> keys = data.keys();
    while(keys.hasNext()) {
      String key = keys.next();
      packer.packString(key); // pack the key
      Object value = data.get(key);
      if(value instanceof JSONArray) {
        packJArray(packer, (JSONArray)value);
      }
      else if(value instanceof JSONObject) {
        packJMap(packer, (JSONObject) value);
      }
      else {
        packPrimitive(packer, value);
      }
    }
  }

  public void packJArray(MessageBufferPacker packer, JSONArray data) throws IOException, JSONException {
    packer.packArrayHeader(data.length());
    for(int i = 0; i < data.length(); i++) {
      Object value = data.get(i);
      if(value instanceof JSONObject) {
        packJMap(packer,(JSONObject)value);
      }
      else if(value instanceof JSONArray){
        packJArray(packer,(JSONArray)value);
      }
      else {
        packPrimitive(packer, value);
      }
    }
  }

  public void packPrimitive(MessageBufferPacker packer, Object value) throws IOException {
    if(value instanceof String) {
      packer.packString((String)value);
    }
    else if(value instanceof Integer) {
      packer.packInt((Integer) value);
    }
    else if(value instanceof Boolean) {
      packer.packBoolean((boolean)value);
    }
    else if(value instanceof Double) {
      packer.packDouble((double)value);
    }
    else if(value instanceof Long) {
      packer.packLong((long)value);
    }
    else if(value == null || value.equals(JSONObject.NULL)) {
      packer.packNil();
    }
    else {
      throw new IOException("Invalid packing value of type " + value.getClass().getName());
    }
  }
}
