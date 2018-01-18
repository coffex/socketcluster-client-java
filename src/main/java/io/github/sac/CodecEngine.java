package io.github.sac;

import com.neovisionaries.ws.client.WebSocketFrame;

/**
 * Created by coffex on 18/1/18.
 */

public interface CodecEngine {
  enum Type {
    TEXT,
    BINARY
  }

  Message decode(WebSocketFrame frame);

  byte[] encode(Message message);

  Type getType();
}
