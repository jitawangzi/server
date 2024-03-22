package cn.game.core.net.protocol.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class DefaultJsonProtocol extends JsonProtocol {

	private JsonObject json;

	@Override
	public String getMsgName() {
		return null;
	}

	@Override
	public void setData(JsonObject data) {
		this.json = data;
	}

	@Override
	public JsonObject getData() {
		return this.json;
	}

	@Override
	public byte[] serializeData() {
		return this.json.toBuffer().getBytes();
	}

	@Override
	public void deserializeData(byte[] data) {
		this.json = new JsonObject();
		this.json.readFromBuffer(0, Buffer.buffer(data));
	}
	
}
