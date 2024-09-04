package cn.game.simulation.util;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

// 定义一个监听器来处理读取到的数据
class ExcelDataListener extends AnalysisEventListener<ExcelRowData> {
	private List<ExcelRowData> dataList = new ArrayList<>();

	@Override
	public void invoke(ExcelRowData data, AnalysisContext context) {
		dataList.add(data);
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		// 读取完成后的处理逻辑
	}

	public List<ExcelRowData> getDataList() {
		return dataList;
	}
}

public class ExcelMessagesReader {
	public static String[] messageNames;
	public static float[] messageWeights;

	public static void main(String[] args) {
		read();
	}

	public static void read() {
		String filePath = System.getProperty("user.dir") + "/messages.xlsx";
		ExcelDataListener listener = new ExcelDataListener();
		EasyExcel.read(filePath, ExcelRowData.class, listener).sheet("协议描述").doRead();

		// 获取读取到的数据
		List<ExcelRowData> dataList = listener.getDataList();
		messageNames = new String[dataList.size()];
		messageWeights = new float[dataList.size()];
		for (int i = 0; i < dataList.size(); i++) {
			ExcelRowData excelRowData = dataList.get(i);
			messageNames[i] = excelRowData.getProtocol();
			messageWeights[i] = excelRowData.getWeight();
		}

//		for (ExcelRowData data : dataList) {
//			System.out.println("序号: " + data.getIndex() + ", 模块: " + data.getModule() + ", 协议: " + data.getProtocol() + ", 协议号: " + data.getProtocolId()
//					+ ", 权重: " + data.getWeight() + ", 描述: " + data.getDescription());
//		}
	}
}
