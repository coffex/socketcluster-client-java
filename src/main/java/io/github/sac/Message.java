package io.github.sac;

import org.json.JSONObject;

/**
 * Created by coffex on 18/1/18.
 */
public class Message {
  public static Message ping() {
    return new Message(Type.PING, null);
  }

  public static Message pong() {
    return new Message(Type.PONG, null);
  }

  public static Message forData(JSONObject data) {
    return new Message(Type.DATA, data);
  }

  enum Type {
    PING,
    PONG,
    DATA
  }

  private JSONObject data;
  private Type type;

  private Message(Type type, JSONObject data) {
    this.type = type;
    this.data = data;
  }

  public Type getType() {
    return type;
  }

  public JSONObject getData() {
    return data;
  }

  @Override
  public String toString() {
    return "Message{" +
        "type=" + type +
        '}';
  }
}
