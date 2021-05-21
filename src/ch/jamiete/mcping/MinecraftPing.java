/*
 * Copyright 2014 jamietech. All rights reserved.
 * https://github.com/jamietech/MinecraftServerPing
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */
package ch.jamiete.mcping;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import ch.jamiete.mcping.MinecraftPingReply.Player;
import ch.jamiete.mcping.MinecraftPingReply.Players;
import ch.jamiete.mcping.MinecraftPingReply.Version;

public class MinecraftPing {

	/**
	 * Fetches a {@link MinecraftPingReply} for the supplied hostname. <b>Assumed
	 * timeout of 2s and port of 25565.</b>
	 * 
	 * @param hostname - a valid String hostname
	 * @return {@link MinecraftPingReply}
	 * @throws IOException
	 */
	public MinecraftPingReply getPing(final String hostname) throws IOException {
		return this.getPing(new MinecraftPingOptions().setHostname(hostname));
	}

	/**
	 * Fetches a {@link MinecraftPingReply} for the supplied options.
	 * 
	 * @param options - a filled instance of {@link MinecraftPingOptions}
	 * @return {@link MinecraftPingReply}
	 * @throws IOException
	 */
	public MinecraftPingReply getPing(final MinecraftPingOptions options) throws IOException {
		MinecraftPingUtil.validate(options.getHostname(), "Hostname cannot be null.");
		MinecraftPingUtil.validate(options.getPort(), "Port cannot be null.");

		final Socket socket = new Socket();
		socket.connect(new InetSocketAddress(options.getHostname(), options.getPort()), options.getTimeout());

		final DataInputStream in = new DataInputStream(socket.getInputStream());
		final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		// > Handshake

		ByteArrayOutputStream handshake_bytes = new ByteArrayOutputStream();
		DataOutputStream handshake = new DataOutputStream(handshake_bytes);

		handshake.writeByte(MinecraftPingUtil.PACKET_HANDSHAKE);
		MinecraftPingUtil.writeVarInt(handshake, MinecraftPingUtil.PROTOCOL_VERSION);
		MinecraftPingUtil.writeVarInt(handshake, options.getHostname().length());
		handshake.writeBytes(options.getHostname());
		handshake.writeShort(options.getPort());
		MinecraftPingUtil.writeVarInt(handshake, MinecraftPingUtil.STATUS_HANDSHAKE);

		MinecraftPingUtil.writeVarInt(out, handshake_bytes.size());
		out.write(handshake_bytes.toByteArray());

		// > Status request

		out.writeByte(0x01); // Size of packet
		out.writeByte(MinecraftPingUtil.PACKET_STATUSREQUEST);
		// < Status response

		MinecraftPingUtil.readVarInt(in); // Size
		int id = MinecraftPingUtil.readVarInt(in);
		if (id == -1) {
			handshake.close();
			handshake_bytes.close();
			out.close();
			in.close();
			socket.close();
			return new MinecraftPingReply(false, "Server prematurely ended stream", "无法连接到服务器",
					new Players(0, 0, new ArrayList<Player>()), new Version(-1, "0.0.0"), "", -1L);
		}
		if (id != MinecraftPingUtil.PACKET_STATUSREQUEST) {
			handshake.close();
			handshake_bytes.close();
			out.close();
			in.close();
			socket.close();
			return new MinecraftPingReply(false, "Server returned invalid packet.", "无法连接到服务器",
					new Players(0, 0, new ArrayList<Player>()), new Version(-1, "0.0.0"), "", -1L);
		}

		int length = MinecraftPingUtil.readVarInt(in);
		if (length == -1) {
			handshake.close();
			handshake_bytes.close();
			out.close();
			in.close();
			socket.close();
			return new MinecraftPingReply(false, "Server prematurely ended stream", "无法连接到服务器",
					new Players(0, 0, new ArrayList<Player>()), new Version(-1, "0.0.0"), "", -1L);
		}
		if (length == 0) {
			handshake.close();
			handshake_bytes.close();
			out.close();
			in.close();
			socket.close();
			return new MinecraftPingReply(false, "Server returned unexpected value.", "无法连接到服务器",
					new Players(0, 0, new ArrayList<Player>()), new Version(-1, "0.0.0"), "", -1L);
		}
		byte[] data = new byte[length];
		in.readFully(data);
		String json = new String(data, options.getCharset());

		long now = System.currentTimeMillis();
		// > Ping

		out.writeByte(0x09); // Size of packet
		out.writeByte(MinecraftPingUtil.PACKET_PING);
		out.writeLong(System.currentTimeMillis());

		// < Ping

		MinecraftPingUtil.readVarInt(in); // Size
		id = MinecraftPingUtil.readVarInt(in);

		int delay = (int) (System.currentTimeMillis() - now);

		if (id == -1) {
			handshake.close();
			handshake_bytes.close();
			out.close();
			in.close();
			socket.close();
			return new MinecraftPingReply(false, "Server prematurely ended stream", "无法连接到服务器",
					new Players(0, 0, new ArrayList<Player>()), new Version(-1, "0.0.0"), "", -1L);
		}
		if (id != MinecraftPingUtil.PACKET_PING) {
			handshake.close();
			handshake_bytes.close();
			out.close();
			in.close();
			socket.close();
			return new MinecraftPingReply(false, "Server returned invalid packet.", "无法连接到服务器",
					new Players(0, 0, new ArrayList<Player>()), new Version(-1, "0.0.0"), "", -1L);
		}

		// Close

		handshake.close();
		handshake_bytes.close();
		out.close();
		in.close();
		socket.close();

		// System.out.println(json);
		JSONObject jobject = JSONObject.parseObject(json);
		// 获取 "players": [
		JSONObject j_player = jobject.getJSONObject("players");
		// 获取 "version": [
		JSONObject j_version = jobject.getJSONObject("version");
        
		// 获取图标base64数据
		String favicon = jobject.getString("favicon");
		
		// 处理motd内容
		Boolean desc_flag = (jobject.get("description") instanceof String);

		String desc = desc_flag ? jobject.getString("description") : "";
		if (!desc_flag) {
			desc += jobject.getJSONObject("description").getString("text");
			if(jobject.getJSONObject("description").containsKey("extra")) {
				JSONArray j_extra = jobject.getJSONObject("description").getJSONArray("extra");
			
				for (Object obj : j_extra) {
					if (obj instanceof JSONObject) {
						JSONObject temp = (JSONObject) obj;
						if (temp.containsKey("text")) {
							desc += temp.getString("text");
						}
					}
				}
			}
		}
		
		// 获取在线人数
		int onlineplayer = j_player.getInteger("online");
		// 获取最大人数
		int maxplayer = j_player.getInteger("max");
		// 处理玩家列表
		List<Player> sample = new ArrayList<Player>();
		JSONArray j_player_sample = j_player.getJSONArray("sample");
		if(j_player_sample != null) {
			if (!j_player_sample.isEmpty()) {
				for (Object obj : j_player_sample) {
					if (obj instanceof JSONObject) {
						JSONObject j_player_sample_player = (JSONObject) obj;
						sample.add(new Player(j_player_sample_player.getString("name"),
							j_player_sample_player.getString("id")));
					}
				}
			}
		}
		// 将玩家类实例化，传入数据
		Players players = new Players(onlineplayer, maxplayer, sample);
		
		// 获取版本名称和protocol版本号
		String version_name = j_version.getString("name");
		Boolean protocol_flag = j_version.get("protocol") instanceof String;
		int version_protocol = (protocol_flag) ? strToInt(j_version.getString("protocol"))
				: j_version.getInteger("protocol");

		// 将版本类实例化，传入数据
		Version version = new Version(version_protocol, version_name);

		// 将回复信息实例化，传入数据
		MinecraftPingReply response = new MinecraftPingReply(true, "", desc, players, version, favicon, delay);
		
		// 返回信息
		return response;
	}

	private int strToInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}