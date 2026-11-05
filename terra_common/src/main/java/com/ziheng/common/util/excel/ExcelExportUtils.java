package com.ziheng.common.util.excel;

import com.ziheng.common.util.excel.annotation.ExcelColumn;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelExportUtils {

    private ExcelExportUtils() {
    }

    /**
     * 导出 Excel
     *
     * @param data      导出数据
     * @param clazz     数据类型
     * @param sheetName 工作表名称
     * @param output    输出流
     */
    public static <T> void export(
            List<T> data,
            Class<T> clazz,
            String sheetName,
            OutputStream output
    ) throws IOException {

        // 获取带有 ExcelColumn 注解的字段，并按顺序排列
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ExcelColumn.class))
                .sorted(Comparator.comparingInt(
                        field -> field.getAnnotation(ExcelColumn.class).order()
                ))
                .collect(Collectors.toList());

        if (fields.isEmpty()) {
            throw new IllegalArgumentException("没有配置 ExcelColumn 导出字段");
        }

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(sheetName);

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建数据样式
            CellStyle dataStyle = createDataStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);

            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                ExcelColumn column = field.getAnnotation(ExcelColumn.class);

                Cell cell = headerRow.createCell(i);
                cell.setCellValue(column.value());
                cell.setCellStyle(headerStyle);

                // 设置列宽，POI 的列宽单位为 1/256 个字符宽度
                sheet.setColumnWidth(i, column.width() * 256);
            }

            // 写入数据
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    T item = data.get(i);
                    Row row = sheet.createRow(i + 1);

                    for (int j = 0; j < fields.size(); j++) {
                        Field field = fields.get(j);
                        ExcelColumn column = field.getAnnotation(ExcelColumn.class);

                        field.setAccessible(true);
                        Object value;

                        try {
                            value = field.get(item);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("读取导出字段失败：" + field.getName(), e);
                        }

                        Cell cell = row.createCell(j);
                        cell.setCellStyle(dataStyle);

                        setCellValue(cell, value, column.format(), workbook);
                    }
                }
            }

            // 冻结首行
            sheet.createFreezePane(0, 1);

            // 添加筛选
            sheet.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            0,
                            Math.max(data == null ? 0 : data.size(), 0),
                            0,
                            fields.size() - 1
                    )
            );

            workbook.write(output);
            output.flush();
        }
    }

    /**
     * 设置单元格内容
     */
    private static void setCellValue(
            Cell cell,
            Object value,
            String format,
            Workbook workbook
    ) {
        if (value == null) {
            cell.setBlank();
            return;
        }

        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }

        if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
            return;
        }

        if (value instanceof Date date) {
            cell.setCellValue(date);

            if (!format.isEmpty()) {
                CellStyle style = workbook.createCellStyle();
                style.cloneStyleFrom(cell.getCellStyle());

                DataFormat dataFormat = workbook.createDataFormat();
                style.setDataFormat(dataFormat.getFormat(format));

                cell.setCellStyle(style);
            }
            return;
        }

        if (value instanceof LocalDate localDate) {
            cell.setCellValue(localDate.format(
                    DateTimeFormatter.ofPattern(
                            format.isEmpty() ? "yyyy-MM-dd" : format
                    )
            ));
            return;
        }

        if (value instanceof LocalDateTime localDateTime) {
            cell.setCellValue(localDateTime.format(
                    DateTimeFormatter.ofPattern(
                            format.isEmpty() ? "yyyy-MM-dd HH:mm:ss" : format
                    )
            ));
            return;
        }

        cell.setCellValue(value.toString());
    }

    /**
     * 表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);

        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        setBorder(style);

        return style;
    }

    /**
     * 数据样式
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        setBorder(style);

        return style;
    }

    /**
     * 设置边框
     */
    private static void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
