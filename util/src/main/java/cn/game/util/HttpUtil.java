package cn.game.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

/**
 * x库
 * @author 金永哲
 * 2013-02-19
 * @version 1.0.1
 * 
 * 2013-02-21 加方法getPage(String url, Map<String, String> params, String pageEncoding, Map<String, String> headers)
 */
public class HttpUtil {

	// 网页默认编码
	public static final String DEFAULT_PAGE_ENCODING = "utf-8";

	/**
	 * get一个网页，返回网页内容，默认使用utf-8编码读取
	 * @param url 网页地址
	 * @return 网页内容，如果失败返回空字符串
	 */
	public static String get(String url) {
		return get(url, DEFAULT_PAGE_ENCODING);
	}

	/**
	 * get一个网页，可以指定编码
	 * @param url 网页地址
	 * @param pageEncoding 页面编码
	 * @return 网页内容，如果事变返回空字符串
	 */
	public static String get(String url, String pageEncoding) {
		InputStream is;
		StringBuilder builder = new StringBuilder();
		try {
			URL aurl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) aurl.openConnection();
			connection.setConnectTimeout(8000);
			connection.setReadTimeout(8000);
			connection.connect();
			is = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, pageEncoding));
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.equals(""))
					builder.append(line);
			}

			connection.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return builder.toString();
	}

	public static String get(String url, Map<String, String> params, String pageEncoding, Map<String, String> headers) {

		String key = null, value = null;

		if (params != null) {
			url += "?";
			for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext();) {
				key = iter.next();
				value = params.get(key);

				if (key != null)
					url += "&";
				url += key + "=" + value;
			}
		}

		// (1) get请求
		HttpGet get = new HttpGet(url);

		// add header
		if (headers != null) {
			for (Iterator<String> iter = headers.keySet().iterator(); iter.hasNext();) {
				key = iter.next();
				value = headers.get(key);
				get.addHeader(key, value);
			}
		}

		DefaultHttpClient http = null;
		try {
			// (3) 发送请求
			http = new DefaultHttpClient();

			// 连接超时、读写超时、重试次数
			HttpConnectionParams.setConnectionTimeout(http.getParams(), 8000);
			HttpConnectionParams.setSoTimeout(http.getParams(), 8000);
			DefaultHttpRequestRetryHandler dhr = new DefaultHttpRequestRetryHandler(0, true);
			http.setHttpRequestRetryHandler(dhr);
			HttpResponse response = http.execute(get);

			// (4) 接收结果
			StringBuilder html = new StringBuilder();
			if (response.getStatusLine().getStatusCode() == 200) {
				// 读取返回的字符串内容
				InputStream in = null;
				try {
					in = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in, pageEncoding));
					String line = null;
					while ((line = reader.readLine()) != null) {
						html.append(line);
					}
				} finally {
					if (in != null)
						in.close();
				}
			}
			return html.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			http.getConnectionManager().shutdown();
		}
	}

	/**
	 * POST请求
	 * @param url
	 * @param params
	 * @return
	 */
	public static String post(String url, Map<String, String> params) {
		return post(url, params, null, null);
	}

	/**
	 * post请求
	 * @param url
	 * @param params
	 * @param pageEncoding
	 * @param headers
	 * @return
	 */
	public static String post(String url, Map<String, String> params, String pageEncoding, Map<String, String> headers) {

		if (pageEncoding == null)
			pageEncoding = "utf-8";

		// 请求
		HttpPost post = new HttpPost(url);

		// 请求参数
		String key, value;
		if (params != null) {
			List<NameValuePair> ps = new ArrayList<NameValuePair>();
			for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext();) {
				key = iter.next();
				value = params.get(key);
				ps.add(new BasicNameValuePair(key, value));
			}
			try {
				post.setEntity(new UrlEncodedFormEntity(ps, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("" + e, e);
			}
		}

		// add header
		if (headers != null) {
			for (Iterator<String> iter = headers.keySet().iterator(); iter.hasNext();) {
				key = iter.next();
				value = headers.get(key);
				post.addHeader(key, value);
			}
		}

		DefaultHttpClient http = null;
		try {
			// 发送请求
			http = new DefaultHttpClient();
			HttpParams httpParams = http.getParams();

			// 连接超时、读写超时、重试次数
			HttpConnectionParams.setConnectionTimeout(httpParams, 80000);
			HttpConnectionParams.setSoTimeout(httpParams, 80000);
			DefaultHttpRequestRetryHandler dhr = new DefaultHttpRequestRetryHandler(5, true);
			http.setHttpRequestRetryHandler(dhr);

			HttpResponse response = http.execute(post);
			HttpEntity entity = response.getEntity();

			// 接收结果
			StringBuilder html = new StringBuilder();
			if (response.getStatusLine().getStatusCode() == 200) {
				// 读取返回的字符串内容
				InputStream in = null;
				try {
					in = entity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in, pageEncoding));
					String line = null;
					while ((line = reader.readLine()) != null) {
						html.append(line);
					}
				} finally {
					if (in != null)
						in.close();
				}
			}
			return html.toString();
		} catch (Exception e) {
			e.printStackTrace();
			// throw new UtilException(e);
			return null;
		} finally {
			http.getConnectionManager().shutdown();
		}
	}

	/**
	 * post发送JSON
	 * @param url
	 * @param requestJSON
	 * @param pageEncoding
	 * @param headers
	 * @return
	 */
	public static String postJSON(String url, String requestJSON, String pageEncoding, Map<String, String> headers) {

		try {
			// 创建连接
			URL aurl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) aurl.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("content-type", "text/json");
			connection.setRequestProperty("Proxy-Connection", "Keep-Alive");
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);
			connection.connect();

			// POST请求
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(requestJSON);
			out.flush();
			out.close();

			// 读取响应
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String lines;
			StringBuffer sb = new StringBuffer("");
			while ((lines = reader.readLine()) != null) {
				lines = new String(lines.getBytes(), "utf-8");
				sb.append(lines);
			}

			reader.close();

			// 断开连接
			connection.disconnect();

			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] postBinary(String url, byte[] data) {
		URL uri = null;
		// 打开连接
		HttpURLConnection connection =null; 
		try {
			uri = new URL(url);
			connection = (HttpURLConnection) uri.openConnection();
			// 设置请求方法为POST
			connection.setRequestMethod("POST");

			// 启用输入输出
			connection.setDoOutput(true);
			connection.setDoInput(true);

			// 设置请求头
			connection.setRequestProperty("Content-Type", "application/octet-stream");

			// 获取输出流，写入二进制数据
			try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
				outputStream.write(data);
				outputStream.flush();
			}

			// 获取响应代码
			int responseCode = connection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				// 读取返回的二进制数据
				try (InputStream inputStream = connection.getInputStream()) {
					// 处理返回的二进制数据
					return inputStream.readAllBytes();
				}
			} else {
				// 处理请求失败的情况
				throw new RuntimeException("http error , responseCode: " + responseCode);
			}
			// 关闭连接

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public static void downFile(String url, String destFilename) {

		// 请求
		HttpGet get = new HttpGet(url);

		DefaultHttpClient http = null;
		try {
			// 发送请求
			http = new DefaultHttpClient();
			HttpParams httpParams = http.getParams();

			// 连接超时、读写超时、重试次数
			HttpConnectionParams.setConnectionTimeout(httpParams, 8000);
			HttpConnectionParams.setSoTimeout(httpParams, 8000);
			DefaultHttpRequestRetryHandler dhr = new DefaultHttpRequestRetryHandler(0, true);
			http.setHttpRequestRetryHandler(dhr);

			HttpResponse response = http.execute(get);

			// 接收结果
			StringBuilder html = new StringBuilder();
			if (response.getStatusLine().getStatusCode() == 200) {

				InputStream in = null;
				FileOutputStream fout = null;
				try {
					fout = new FileOutputStream(destFilename);
					int len;
					byte[] buf = new byte[512];
					in = response.getEntity().getContent();
					while ((len = in.read(buf)) > 0) {
						fout.write(buf, 0, len);
					}
				} finally {

					if (in != null)
						try {
							in.close();
						} catch (Exception e) {
						}
					if (fout != null)
						try {
							fout.close();
						} catch (Exception e) {
						}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			http.getConnectionManager().shutdown();
		}
	}

	public static void main(String[] args) throws Exception {
		// downFile("http://login.qxsgz.com.cn:7998/dict_version.json",
		// "d:/tmp/a.txt");

		String url = "https://www.okcoin.cn/api/v1/ticker.do";
		String s = HttpUtil.get(url, null, "utf-8", null);
		System.out.println(s);

	}
}
