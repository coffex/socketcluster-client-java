package io.github.sac;

import com.neovisionaries.ws.client.WebSocketFrame;
import org.json.JSONException;
import org.json.JSONObject;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by coffex on 18/1/18.
 */
public class MinBinCodecEngine implements CodecEngine {
  private MinBinPacketMapper mapper;
  private MinBinPacketPacker packer;

  public MinBinCodecEngine() {
    mapper = new MinBinPacketMapper();
    packer = new MinBinPacketPacker();
  }

  public MinBinCodecEngine(MinBinPacketMapper mapper, MinBinPacketPacker packer) {
    this.mapper = mapper;
    this.packer = packer;
  }

  @Override
  public Message decode(WebSocketFrame frame) {
    MessageUnpacker messageUnpacker = MessagePack.newDefaultUnpacker(frame.getPayload());
    try {
      Value value = messageUnpacker.unpackValue();
      if (value.isStringValue()) {
        String str = value.asStringValue().asString();
        if ("#1".equalsIgnoreCase(str)) {
          return Message.ping();
        } else if ("#2".equalsIgnoreCase(str)) {
          return Message.pong();
        }
      } else if (value.isMapValue()) {
        JSONObject jsonObject = new JSONObject();
        packer.unpackJMap(value.asMapValue(), jsonObject);
        mapper.mapIncoming(jsonObject);
        return Message.forData(jsonObject);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public byte[] encode(Message message) {
    MessageBufferPacker messageBufferPacker = MessagePack.newDefaultBufferPacker();
    try {
      switch (message.getType()) {
        case PING:
          packer.packPrimitive(messageBufferPacker, "#1");
          break;
        case PONG:
          packer.packPrimitive(messageBufferPacker, "#2");
          break;
        case DATA:
          mapper.mapOutgoing(message.getData());
          packer.packJMap(messageBufferPacker, message.getData());
          break;
      }
      messageBufferPacker.close();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return messageBufferPacker.toByteArray();
  }

  @Override
  public Type getType() {
    return Type.BINARY;
  }
}
