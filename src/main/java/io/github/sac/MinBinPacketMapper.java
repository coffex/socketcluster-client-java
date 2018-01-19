package io.github.sac;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class maps socketcluster packets to and from the min-bin format required for transport
 */
public class MinBinPacketMapper {
  public void mapOutgoing(JSONObject json) throws JSONException {
    compressPublishPacket(json);
    compressEmitPacket(json);
    compressResponsePacket(json);
  }

  private void compressEmitPacket(JSONObject object) throws JSONException {
    if(!object.has("event") || object.isNull("event")){
      return;
    }
    JSONArray arr = new JSONArray();
    arr.put(object.get("event"));
    arr.put(object.get("data"));

    if(object.has("cid") && !object.isNull("cid")){
      arr.put(object.get("cid"));
      object.remove("cid");
    }
    object.put("e", arr);
    object.remove("event");
    object.remove("data");
  }

  private void compressPublishPacket(JSONObject object) throws JSONException {
    if(!object.has("event") || !"#publish".equals(object.getString("event")) || !object.has("data") || object.isNull("event")){
      return;
    }
    JSONArray arr = new JSONArray();
    arr.put(object.getJSONObject("data").get("channel"));
    arr.put(object.getJSONObject("data").get("data"));

    if(object.has("cid") && !object.isNull("cid")){
      arr.put(object.get("cid"));
    }
    object.put("p", arr);
    object.remove("event");
    object.remove("data");
    object.remove("cid");
  }

  private void compressResponsePacket(JSONObject object) throws JSONException {
    if(!object.has("rid") || object.isNull("rid")){
      return;
    }
    JSONArray arr = new JSONArray();
    arr.put(object.get("rid"));
    arr.put(object.get("error"));
    arr.put(object.get("data"));
    object.put("r", arr);
    object.remove("rid");
    object.remove("error");
    object.remove("data");
  }

  public void mapIncoming(JSONObject json) throws JSONException {
    decompressPublishPacket(json);
    decompressEmitPacket(json);
    decompressResponsePacket(json);
  }

  private void decompressEmitPacket(JSONObject object) throws JSONException {
    if(!object.has("e") || object.isNull("e")){
      return;
    }
    JSONArray arr = object.getJSONArray("e");
    object.put("event", arr.get(0));
    object.put("data", arr.get(1));
    if(arr.length() > 2 && !arr.isNull(2)){
      object.put("cid", arr.getInt(2));
    }
    object.remove("e");
  }

  private void decompressPublishPacket(JSONObject object) throws JSONException {
    if(!object.has("p") || object.isNull("p")){
      return;
    }
    object.put("event", "#publish");
    JSONObject data = new JSONObject();
    JSONArray arr = object.getJSONArray("p");
    data.put("channel", arr.get(0));
    data.put("data", arr.get(1));
    if(arr.length() > 2 && !arr.isNull(2)){
      object.put("cid", arr.get(2));
    }
    object.put("data", data);
    object.remove("p");
  }

  private void decompressResponsePacket(JSONObject object) throws JSONException {
    if(!object.has("r") || object.isNull("r")){
      return;
    }
    JSONArray arr = object.getJSONArray("r");

    object.put("rid", arr.get(0));
    object.put("error", arr.get(1));
    object.put("data", arr.get(2));
    object.remove("r");
  }
}
