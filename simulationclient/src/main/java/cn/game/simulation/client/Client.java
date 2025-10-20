package cn.game.simulation.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.net.ssl.SSLException;

import cn.game.protocol.protobuf.BaseMsg;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import cn.game.core.net.client.AbstractNetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.protocol.protobuf.Account.AccountChannelType;
import cn.game.protocol.protobuf.Account.AccountErrorCode;
import cn.game.protocol.protobuf.Account.AccountLogin;
import cn.game.protocol.protobuf.Account.AccountLoginResponse;
import cn.game.protocol.protobuf.Account.AccountRegister;
import cn.game.protocol.protobuf.Account.AccountRegisterResponse;
import cn.game.protocol.protobuf.Account.AccountServerList;
import cn.game.protocol.protobuf.Account.AccountServerListResponse;
import cn.game.protocol.protobuf.Account.HttpResult;
import cn.game.protocol.protobuf.Account.ServerInfo;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerHeartbeatRequest_01000005;
import cn.game.protocol.protobuf.PlayerMsg.PlayerInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLoginRequest_01000001;
import cn.game.protocol.protobuf.GuildMsg.GuildMemberInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildPersonalInfo;
import cn.game.simulation.client.handler.WebSocketClientHandler;
import cn.game.simulation.socket.ClientHandler;
import cn.game.util.HttpUtil;
import cn.game.util.IdWorker;
import cn.game.util.Rnd;
import cn.game.util.SpringContextLoader;
import cn.game.util.log.LoggerType;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Promise;

/**
 * 模拟的客户端数据
* 2016-6-13 下午5:15:18
 * @author SYQ
 */
public class Client extends AbstractNetClient {

//	static final String URL = System.getProperty("url", "ws://127.0.0.1:7011/");
	public static final AttributeKey<Client> NETTY_CHANNEL_KEY = AttributeKey.valueOf("client");

	public static Logger logger = LoggerFactory.getLogger(Client.class);
	public static Logger netLogger = LoggerFactory.getLogger("Net");
	public static Logger systemOutLog = LoggerFactory.getLogger("SystemOut");

	private Channel channel;
	private String passportSessionId;
	public static IdWorker idWorker = new IdWorker(1, 0);

	public String name;
	private String pwd;
	// private String gameIp ;
	// private int gamePort ;
	// private String loginIp ;
	// private int loginPort ;
	// private String logbackFile ;
	// private String args;

	public int accountId;
	public int roleId;
	public volatile String sign;
	public int gameServerId;
	public Properties initialProp;
	public String headImageUrl;
	public int sex;

	/** 要登录的serverId */
	private String serverId;
	private String version;
	/** 要登录的server ip */
	private String serverIp;
	/** 要登录的server 端口 */
	private int serverPort;
	/** 是否是pc端 */
	private boolean isPc;
	/** 存档列表 */
	// List<PlayerArchiveInfo> archivesList;
	public volatile int messageType = (byte) 0x01;

	public static Map<String, Client> clients = new ConcurrentHashMap<>();

	public static Map<Integer, String> callbacks = new ConcurrentHashMap<>();

	private AtomicBoolean init = new AtomicBoolean(false);

	public static final String defaultChannel = "official";
//	public static final String defaultChannel = "changyou";
//	public static final String defaultChannel = "wechat";
//	public static final String defaultChannel = "steam";
//	int seq = 0;

	public AtomicInteger sendCount = new AtomicInteger(0);
	public AtomicInteger recvCount = new AtomicInteger(0);
	public AtomicInteger seq = new AtomicInteger(1);

	private volatile int resendCount = 0;
	public static boolean exitOnClientClose = true;

	/** 发送中的消息组 */
	public int sendingGroup;
	/** 上一次发送的消息名 */
	public String msgNameSend;

