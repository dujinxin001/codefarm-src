package com.sxj.poi.transformer;

import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER_SELECTION;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_FILL;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_GENERAL;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_JUSTIFY;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_LEFT;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_RIGHT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DASHED;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DASH_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DASH_DOT_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DOTTED;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DOUBLE;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_HAIR;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASHED;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASH_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASH_DOT_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_NONE;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_SLANTED_DASH_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_THICK;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_THIN;
import static org.apache.poi.ss.usermodel.CellStyle.VERTICAL_BOTTOM;
import static org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER;
import static org.apache.poi.ss.usermodel.CellStyle.VERTICAL_TOP;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.sxj.poi.transformer.impl.HSSFHtmlHelper;
import com.sxj.poi.transformer.impl.XSSFHtmlHelper;

public class ExcelTransformer implements ITransformer
{
    private HtmlHelper helper = null;
    
    private boolean outputHTMLTag = false;
    
    private Workbook workbook = null;
    
    private boolean gotBounds;
    
    private int firstColumn;
    
    private int endColumn;
    
    private static final String DEFAULTS_CLASS = "excelDefaults";
    
    private static final String COL_HEAD_CLASS = "colHeader";
    
    private static final String ROW_HEAD_CLASS = "rowHeader";
    
    private static final Map<Short, String> ALIGN = mapFor(ALIGN_LEFT,
            "left",
            ALIGN_CENTER,
            "center",
            ALIGN_RIGHT,
            "right",
            ALIGN_FILL,
            "left",
            ALIGN_JUSTIFY,
            "left",
            ALIGN_CENTER_SELECTION,
            "center");
            
    private static final Map<Short, String> VERTICAL_ALIGN = mapFor(
            VERTICAL_BOTTOM,
            "bottom",
            VERTICAL_CENTER,
            "middle",
            VERTICAL_TOP,
            "top");
            
    private static final Map<Short, String> BORDER = mapFor(BORDER_DASH_DOT,
            "dashed 1pt",
            BORDER_DASH_DOT_DOT,
            "dashed 1pt",
            BORDER_DASHED,
            "dashed 1pt",
            BORDER_DOTTED,
            "dotted 1pt",
            BORDER_DOUBLE,
            "double 3pt",
            BORDER_HAIR,
            "solid 1px",
            BORDER_MEDIUM,
            "solid 2pt",
            BORDER_MEDIUM_DASH_DOT,
            "dashed 2pt",
            BORDER_MEDIUM_DASH_DOT_DOT,
            "dashed 2pt",
            BORDER_MEDIUM_DASHED,
            "dashed 2pt",
            BORDER_NONE,
            "none",
            BORDER_SLANTED_DASH_DOT,
            "dashed 2pt",
            BORDER_THICK,
            "solid 3pt",
            BORDER_THIN,
            "dashed 1pt");
            
    private static <K, V> Map<K, V> mapFor(Object... mapping)
    {
        Map<K, V> map = new HashMap<K, V>();
        for (int i = 0; i < mapping.length; i += 2)
        {
            map.put((K) mapping[i], (V) mapping[i + 1]);
        }
        return map;
    }
    
    @Override
    public void toHTML(InputStream source, OutputStream output)
            throws POITransformException
    {
        try
        {
            workbook = WorkbookFactory.create(source);
            if (workbook == null)
                throw new NullPointerException("wb");
            if (output == null)
                throw new NullPointerException("output");
            prepare();
            toHTML(output);
        }
        catch (InvalidFormatException | IOException e)
        {
            throw new POITransformException(e);
        }
        finally
        {
            try
            {
                source.close();
                output.close();
            }
            catch (IOException e)
            {
            }
            
        }
        
    }
    
    private void prepare()
    {
        setupColorMap();
    }
    
