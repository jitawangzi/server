package cn.game.protocol.steam;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import cn.game.protocol.steam.model.SteamApiList;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class SteamApiGenerator {

	public static final String KEY = "8157C7C239EC859582FD3ED14D1A1BC3";
	public static final String URL = "http://api.steampowered.com/ISteamWebAPIUtil/GetSupportedAPIList/v1/?key=" + KEY;

	public static void main(String[] args) throws Exception {

		InputStream is = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = SteamApiGenerator.class.getClassLoader();
		}
		is = loader.getResourceAsStream("velocity.properties");
		Properties prop = new Properties();
		prop.load(is);
		Velocity.init(prop);

		generater();
	}

	private static void generater() {
		Vertx vertx = Vertx.vertx();
		WebClient httpClient = WebClient.create(vertx);
		httpClient.getAbs(URL).as(BodyCodec.json(SteamApiList.class)).send().onFailure(Throwable::printStackTrace).onSuccess(r -> {
			SteamApiList apiList = r.body();

			VelocityContext ctx = new VelocityContext();
			ctx.put("apis", apiList);
			Template template = Velocity.getTemplate("steam_web_api.vm");
			write("D:\\work\\core\\src\\main\\java\\cn\\game\\core\\net\\steam/SteamAPI.java", template, ctx);

		});
	}

	private static void write(String filename, Template tpl, VelocityContext ctx) {
		Writer w = null;
		try {
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
			tpl.merge(ctx, w);
			w.close();
		} catch (Exception e) {
			throw new RuntimeException("" + e, e);
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