	// 消息序号: 消息名，消息发送时间，纳秒
//	public Map<Integer, Pair<String, Long>> sendMessages = new ConcurrentHashMap<>();
//	 消息序号: 消息名，消息接收时间，纳秒
//	public Map<Integer, Pair<String, Long>> recvMessages = new ConcurrentHashMap<>();

	private static final EventLoopGroup group = new NioEventLoopGroup();

	/** 玩家数据 **/
	private PlayerAllInfo playerAllInfo;

	private BattleMsg.BattlePvPTargetListResponse_13000112 targetListResponse;
	private long inPvPBattlePid;

	private Map<Integer, Message> sendingMessageMap = new HashMap<>();
	/** 当前处理的消息序号 */
	private volatile int curMessageSeq;

	public static long startTime;
	public static long startConnectTime;
	public static long startLoginTime;
	/** 最后一次发消息的时间 */
	private long lastSendMessageTime;
	/** 最后一次发消息的内容 */
	private byte[] lastSendMessageContent;

	public int guideType = 1;
	public int guideStep = 1;

	public List<SimplePlayerInfo> recommendList = new ArrayList<>();;

	// 上一次心跳时间
	private long lastHeartbeatTime = System.currentTimeMillis();
	// 保存一些临时数据，用在后续的测试模拟协议数据
	public GuildMemberInfo guildMember;
	public GuildPersonalInfo guildPersonalInfo;

	public List<Integer> guildIds = new ArrayList<>();
	// 踏碎凌霄 助战奖励信息
	public List<BaseMsg.EquipTowerHelpRewardInfo> helpRewardList = new ArrayList<>();

	public static Client getClient(int callback) {
		String string = callbacks.get(callback);
		return clients.get(string);
	}

	public Client(String name) {
		this.name = name;
		startTime = System.currentTimeMillis();
	}

	public Client(String name, String pwd) {
		this.name = name;
		this.pwd = pwd;
		startTime = System.currentTimeMillis();
	}

	public Client(String name, String pwd, String serverId, String version) {
		this.name = name;
		this.pwd = pwd;
		this.serverId = serverId;
		this.version = version;
		startTime = System.currentTimeMillis();
	}

	public Client() {
		startTime = System.currentTimeMillis();
	}