    private Formatter ensureOut(OutputStream output)
            throws POITransformException
    {
        try
        {
            return new Formatter(output, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new POITransformException(e);
        }
    }
    
    public void printStyles(Formatter out)
    {
        
        // First, copy the base css
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream("excelStyle.css")));
            String line;
            while ((line = in.readLine()) != null)
            {
                out.format("%s%n", line);
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Reading standard css", e);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    //noinspection ThrowFromFinallyBlock
                    throw new IllegalStateException("Reading standard css", e);
                }
            }
        }
        
        // now add css for each used style
        Set<CellStyle> seen = new HashSet<CellStyle>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++)
        {
            Sheet sheet = workbook.getSheetAt(i);
            Iterator<Row> rows = sheet.rowIterator();
            while (rows.hasNext())
            {
                Row row = rows.next();
                for (Cell cell : row)
                {
                    CellStyle style = cell.getCellStyle();
                    if (!seen.contains(style))
                    {
                        printStyle(style, out);
                        seen.add(style);
                    }
                }
            }
        }
    }
    
    private void printStyle(CellStyle style, Formatter out)
    {
        out.format(".%s .%s {%n", DEFAULTS_CLASS, styleName(style));
        styleContents(style, out);
        out.format("}%n");
    }
    
    private <K> void styleOut(String attr, K key, Map<K, String> mapping,
            Formatter out)
    {
        String value = mapping.get(key);
        if (value != null)
        {
            out.format("  %s: %s;%n", attr, value);
        }
    }
    
    private void styleContents(CellStyle style, Formatter out)
    {
        styleOut("text-align", style.getAlignment(), ALIGN, out);
        styleOut("vertical-align", style.getAlignment(), VERTICAL_ALIGN, out);
        fontStyle(style, out);
        borderStyles(style, out);
        helper.style(style, out);
    }
    
    private void borderStyles(CellStyle style, Formatter out)
    {
        styleOut("border-left", style.getBorderLeft(), BORDER, out);
        styleOut("border-right", style.getBorderRight(), BORDER, out);
        styleOut("border-top", style.getBorderTop(), BORDER, out);
        styleOut("border-bottom", style.getBorderBottom(), BORDER, out);
    }
    
    private void fontStyle(CellStyle style, Formatter out)
    {
        Font font = workbook.getFontAt(style.getFontIndex());
        
        if (font.getBoldweight() >= HSSFFont.BOLDWEIGHT_BOLD)
            out.format("  font-weight: bold;%n");
        if (font.getItalic())
            out.format("  font-style: italic;%n");
            
        int fontheight = font.getFontHeightInPoints();
        if (fontheight == 9)
        {
            //fix for stupid ol Windows
            fontheight = 10;
        }
        out.format("  font-size: %dpt;%n", fontheight);
        
        // Font color is handled with the other colors
    }
    
    private String styleName(CellStyle style)
    {
        if (style == null)
            style = workbook.getCellStyleAt((short) 0);
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        try
        {
            fmt.format("style_%02x", style.getIndex());
            return fmt.toString();
        }
        finally
        {
            fmt.close();
        }
    }
    
    private void printInlineStyle(Formatter out)
    {
        //out.format("<link href=\"excelStyle.css\" rel=\"stylesheet\" type=\"text/css\">%n");
        out.format("<style type=\"text/css\">%n");
        printStyles(out);
        out.format("</style>%n");
    }
    
    private void toHTML(OutputStream output) throws POITransformException
    {
        Formatter out = null;
        try
        {
            out = ensureOut(output);
            if (outputHTMLTag)
            {
                out.format(
                        "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Strict//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">%n");
                //                out.format("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>%n");
                out.format("<html>%n");
                out.format(
                        "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">%n");
                out.format("<head>%n");
                out.format("</head>%n");
                out.format("<body>%n");
            }
            
            printInlineStyle(out);
            printSheets(out);
            
            if (outputHTMLTag)
            {
                out.format("</body>%n");
                out.format("</html>%n");
            }
        }
        finally
        {
            if (out != null)
                out.close();
            if (output instanceof Closeable)
            {
                Closeable closeable = (Closeable) output;
                try
                {
                    closeable.close();
                }
                catch (IOException e)
                {
                    throw new POITransformException(e);
                }
            }
        }
    }
    
    private void printSheets(Formatter out)
    {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++)
        {
            Sheet sheet = workbook.getSheetAt(i);
            printSheet(sheet, out);
        }
    }
    
    public void printSheet(Sheet sheet, Formatter out)
    {
        out.format("<table class=\"%s\">%n", DEFAULTS_CLASS);
        printCols(sheet, out);
        printSheetContent(sheet, out);
        out.format("</table>%n");
    }
    
    private void printColumnHeads(Formatter out)
    {
        out.format("<thead>%n");
        out.format("  <tr class=\"%s\">%n", COL_HEAD_CLASS);
        out.format("    <th class=\"%s\">&#x25CA;</th>%n", COL_HEAD_CLASS);
        //noinspection UnusedDeclaration
        StringBuilder colName = new StringBuilder();
        for (int i = firstColumn; i < endColumn; i++)
        {
            colName.setLength(0);
            int cnum = i;
            do
            {
                colName.insert(0, (char) ('A' + cnum % 26));
                cnum /= 26;
            } while (cnum > 0);
            out.format("    <th class=\"%s\">%s</th>%n",
                    COL_HEAD_CLASS,
                    colName);
        }
        out.format("  </tr>%n");
        out.format("</thead>%n");
    }
    
    private void printSheetContent(Sheet sheet, Formatter out)
    {
        printColumnHeads(out);
        out.format("<tbody>%n");
        Iterator<Row> rows = sheet.rowIterator();
        while (rows.hasNext())
        {
            Row row = rows.next();
            
            out.format("  <tr>%n");
            out.format("    <td class=\"%s\">%d</td>%n",
                    ROW_HEAD_CLASS,
                    row.getRowNum() + 1);
            for (int i = firstColumn; i < endColumn; i++)
            {
                String content = "&nbsp;";
                String attrs = "";
                CellStyle style = null;
                if (i >= row.getFirstCellNum() && i < row.getLastCellNum())
                {
                    Cell cell = row.getCell(i);
                    if (cell != null)
                    {
                        style = cell.getCellStyle();
                        attrs = tagStyle(cell, style);
                        //Set the value that is rendered for the cell
                        //also applies the format
                        CellFormat cf = CellFormat
                                .getInstance(style.getDataFormatString());
                        CellFormatResult result = cf.apply(cell);
                        content = result.text;
                        if (content.equals(""))
                            content = "&nbsp;";
                    }
                }
                out.format("    <td class=\"%s %s\">%s</td>%n",
                        styleName(style),
                        attrs,
                        content);
            }
            out.format("  </tr>%n");
        }
        out.format("</tbody>%n");
    }
    
    private String tagStyle(Cell cell, CellStyle style)
    {
        if (style.getAlignment() == ALIGN_GENERAL)
        {
            switch (ultimateCellType(cell))
            {
                case HSSFCell.CELL_TYPE_STRING:
                    return "style=\"text-align: left;\"";
                case HSSFCell.CELL_TYPE_BOOLEAN:
                case HSSFCell.CELL_TYPE_ERROR:
                    return "style=\"text-align: center;\"";
                case HSSFCell.CELL_TYPE_NUMERIC:
                default:
                    // "right" is the default
                    break;
            }
        }
        return "";
    }
    
    private static int ultimateCellType(Cell c)
    {
        int type = c.getCellType();
        if (type == Cell.CELL_TYPE_FORMULA)
            type = c.getCachedFormulaResultType();
        return type;
    }
    
    private void printCols(Sheet sheet, Formatter out)
    {
        out.format("<col/>%n");
        ensureColumnBounds(sheet);
        for (int i = firstColumn; i < endColumn; i++)
        {
            out.format("<col/>%n");
        }
    }
    
    private void ensureColumnBounds(Sheet sheet)
    {
        if (gotBounds)
            return;
            
        Iterator<Row> iter = sheet.rowIterator();
        firstColumn = (iter.hasNext() ? Integer.MAX_VALUE : 0);
        endColumn = 0;
        while (iter.hasNext())
        {
            Row row = iter.next();
            short firstCell = row.getFirstCellNum();
            if (firstCell >= 0)
            {
                firstColumn = Math.min(firstColumn, firstCell);
                endColumn = Math.max(endColumn, row.getLastCellNum());
            }
        }
        gotBounds = true;
    }
    
    private void setupColorMap()
    {
        if (workbook instanceof HSSFWorkbook)
            helper = new HSSFHtmlHelper((HSSFWorkbook) workbook);
        else if (workbook instanceof XSSFWorkbook)
            helper = new XSSFHtmlHelper((XSSFWorkbook) workbook);
        else
            throw new IllegalArgumentException("unknown workbook type: "
                    + workbook.getClass().getSimpleName());
    }
    
    @Override
    public void toPDF(InputStream source, OutputStream output)
            throws POITransformException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void setPictureExactor(AbstractPictureExactor pictureExactor)
    {
        // TODO Auto-generated method stub
        
    }
    
    public boolean isOutputHTMLTag()
    {
        return outputHTMLTag;
    }
    
    public void setOutputHTMLTag(boolean outputHTMLTag)
    {
        this.outputHTMLTag = outputHTMLTag;
    }
    
}
