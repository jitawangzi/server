package cn.game.protocol.tool;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.javassist.Modifier;

import com.alibaba.excel.EasyExcel;

import cn.game.protocol.manual.ErrorMsgEnum;

/**    
 * 将枚举数据，导出到excel表中
 * 
 * 枚举的格式不带name，输出excel的时候，特别添加一下
 * 
 * 2024年5月13日 下午2:39:45
 * @author SYQ
 */
public class EnumToExcel {
	private static final String outputDir = System.getenv("metafolder") + "/../Excels/";

	public static void main(String[] args) throws Exception {
		exportExcel(ErrorMsgEnum.class, "ErrorMessage#错误枚举");
	}

	private static <T extends Enum<T>> void exportExcel(Class<T> enumClass, String sheet)
			throws FileNotFoundException, IllegalAccessException {
		String filePath = outputDir + enumClass.getSimpleName() + ".xlsx";

		EasyExcel.write(new FileOutputStream(filePath), ExportModel.class).sheet(sheet).doWrite(getDataReflect(enumClass));
		System.out.println("Excel 文件已导出：" + filePath);
	}

	public static <T extends Enum<T>> List<List<Object>> getDataReflect(Class<T> enumClass) throws IllegalArgumentException, IllegalAccessException {
		List<List<Object>> data = new ArrayList<>();

		// 添加表头
		List<Object> header1 = new ArrayList<>();
		List<Object> header2 = new ArrayList<>();
		List<Object> header3 = new ArrayList<>();
		List<Object> header4 = new ArrayList<>();

		Field[] fields = enumClass.getDeclaredFields();
		int findex = 0;
		for (Field field : fields) {
			field.setAccessible(true);
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if (findex == 1) { // 额外增加一个name字段
				header1.add("c"); // 第一行先固定加上cs
				header2.add("name");
				header3.add("string");
			}
			header1.add("c"); // 第一行先固定加上cs
			header2.add(field.getName());
			header3.add(field.getType().getSimpleName().toLowerCase());
//			header4.add(field.get(ItemTypeEnum.values()[0]));
			// 随便在加点,第四行第一列不让为null
			if (findex == 0) {
				header4.add("ID");
			}
			findex++;
		}
		data.add(header1);
		data.add(header2);
		data.add(header3);
		data.add(header4);

		// 添加数据
		for (Enum<?> e : enumClass.getEnumConstants()) {
			findex = 0;
			List<Object> row = new ArrayList<>();
			for (Field field : fields) {
				field.setAccessible(true);
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				if (findex == 1) { // 额外增加name字段的数据
					row.add(e.name());
				}
				row.add(field.get(e));
				findex++;
			}
			data.add(row);
		}

		return data;
	}

	public static class ExportModel {
		// 此处不需要定义任何属性
	}
}
