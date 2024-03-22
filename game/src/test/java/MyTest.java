/*
 *    Copyright 2006-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

public class MyTest {

	public static void main(String[] args) throws Exception {

	// LogFactory.forceSlf4jLogging();
	// System.setProperty("user.name", "zzs");
	// 这个集合记录着生成、合并、覆盖文件的信息

	/*	List<String> warnings = new ArrayList<String>();
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("dataServer-mybatorConfig.xml");
		ConfigurationParser cp = new ConfigurationParser(warnings);
		Configuration config = cp.parseConfiguration(in);
	
		// 不覆盖 Java 文件
		boolean overwrite = false;
		DefaultShellCallback callback = new DefaultShellCallback(overwrite);
		MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
	
		// 生成文件
		myBatisGenerator.generate(null);
		// 打印信息
		warnings.forEach(System.err::println);*/
}

}