	public void loginPassport(String url) {

		JSONObject jsonObject = new JSONObject();

		if (name == null || name.trim().length() == 0) {
			register(url);
		}
//		map.put("username", name);
//		map.put("channel", "lk");
//		map.put("gameId", 0 + "");
//		map.put("pwd", pwd);
//		jsonObject.put("username", name);
		jsonObject.put("channel", defaultChannel);
		if (defaultChannel == "official") {
			jsonObject.put("token", this.name + " " + this.pwd);
		} else {
			jsonObject.put("token", "0b1FJSll2terle4UMhll23wScx3FJSlu");
		}
		jsonObject.put("gameId", 0 + "");
		String resp = HttpUtil.postJSON(url + "/account/third_party_confirm", jsonObject.toJSONString(), "UTF-8", null);
		if (StringUtils.isEmpty(resp) || resp.indexOf("帐号不存在") > -1) {
			register(url, name, pwd);
			loginPassport(url);
			return;
		}

		JSONObject respJsonObject = JSONObject.parseObject(resp);
		systemOutLog.info("登陆返回: " + respJsonObject);
		String passport = respJsonObject.getString("passport_session_id");
		setPassportSessionId(passport);
//		map = new HashMap<>();
		jsonObject = new JSONObject();
//		map.put("passport_session_id", passport+"");
		jsonObject = new JSONObject();
		jsonObject.put("passport_session_id", passport + "");
		resp = HttpUtil.postJSON(url + "/account/server_list", jsonObject.toJSONString(), "UTF-8", null);

		JSONObject serverList = JSONObject.parseObject(resp);
		JSONArray jsonArray = serverList.getJSONArray("serverList");
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject obj = jsonArray.getJSONObject(i);
			String sid = obj.getString("server_id");
			if (serverId != null && serverId.equals(sid)) {
				this.serverIp = obj.getString("ip");
				this.serverPort = obj.getIntValue("port");
				break;
			}
		}
		systemOutLog.info(resp);

	}

	public void loginPassportProto(String url) throws Exception {

		if (name == null || name.trim().length() == 0) {
			registerProto(url);
		}
		AccountLogin.Builder loginBuilder = AccountLogin.newBuilder();
		if (defaultChannel == "official") {
			loginBuilder.setToken(this.name + " " + this.pwd);
			loginBuilder.setChannel(AccountChannelType.OFFICIAL);
		} else if (defaultChannel == "changyou") {
			loginBuilder.setToken(
					"{\"validateInfo\":\"be82a5a1d9720cffc6ffd5c84dff7e38793cdb1f949ed022c125012ad17c10bbf3ee8f02d95098bcb3eaa04892c5121ebd1998c5cd017e1da2000381ff5a087bd3647c680bec8f6f0f405e17ccda13378caf72c97e437c44c281b8503e45ce119ae1d960ccaced18469a9eaf7a41d28eabf79d7586b66ca461f551f6d09bb287e73d92360b457b0987e17770b2236f8e9c8777fc291529851220c93d73e7bf8375b794afa5420795c47d441ff4afcd6308105c989f48c9c2227208d76c3b3545266eb4851bb9d00e88d4cdce855667cd236be3168216eacc852cac6a4d056269fa5c3ce21b7b45c267c791cc1c1e5b078cf483d785c69f0\", \"channel_id\": \"3013\", \"opcode\": \"10001\"}");
			loginBuilder.setChannel(AccountChannelType.CHANGYOU);
		} else if (defaultChannel == "wechat") {
			loginBuilder.setToken("0d1Nbq000lqL7S1niE1005gzLB3Nbq0n");
			loginBuilder.setChannel(AccountChannelType.WECHAT);
		}
		byte[] resp = HttpUtil.postBinary(url + "/account/third_party_confirm", loginBuilder.build().toByteArray());
		AccountLoginResponse from = AccountLoginResponse.parseFrom(resp);
		HttpResult result = from.getResult();
		systemOutLog.info("登陆返回: " + result);
		systemOutLog.info(from.getResult().getErrorMsg());
		if (from.hasResult() && from.getResult().getErrorCode() == AccountErrorCode.ACCOUNT_NOT_EXIST) {
			registerProto(url, name, pwd);
			loginPassportProto(url);
			return;
		}

		String passport = from.getPassportSessionId();
		setPassportSessionId(passport);

		// 请求服务器列表
		resp = HttpUtil.postBinary(url + "/account/server_list",
				AccountServerList.newBuilder().setPassportSessionId(passport).build().toByteArray());
		AccountServerListResponse serverListResponse = AccountServerListResponse.parseFrom(resp);
		systemOutLog.info(serverListResponse.toString());

		chooseServer(serverListResponse.getServersList());

	}

	private void chooseServer(List<cn.game.protocol.protobuf.Account.ServerInfo> list) {

		List<ServerInfo> ret = new ArrayList<>();
		for (ServerInfo serverInfo : list) {
			if (serverInfo.getStatus() != 1) {
				continue;
			}
			ret.add(serverInfo);
		}
		if (ret.isEmpty()) {
			logger.warn("没有合适的Game服务器");
//			throw new IllegalArgumentException("没有合适的Game服务器");
		}
		ServerInfo serverInfo = null;
		if (serverId == null && !ret.isEmpty()) {
			serverInfo = Rnd.randomElement(ret);
		} else {
			for (ServerInfo info : ret) {
				if (serverId.equalsIgnoreCase(info.getServerId())) {
					serverInfo = info;
					break;
				}
			}
		}
		if (serverInfo != null) {
			this.serverIp = serverInfo.getIp();
			this.serverPort = serverInfo.getPort();
		}
	}

	public void register(String url) {

		register(url, null, null);
	}

	public void register(String url, String name, String pwd) {

		Map<String, String> map = new HashMap<>();
		JSONObject jsonObject = new JSONObject();
		if (name == null) {
			this.name = UUID.randomUUID().toString();
			if (pwd != null && pwd.length() > 0) {
				this.pwd = pwd;
			} else {
				this.pwd = "pass";
			}
		}
//		map.put("account", this.name);
//		map.put("channel", "official");
//		map.put("pwd", this.pwd);
//		map.put(name, pwd);
		jsonObject.put("account", this.name);
//		jsonObject.put("channel", "official");
		jsonObject.put("pwd", this.pwd);
//		String resp = HttpUtil.postJSON(url + "/account/register", jsonObject.toJSONString(),"UTF-8",null);
		byte[] resp = HttpUtil.postBinary(url + "/account/register",
				AccountRegister.newBuilder().setAccount(this.name).setPwd(this.pwd).build().toByteArray());
//		System.out.println(resp);
		try {
			String errorMsg = AccountRegisterResponse.parseFrom(resp).getResult().getErrorMsg();
			System.out.println(errorMsg);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void registerProto(String url, String name, String pwd) {

		systemOutLog.info("注册账号: ");

		if (name == null) {
			this.name = UUID.randomUUID().toString();
			if (pwd != null && pwd.length() > 0) {
				this.pwd = pwd;
			} else {
				this.pwd = "pass";
			}
		}
		byte[] resp = HttpUtil.postBinary(url + "/account/register",
				AccountRegister.newBuilder().setAccount(this.name).setPwd(this.pwd).build().toByteArray());
		try {
			String errorMsg = AccountRegisterResponse.parseFrom(resp).getResult().getErrorMsg();
			systemOutLog.info(errorMsg);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void registerProto(String url) {

		registerProto(url, null, null);

	}

	public void loginGateway(String ip, int port, String sourceIp) throws URISyntaxException, UnknownHostException, SSLException {

		startConnectTime = System.currentTimeMillis();
		connect(ip, port, sourceIp);

//		PlayerMsg.PlayerLoginRequest_01000001.Builder builder = PlayerMsg.PlayerLoginRequest_01000001.newBuilder();
//		builder.setServerId(ServerTestContext.serverId);
//		builder.setSessionId(passportSessionId + "");
//
//		sendProtocol(builder.build());
	}

	public void loginGateway(String ip, int port) throws URISyntaxException, UnknownHostException, SSLException {

		startConnectTime = System.currentTimeMillis();
		connect(ip, port, null);
	}

	public boolean run() {
		if (getInit() == false) {
			return false;
		}
		Object req = null;
//		seq.getAndIncrement();
		return true;
	}

	public void afterLogin(PlayerAllInfo allInfo) {
		setPlayerAllInfo(allInfo);
		PlayerInfo player = allInfo.getPlayer();
		setPlayerId(player.getId());
		setInit();
	}

	public static void main(String args[]) throws Exception {

		SpringContextLoader.loadWithFile(args);

		Client client = new Client();
		client.connect("localhost", 7011, null);

		// FriendApplyRequest_05000005.Builder builder =
		// cn.game.protocol.protobuf.FriendMsg.FriendApplyProcessor_05000005.newBuilder();

		// builder.addFriendId(22386);

		System.exit(0);

	}

	/**
	 * 连接之后登录
	 * @param serverIp
	 * @param port
	 * @throws URISyntaxException
	 * @throws UnknownHostException 
	 * @throws SSLException 
	 */
	public void connect(String serverIp, int port, String sourceIp) throws URISyntaxException, UnknownHostException, SSLException {
		connect(serverIp, port, sourceIp, true);
	}

	public Promise<Client> connect(String serverIp, int port, boolean login) throws URISyntaxException, UnknownHostException, SSLException {
		return connect(serverIp, port, null, true);
	}

	/**
	 * 连接游戏服务器
	 * @param serverIp
	 * @param port
	 * @param login
	 *            连接之后是否登录
	 * @throws URISyntaxException
	 * @throws UnknownHostException 
	 * @throws SSLException 
	 */
	public Promise<Client> connect(String serverIp, int port, String sourceIp, boolean login)
			throws URISyntaxException, UnknownHostException, SSLException {
		if (serverIp == null) {
			serverIp = this.serverIp;
			port = this.serverPort;
		}
		if (StringUtils.isEmpty(serverIp) || port == 0) {
			throw new IllegalArgumentException("serverIp or port is null,没有指定game ip和端口，也没有可用的指定id的game服务器");
		}
		InetAddressValidator validator = InetAddressValidator.getInstance();
		boolean validInet4Address = validator.isValidInet4Address(serverIp);
		boolean wss = !validInet4Address;
		// 如果配置了域名，则使用wss连接，如果是ip则用ws
		String url = (wss ? "wss://" : "ws://") + serverIp + ":" + port + "/";
		URI uri = new URI(url);
		final WebSocketClientHandler handler = new WebSocketClientHandler(
				WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));
		Bootstrap bootstrap = new Bootstrap().group(group)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.TCP_NODELAY, true);

		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws SSLException {
				ChannelPipeline p = ch.pipeline();
				if (wss) {
					SslContext sslCtx = SslContextBuilder.forClient().build();
					p.addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
				}
				// 客户端的消息处理器
				ClientHandler clientHandler = new ClientHandler();
				clientHandler.setDispatcher(SpringContextLoader.getContext().getBean(Dispatcher.class));
				// 支持大点的数据包。5m
				p.addLast(new HttpClientCodec(), new HttpObjectAggregator(5 * 1024 * 1024), new WebSocketFrameAggregator(5 * 1024 * 1024),
						handler, clientHandler);
			}
		});

		// 连接到服务器：
		Promise<Client> promise = group.next().newPromise();
		ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(uri.getHost(), port),
				sourceIp == null ? null : new InetSocketAddress(InetAddress.getByName(sourceIp), 0));

		connectFuture.addListener(f -> {
			ChannelFuture handshakeFuture = handler.handshakeFuture();
			handshakeFuture.addListener(f2 -> {
				if (!f2.isSuccess()) {
					Throwable cause = f2.cause();
					logger.warn("WS handshake failed: {}", url, cause);
					promise.tryFailure(cause);
					return;
				}
				this.channel = connectFuture.channel();
				Attribute<Client> attr = this.channel.attr(NETTY_CHANNEL_KEY);
				attr.set(this);
				promise.trySuccess(this);
				if (login) {
					// 登录到游戏服
					startLoginTime = System.currentTimeMillis();
//					ServerLoginRequest_01000051.Builder builder = PlayerMsg.ServerLoginRequest_01000051.newBuilder();
					PlayerLoginRequest_01000001.Builder builder = PlayerLoginRequest_01000001.newBuilder();
					builder.setSessionId(passportSessionId + "");
					builder.setVerstion(version);
					builder.setDeviceId("testdevice");

					builder.setAdChannel("4019392002");
					builder.setPlatform(5);
					builder.setSdkPayChannel("0010");
					builder.setSdkVersion("NULL");
					builder.setSystem("system");
					builder.setClueToken("{}");

					sendProtocol(builder.build());
				}
			});
		});

		return promise;
	}

	@SuppressWarnings("unchecked")
	private ChannelFuture sendWsPack(Message msg) {
		if (msg == null) {
			throw new IllegalArgumentException("发送的消息不能为空！");
		}
		byte[] byteArray = msg.toByteArray();
		CompositeByteBuf compositeBuffer = Unpooled.compositeBuffer(2);
		ByteBuf headerBuf = Unpooled.buffer(12);
		headerBuf.writeInt(byteArray.length);
		int seqSend = seq.getAndIncrement();
		headerBuf.writeInt(seqSend);
//		headerBuf.writeInt(111);
		headerBuf.writeInt(PbProtocol.getInstance().getMsgId(msg.getClass().getSimpleName()));
		ByteBuf bodyBuf = Unpooled.wrappedBuffer(byteArray);
		compositeBuffer.addComponents(headerBuf, bodyBuf);
		ByteBuf copiedBuffer = Unpooled.copiedBuffer(headerBuf.array(), bodyBuf.array());

		compositeBuffer.writerIndex(headerBuf.readableBytes() + bodyBuf.readableBytes());
		long sendTime = System.nanoTime();
		if (this.channel != null && this.channel.isActive() && this.channel.isWritable()) {
			BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame(compositeBuffer);
			ChannelFuture future = this.channel.writeAndFlush(binaryWebSocketFrame);
			future.addListeners(f -> {
				if (f.isSuccess()) {
//					System.out.print("==============================消息发送成功==========================");
					if (!msg.getClass().getSimpleName().equals("PlayerHeartbeatRequest_01000005")) {
						netLogger.info("opType[send]playerId[{}]name[{}]msgName[{}]msgData[{}]seq[{}]", playerId, name,
								msg.getClass().getSimpleName(), TextFormat.shortDebugString(msg), seqSend);
					}
					// 先不记录这个数据了
//					sendingMessageMap.put(seqSend, msg);
					sendCount.incrementAndGet();
					sendMessages.put(seqSend, Pair.of(msg.getClass().getSimpleName(), sendTime));
					lastSendMessageContent = copiedBuffer.array();
					resendCount = 0;

				} else {
					logger.error("消息发送失败：   opType[send]playerId[{}]name[{}]msgName[{}]msgData[{}]seq[{}]cause[{}]", playerId, name,
							msg.getClass().getSimpleName(), TextFormat.shortDebugString(msg), seqSend, f.cause());
				}
			});
			return future;
//			logger.info("【send】: " + msg.getClass().getSimpleName() + "  " + TextFormat.shortDebugString(msg));
		} else {
			logger.warn("player[{}] write message[{}] err,session[{}]", this, TextFormat.shortDebugString(msg), channel);
			return null;
		}

	}

	/** 
	 * 一般是当消息没有收到回复时，用来重发某个消息
	 * @param binaryWebSocketFrame
	 */
	private boolean resendWsPack(BinaryWebSocketFrame binaryWebSocketFrame) {
		if (resendCount++ >= 3 ) {
			return false; 
		}
		if (this.channel != null && this.channel.isActive() && this.channel.isWritable()) {
			ChannelFuture future = this.channel.writeAndFlush(binaryWebSocketFrame);
		}
		logger.warn("[{}]resend message,count[{}]", this, resendCount);
		return true; 
	}

	@Override
	public boolean needProcess(IProtocol<?> protocol) {
		this.curMessageSeq = protocol.getSeq();
		return true;
	}

