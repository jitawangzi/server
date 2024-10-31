package cn.game.login.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

	/** 
	 * 哈希密码
	 * @param plainPassword
	 * @return
	 */
	public static String hashPassword(String plainPassword) {
		return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
	}

	/** 
	 * 验证密码
	 * @param plainPassword
	 * @param hashedPassword
	 * @return
	 */
	public static boolean checkPassword(String plainPassword, String hashedPassword) {
		return BCrypt.checkpw(plainPassword, hashedPassword);
	}

	public static void main(String[] args) {
		// 示例用法
		String username = "user@example.com";
		String plainPassword = null;

		// 哈希密码
		String hashedPassword = hashPassword(plainPassword);
		System.out.println("Hashed Password: " + hashedPassword);

		// 用户输入的密码
		String userInputPassword = null; // 模拟用户输入

		// 验证密码
		if (checkPassword(userInputPassword, hashedPassword)) {
			System.out.println("Password is correct!");
		} else {
			System.out.println("Password is incorrect!");
		}

		// 测试一个错误的密码
		if (checkPassword("wrongPassword", hashedPassword)) {
			System.out.println("Password is correct!");
		} else {
			System.out.println("Password is incorrect!");
		}
	}
}
