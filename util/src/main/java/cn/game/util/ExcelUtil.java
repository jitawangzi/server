package cn.game.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;

public class ExcelUtil {
	/** MapExcel输出的目标文件夹 */
	private static String exploreMapDir = "D:\\ExploreMap";
	private static int sheetIndex = 0;

	public static void main(String[] args) {
		printExploreMap(null);

	}

	public static void writeDataAutoWidth(String fileName, List<List<Object>> data) {

		ExcelWriterBuilder writerBuilder = EasyExcel.write(fileName, null);
		writerBuilder.registerWriteHandler(new AutoWidthHandler());

		writerBuilder.sheet("Sheet1").doWrite(data);
		
	}

	static class AutoWidthHandler implements CellWriteHandler {
		private Map<Integer, Map<Integer, Integer>> columnWidthMap = new HashMap<>();

		@Override
		public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, org.apache.poi.ss.usermodel.Row row, Head head,
				Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {
		}

		@Override
		public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell, Head head, Integer relativeRowIndex,
				Boolean isHead) {
		}

		@Override
		public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<WriteCellData<?>> cellDataList, Cell cell,
				Head head, Integer relativeRowIndex, Boolean isHead) {
			Sheet sheet = cell.getSheet();
			int columnWidth = getColumnWidth(cell);

			Map<Integer, Integer> map = columnWidthMap.computeIfAbsent(sheet.getSheetName().hashCode(), k -> new HashMap<>());
			Integer maxColumnWidth = map.get(cell.getColumnIndex());
			if (maxColumnWidth == null || columnWidth > maxColumnWidth) {
				map.put(cell.getColumnIndex(), columnWidth);
				sheet.setColumnWidth(cell.getColumnIndex(), columnWidth * 256);
			}
		}

		private int getColumnWidth(Cell cell) {
			int columnWidth = 0;
			switch (cell.getCellType()) {
			case STRING:
				columnWidth = cell.getStringCellValue().getBytes().length;
				break;
			case NUMERIC:
				columnWidth = String.valueOf(cell.getNumericCellValue()).getBytes().length;
				break;
			case BOOLEAN:
				columnWidth = String.valueOf(cell.getBooleanCellValue()).getBytes().length;
				break;
			case FORMULA:
				columnWidth = cell.getCellFormula().getBytes().length;
				break;
			case BLANK:
				columnWidth = 1;
				break;
			default:
				break;
			}
			return columnWidth;
		}
	}

	/**
	 * 打印探索地图
	 * @param objects
	 */
	public static void printExploreMap(Object[][] objects) {
//		if (!Config.isTest) {
//			return;
//		}
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
//		if (!Config.isTest) { return; }
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