//	@Override
//	public void afterProcess(IProtocol<?> protocol) {
//		this.sendingMessageMap.remove(this.curMessageSeq);
//
//	}

	@Override
	public long getPlayerId() {
		return playerId;
	}

	@Override
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public String getPassportSessionId() {
		return passportSessionId;
	}

	public void setPassportSessionId(String passportSessionId) {
		this.passportSessionId = passportSessionId;
	}

	public boolean getInit() {
		return init.get();
	}

	public void setInit() {
		this.init.getAndSet(true);
	}

	public void heartbeat() {
		long currentTime = System.currentTimeMillis();
		if (getInit() == false) {
			long waitLoginTime = currentTime - startTime;
			if (waitLoginTime > 60000) {
				LoggerType.Stdout.logger.error(this.name + " 登录Game初始化超时： " + waitLoginTime / 1000 + " s");
				return;
			}
			return;
		}
		if (currentTime - lastHeartbeatTime > 15000) {
			sendWsPack(PlayerHeartbeatRequest_01000005.getDefaultInstance());
			lastHeartbeatTime = currentTime;
		}
	}

	/** 
	 * 一个请求收到返回包后，才能发下一个请求
	 * @return
	 */
	public boolean isLastMessageReturn() {
		int i = seq.get();
		if (i == 1) {
			return true;
		}
		return recvMessages.get(i - 1) != null;
	}

	public void waitLastMessageReturn() throws TimeoutException {
		int loop = 0;
		while (!isLastMessageReturn()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			loop++;
			if (loop > 30000) {
				throw new TimeoutException("");
			}
		}
	}

	public boolean resendLastMessage() {
		// 如果5秒都没有收到返回，那就重发
		if (lastSendMessageContent != null &&  System.currentTimeMillis() - lastSendMessageTime > 5000) {
			boolean resendWsPack = resendWsPack(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(lastSendMessageContent)));
			if (!resendWsPack) {
				return false;
			}
			setLastSendMessageTime(System.currentTimeMillis());
			return true; 
		}
		return false;
	}

	public ChannelFuture sendProtocol(Message message) {
		setLastSendMessageTime(System.currentTimeMillis());
		return sendWsPack(message);
	}

	public void sendProtocolAfterInit(Message message) {
		sendProtocolAfterInit(message, false);
	}

	public void sendProtocolAfterInit(Message message, boolean wait) {

		if (wait) {
			waitInit();
		}
		if (!getInit())
			return;
		if (message != null) {
			sendWsPack(message);
		}
	}

	public void sendProtocolAfterInit(Supplier<Message> supplier, boolean wait) {

		if (wait) {
			waitInit();
		}
		if (!getInit()) {
			logger.warn("发送消息失败，玩家数据还没有初始化. message[{}]", supplier.get());
			return;
		}
		if (supplier != null) {
			sendWsPack(supplier.get());
		}
	}

	public void waitInit() {
		int loop = 0;
		while (!getInit()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			loop++;
			if (loop > 30000) {
				break;
			}
		}

	}

	@Override
	public void sendProtocol(Object message) {

		sendWsPack((Message) message);
	}

	@Override
	public void sendProtocol(Object message, int errorCode) {
		// TODO Auto-generated method stub

	}

	public PlayerAllInfo getPlayerAllInfo() {
		return playerAllInfo;
	}

	public void setPlayerAllInfo(PlayerAllInfo playerAllInfo) {
		this.playerAllInfo = playerAllInfo;
	}

	public void setPc(boolean isPc) {
		this.isPc = isPc;
	}

	// public List<PlayerArchiveInfo> getArchivesList() {
	// return archivesList;
	// }
	//
	// public void setArchivesList(List<PlayerArchiveInfo> archivesList) {
	// this.archivesList = archivesList;
	// }

	public long getLastSendMessageTime() {
		return lastSendMessageTime;
	}

	public void setLastSendMessageTime(long lastSendMessageTime) {
		this.lastSendMessageTime = lastSendMessageTime;
	}

	public Message getCurRequest() {

		return this.sendingMessageMap.get(curMessageSeq);
	}

	@Override
	public String toString() {
		return "Client:playerId[" + playerId + "]name[" + name + "]";
	}

//	@Override
//	public void sendProtocol(PDUProtocol message, Consumer<PDUProtocol> consumer) {
	// TODO Auto-generated method stub

//	}

	public BattleMsg.BattlePvPTargetListResponse_13000112 getTargetListResponse() {
		return targetListResponse;
	}

	public void setTargetListResponse(BattleMsg.BattlePvPTargetListResponse_13000112 targetListResponse) {
		this.targetListResponse = targetListResponse;
	}

	public boolean isInPvPBattle() {
		return inPvPBattlePid > 0;
	}

	public void setInPvPBattle(long inPvPBattlePid) {
		this.inPvPBattlePid = inPvPBattlePid;
	}

	public String getinPvPBattlePid() {
		return inPvPBattlePid + "";
	}

	// @Override
//	public String getIp() {
//		return null;
//	}
//
//	@Override
//	public int getPort() {
//		return 0;
//	}

}
