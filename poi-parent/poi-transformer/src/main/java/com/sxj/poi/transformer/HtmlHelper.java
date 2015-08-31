
package com.sxj.poi.transformer;

import java.util.Formatter;

import org.apache.poi.ss.usermodel.CellStyle;

public interface HtmlHelper
{
    void style(CellStyle style, Formatter out);
}
