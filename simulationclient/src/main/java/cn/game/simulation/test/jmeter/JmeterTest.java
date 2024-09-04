package cn.game.simulation.test.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.util.HttpUtil;

public class JmeterTest extends AbstractJavaSamplerClient {

	private static final Logger log = LoggerFactory.getLogger(JmeterTest.class);

	@Override
	public Arguments getDefaultParameters() {
		Arguments arguments = new Arguments();
		arguments.addArgument("url", "https://www.baidu.com");
		arguments.addArgument("a", "aa");
		arguments.addArgument("b", "bb");
		return arguments;
	}

	@Override
	public void setupTest(JavaSamplerContext context) {
//		context.
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {

		SampleResult sampleResult = new SampleResult();

		sampleResult.setSampleLabel("httptest");
		//接受JMeter界面上传输的参数

		String url = context.getParameter("url");
		String a = context.getParameter("a");
		String b = context.getParameter("b");

		sampleResult.setSamplerData("请求参数x的值为：" + url);

		//标记请求开始

		sampleResult.sampleStart();

		String string = HttpUtil.get(url);
		log.info(string);
		System.out.println(string);
		String string2 = HttpUtil.get("https://www.baidu.com");
		System.err.println(string2);
		log.info(string2);

		sampleResult.setSuccessful(true);
		sampleResult.setResponseData(string.getBytes());
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
//		String string2 = HttpUtil.get("https://www.baidu.com");
//		System.out.println(string2);
//		proce
	}

}
