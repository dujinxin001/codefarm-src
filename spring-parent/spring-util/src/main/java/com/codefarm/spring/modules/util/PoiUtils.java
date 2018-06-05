package com.codefarm.spring.modules.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PoiUtils {

	private static String getValue(HSSFCell hssfCell) {
		if (hssfCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			// 返回布尔类型的值
			return String.valueOf(hssfCell.getBooleanCellValue());
		} else if (hssfCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			// 返回数值类型的值
			return String.valueOf(hssfCell.getNumericCellValue());
		} else {
			// 返回字符串类型的值
			return String.valueOf(hssfCell.getStringCellValue());
		}
	}

	private static String getValueS(XSSFCell xssFCell) {
		if (xssFCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			// 返回布尔类型的值
			return String.valueOf(xssFCell.getBooleanCellValue());
		} else if (xssFCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			// 返回数值类型的值
			return String.valueOf(xssFCell.getNumericCellValue());
		} else {
			// 返回字符串类型的值
			return String.valueOf(xssFCell.getStringCellValue());
		}
	}

	/**
	 * 读取xls文件内容
	 * 
	 * @return List<XlsDto>对象
	 * @throws IOException
	 *             输入/输出(i/o)异常
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static Map<Integer, List<String>> readXls(InputStream input)
			throws IOException, TransformerException, ParserConfigurationException {
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook(input);
		Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
		HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
		if (hssfSheet == null) {
			return map;
		}
		// 循环行Row
		for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
			HSSFRow hssfRow = hssfSheet.getRow(rowNum);
			if (hssfRow == null) {
				continue;
			}
			List<String> list = new ArrayList<String>();
			for (int cellNum = 0; cellNum < hssfRow.getLastCellNum(); cellNum++) {
				HSSFCell xh = hssfRow.getCell(cellNum);
				if (xh == null) {
					continue;
				}
				list.add(getValue(xh));
			}
			map.put(rowNum, list);
		}
		return map;
	}

	/**
	 * 读取xls文件内容
	 * 
	 * @return List<XlsDto>对象
	 * @throws IOException
	 *             输入/输出(i/o)异常
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static Map<Integer, List<String>> readXlsS(InputStream input)
			throws IOException, TransformerException, ParserConfigurationException {
		XSSFWorkbook xssFWorkbook = new XSSFWorkbook(input);
		Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
		XSSFSheet hssfSheet = xssFWorkbook.getSheetAt(0);
		if (hssfSheet == null) {
			return map;
		}
		// 循环行Row
		for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
			XSSFRow hssfRow = hssfSheet.getRow(rowNum);
			if (hssfRow == null) {
				continue;
			}
			List<String> list = new ArrayList<String>();
			for (int cellNum = 0; cellNum < hssfRow.getLastCellNum(); cellNum++) {
				XSSFCell xh = hssfRow.getCell(cellNum);
				if (xh == null) {
					continue;
				}
				list.add(getValueS(xh));
			}
			map.put(rowNum, list);
		}
		return map;
	}

	/**
	 * 
	 * @Title: createWorKBook
	 * @author zhouwei
	 * @Description: TODO(创建excle对象)
	 * @param list
	 * @return
	 * @throws IOException 
	 */

	public static byte[] createWorKBook(InputStream input, List<LinkedHashMap<String, String>> data) throws IOException {
		// 创建工作簿对象
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook(input);
		// 创建单元格
		HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
		// 创建第一行
		// HSSFRow hssfRow = hssfSheet.createRow((int) 0);
		//
		// HSSFCellStyle hssfCellStyle = hssfWorkbook.createCellStyle();
		// 设置单元格长度
		// hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		// 创建表格的head
		// this.createExcelHead(hssfRow, hssfCellStyle);
		// 创建表格的body
		createExcelBody(data, hssfSheet);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		hssfWorkbook.write(byteArrayOutputStream);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		int len = byteArrayInputStream.available();
		byte[] bytes = new byte[len];
		byteArrayInputStream.read(bytes);
		return bytes;
	}

	
	private static void createExcelBody(List<LinkedHashMap<String, String>> data, HSSFSheet hssfSheet) {
		// 创建行对像
		HSSFRow hssfRow = null;
		// 遍历数据
		if (data == null) {
			return;
		}
		for (int i = 0; i < data.size(); i++) {
			// 创建第i+1行
			hssfRow = hssfSheet.createRow((int) i + 1);
			// 获取第I条数据
			Map<String, String> stu = data.get(i);
			if (stu == null) {
				continue;
			}
			int index=0;
			for (String key : stu.keySet()) {
				String value = stu.get(key);
				hssfRow.createCell(index).setCellValue(value);
				index++;
			}
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			FileInputStream in = new FileInputStream("D:/市场信息模版.xls");
			PoiUtils.readXls(in);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
