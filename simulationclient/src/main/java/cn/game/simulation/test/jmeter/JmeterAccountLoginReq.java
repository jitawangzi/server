package cn.game.simulation.test.jmeter;

import java.util.UUID;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import cn.game.util.HttpUtil;

public class JmeterAccountLoginReq extends AbstractJavaSamplerClient {

	private static final Logger log = LoggerFactory.getLogger(JmeterAccountLoginReq.class);

	@Override
	public Arguments getDefaultParameters() {
		Arguments arguments = new Arguments();
		arguments.addArgument("url", "http://192.168.1.67:9390");
		arguments.addArgument("account", "");
		arguments.addArgument("pwd", "");
		return arguments;
	}

	@Override
	public void setupTest(JavaSamplerContext context) {
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {

		SampleResult sampleResult = new SampleResult();

		sampleResult.setSampleLabel("注册账号");
		//标记请求开始
		sampleResult.sampleStart();

		String url = context.getParameter("url");

		JSONObject jsonObject = new JSONObject();
		String name = UUID.randomUUID().toString();
		String pwd = "123";
		jsonObject.put("account", name);
		jsonObject.put("channel", "lk");
		jsonObject.put("pwd", pwd);

		jsonObject.put("username", name);
		jsonObject.put("channel", "lk");
		jsonObject.put("gameId", 0 + "");
		jsonObject.put("pwd", pwd);

		sampleResult.setSamplerData("请求参数x的值为：" + jsonObject);

		String resp = HttpUtil.postJSON(url + "/account/third_party_confirm", jsonObject.toJSONString(), "UTF-8", null);
		System.out.println(resp);

		sampleResult.setSuccessful(true);
		sampleResult.setResponseData(resp);
		return sampleResult;
	}

	@Override
	public void teardownTest(JavaSamplerContext context) {
		// TODO Auto-generated method stub
		System.err.println("测试结束");

	}
	public static void main(String[] args) {
//		JmeterTest test = new JmeterTest();
//		test.runTest(context);
		String string2 = HttpUtil.get("https://www.baidu.com");
		System.out.println(string2);
	}

}
