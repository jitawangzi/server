package cn.game.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;

public class ExcelUtil {
	/** MapExcel输出的目标文件夹 */
	private static String exploreMapDir = "D:\\ExploreMap";
	private static int sheetIndex = 0;

	public static void main(String[] args) {
		printExploreMap(null);

	}

	/**
	 * 打印探索地图
	 * @param objects
	 */
	public static void printExploreMap(Object[][] objects) {
		if (!Config.isTest) {
			return;
		}
		File file = new File(exploreMapDir);
		if (!file.exists()) {
			file.mkdir();
		}
		String fileName = exploreMapDir + File.separator + sheetIndex + ".xlsx";
		ExcelWriter excelWriter = EasyExcel.write(fileName).build();
		try {

			WriteSheet writeSheet = EasyExcel.writerSheet(sheetIndex).build();
			sheetIndex++;

			List<List<Object>> data = dataList(objects);
			excelWriter.write(data, writeSheet);

		} finally {
			// 千万别忘记finish 会帮忙关闭流
			if (excelWriter != null) {
				excelWriter.finish();
			}
		}
	}
	public static void printExploreMapScore(List<List<Object>> data) {
		if (!Config.isTest) { return; }
		File file = new File(exploreMapDir);
		if (!file.exists()) {
			file.mkdir();
		}
		String fileName = exploreMapDir + File.separator + sheetIndex + ".xlsx";
		ExcelWriter excelWriter = EasyExcel.write(fileName).build();
		try {

			WriteSheet writeSheet = EasyExcel.writerSheet(sheetIndex).build();
			sheetIndex++;

			excelWriter.write(data, writeSheet);

		} finally {
			// 千万别忘记finish 会帮忙关闭流
			if (excelWriter != null) {
				excelWriter.finish();
			}
		}
	}

	private static List<List<Object>> dataList(Object[][] objects) {
		List<List<Object>> list = new ArrayList<>();
		// x坐标
		int x_size = objects[0].length;
		List<Object> x = new ArrayList<>();
		x.add("");
		for (int i = 0; i < x_size; i++) {
			x.add("X-" + i);
		}
		list.add(x);

		// 格子编号
		int pos = 0;
		for (int i = 0; i < objects.length; i++) {
			List<Object> dataList = new ArrayList<>();
			// y坐标
			dataList.add("Y-" + i);
			for (int j = 0; j < objects[i].length; j++) {
				String str = "";
				if (objects[i][j] != null) {
					str = objects[i][j].toString();
				}
				str = pos + str;
				dataList.add(str);
				pos++;
			}
			list.add(dataList);
		}

		return list;
	}

}
