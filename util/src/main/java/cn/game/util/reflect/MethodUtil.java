package cn.game.util.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodUtil {
	protected static Logger log = LoggerFactory.getLogger(MethodUtil.class);

	/**
	 * 自定义异常：方法重名异常
	 */
	public static class DuplicateMethodException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public DuplicateMethodException(String methodName, List<Method> methods) {
			super(String.format("找到多个同名方法 '%s':\n%s", methodName,
					methods.stream()
							.map(m -> String.format("  %s(%s)", m.getName(),
									Arrays.stream(m.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "))))
							.collect(Collectors.joining("\n"))));
		}
	}

	/**
	 * 获取当前方法的Method对象（用于实例方法）
	 * @param object 当前对象实例
	 * @return Method对象
	 */
    public static Optional<Method> getCurrentMethod(Object object) {
		return getCurrentMethod(object.getClass());
	}

	/**
	 * 获取当前方法的Method对象（用于静态方法）
	 * @param clazz 当前类
	 * @return Method对象
	 */
	public static Optional<Method> getCurrentMethod(Class<?> clazz) {
        try {
			// 使用StackWalker获取当前方法名和声明类
            StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            StackWalker.StackFrame frame = walker.walk(frames -> 
                frames.skip(1).findFirst()).orElse(null);
                
            if (frame == null) {
                return Optional.empty();
            }

            String methodName = frame.getMethodName();
            
			// 查找所有同名方法
			List<Method> matchingMethods = Arrays.stream(clazz.getDeclaredMethods())
					.filter(m -> m.getName().equals(methodName))
					.collect(Collectors.toList());

			// 检查是否存在重名方法
			if (matchingMethods.size() > 1) {
				throw new DuplicateMethodException(methodName, matchingMethods);
            }
            
			return matchingMethods.stream().findFirst();

		} catch (DuplicateMethodException e) {
			throw e;
        } catch (Exception e) {
			e.printStackTrace();
            return Optional.empty();
        }
	}

	/**
	 * 打印方法的详细信息
	 * @param method Method对象
	 */
    public static void printMethodInfo(Method method) {
        if (method == null) return;
        
        method.setAccessible(true);
        
        System.out.println("Method Information:");
        System.out.println("- Method Name: " + method.getName());
        System.out.println("- Return Type: " + method.getReturnType().getName());
        System.out.println("- Parameters:");
        
        var parameters = method.getParameters();
        try {
            StackTraceElement[] stackTrace = new Exception().getStackTrace();
            StackTraceElement caller = stackTrace[1];
            String className = caller.getClassName();
            String methodName = caller.getMethodName();
            int lineNumber = caller.getLineNumber();
            
            System.out.println("  Called from: " + className + "." + methodName + " (line " + lineNumber + ")");
            
            for (int i = 0; i < parameters.length; i++) {
                System.out.println("  Parameter " + (i + 1) + ":");
                System.out.println("    Name: " + parameters[i].getName());
				System.out.println("    Type: " + parameters[i].getType().getName());
			}

			if (method.getAnnotations().length > 0) {
				System.out.println("- Annotations:");
				for (var annotation : method.getAnnotations()) {
					System.out.println("  " + annotation);
				}
			}

			System.out.println("- Modifiers: " + java.lang.reflect.Modifier.toString(method.getModifiers()));

			if (method.getExceptionTypes().length > 0) {
				System.out.println("- Throws:");
				for (Class<?> exceptionType : method.getExceptionTypes()) {
					System.out.println("  " + exceptionType.getName());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}