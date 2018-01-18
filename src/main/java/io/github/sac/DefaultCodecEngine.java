package io.github.sac;

import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by coffex on 18/1/18.
 */
public class DefaultCodecEngine implements CodecEngine {
  @Override
  public Message decode(WebSocketFrame frame) {
    String payload = frame.getPayloadText();
    if ("#1".equalsIgnoreCase(payload)) {
      return Message.ping();
    } else if ("#2".equalsIgnoreCase(payload)) {
      return Message.pong();
    } else {
      try {
        return Message.forData(new JSONObject(payload));
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  @Override
  public byte[] encode(Message message) {
    try {
      switch (message.getType()) {
        case PING:
            return "#1".getBytes("UTF-8");
        case PONG:
          return "#2".getBytes("UTF-8");
        case DATA:
          return message.getData().toString().getBytes("UTF-8");
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Type getType() {
    return Type.TEXT;
  }
}
