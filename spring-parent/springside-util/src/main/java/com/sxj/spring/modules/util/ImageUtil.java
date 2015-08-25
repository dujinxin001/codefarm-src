/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sxj.spring.modules.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.omg.CORBA.SystemException;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * 图片操作类
 * 
 * @author Administrator
 */
public class ImageUtil
{
    
    /**
     * 按比例缩放图片
     * 
     * @param srcImage
     * @param rate
     * @return
     */
    public static byte[] scaleRate(byte[] file_buff, double rate,
            String file_ext_name)
    {
        return scaleRate(file_buff, rate, rate, file_ext_name, null);
    }
    
    /**
     * 按比例缩放图片
     * 
     * @param srcImage
     * @param width
     * @param height
     * @return
     */
    public static byte[] scaleRate(byte[] file_buff, int width, int height,
            String file_ext_name)
    {
        try
        {
            Dimension dimension = getDimension(file_buff);
            double w = dimension.getWidth();
            double h = dimension.getHeight();
            if ((w < width) && (h < height))
            {
                return file_buff;
            }
            if (height == 0)
            {
                if (w <= width)
                {
                    return file_buff;
                }
                return scaleRate(file_buff,
                        width / w,
                        width / w,
                        file_ext_name,
                        null);
            }
            if (width == 0)
            {
                if (h <= height)
                {
                    return file_buff;
                }
                return scaleRate(file_buff,
                        height / h,
                        height / h,
                        file_ext_name,
                        null);
            }
            if (w / h > width / height)
            {
                return scaleRate(file_buff,
                        width / w,
                        width / w,
                        file_ext_name,
                        null);
            }
            return scaleRate(file_buff,
                    height / h,
                    height / h,
                    file_ext_name,
                    null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 按比例缩放图片
     * 
     * @param srcImage
     * @param xscale
     * @param yscale
     * @param hints
     * @return
     */
    public static byte[] scaleRate(byte[] file_buff, double xscale,
            double yscale, String file_ext_name, RenderingHints hints)
    {
        try
        {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    file_buff);
            BufferedImage srcImage = ImageIO.read(inputStream);
            int width = (int) (srcImage.getWidth() * xscale);
            int height = (int) (srcImage.getHeight() * yscale);
            BufferedImage image = new BufferedImage(width, height, 1);
            image.getGraphics().drawImage(srcImage.getScaledInstance(width,
                    height,
                    16),
                    0,
                    0,
                    null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(image);
            ImageIO.write(image, file_ext_name, out);
            return out.toByteArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 按指定宽高缩放图片
     * 
     * @param srcImage
     * @param width
     * @param height
     * @param changeRate
     * @return
     * @throws IOException 
     * @throws SystemException 
     */
    public static BufferedImage scaleFixed(InputStream input,
            OutputStream output, int width, int height, String file_ext_name,
            boolean changeRate) throws IOException
    {
        if (input == null)
        {
            return null;
        }
        if (width <= 0 || height <= 0)
        {
            return null;
        }
        BufferedImage srcImage = ImageIO.read(input);
        //int oldWidth = srcImage.getWidth();
        //int oldHeight = srcImage.getHeight();
        //            if (oldWidth == width && oldHeight == height)
        //            {
        //                ImageIO.write(toImage, file_ext_name, out);// 输出到文件流
        //                return file_buff;
        //            }
        Image from = srcImage.getScaledInstance(srcImage.getWidth(),
                srcImage.getHeight(),
                BufferedImage.SCALE_DEFAULT);
        
        BufferedImage toImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = toImage.createGraphics();
        
        if (file_ext_name.toLowerCase().equals("png"))
        {
            toImage = g2d.getDeviceConfiguration().createCompatibleImage(width,
                    height,
                    Transparency.TRANSLUCENT);
            g2d.dispose();
            g2d = toImage.createGraphics();
        }
        else if (file_ext_name.toLowerCase().equals("gif"))
        {
            toImage = g2d.getDeviceConfiguration().createCompatibleImage(width,
                    height,
                    Transparency.TRANSLUCENT);
            g2d.dispose();
            g2d = toImage.createGraphics();
        }
        g2d.drawImage(from, 0, 0, width, height, null);
        ImageIO.write(toImage, file_ext_name, output);// 输出到文件流
        return toImage;
    }
    
    /**
     * 按指定宽高缩放图片文件
     * 
     * @param srcFile
     * @param destFile
     * @param width
     * @param height
     * @return
     */
    public static void scaleFixedImageFile(String srcFile, String destFile,
            int width, int height) throws IOException
    {
        scaleFixedImageFile(srcFile, destFile, width, height, true);
    }
    
    /**
     * 按指定宽高缩放图片文件
     * 
     * @param srcFile
     * @param destFile
     * @param width
     * @param height
     * @param changeRate
     * @return
     */
    public static void scaleFixedImageFile(String srcFile, String destFile,
            int width, int height, boolean changeRate) throws IOException
    {
        // File f = new File(srcFile);
        // try {
        // // BufferedImage image = ImageIO.read(f);
        // // BufferedImage newImage = scaleFixed(image, width, height,
        // // changeRate);
        // // writeImageFile(destFile, newImage);
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }
    
    /**
     * 按比例缩放图片文件
     * 
     * @param srcFile
     * @param destFile
     * @param width
     * @param height
     * @return
     */
    // public static void scaleRateImageFile(String srcFile, String destFile,
    // int width, int height) throws IOException {
    // File f = new File(srcFile);
    // try {
    // BufferedImage image = ImageIO.read(f);
    // BufferedImage newImage = scaleRate(image, width, height);
    // writeImageFile(destFile, newImage);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    
    /**
     * 按比例缩放图片文件
     * 
     * @param srcFile
     * @param destFile
     * @param rate
     * @return
     */
    // public static void scaleRateImageFile(String srcFile, String destFile,
    // double rate) throws IOException {
    // File f = new File(srcFile);
    // try {
    // BufferedImage image = ImageIO.read(f);
    // BufferedImage newImage = scaleRate(image, rate);
    // writeImageFile(destFile, newImage);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    
    /**
     * 获取灰度图
     * 
     * @param srcImage
     * @return
     */
    public static byte[] gray(byte[] file_buff)
    {
        try
        {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    file_buff);
            BufferedImage srcImage = ImageIO.read(inputStream);
            BufferedImage dstImage = new BufferedImage(srcImage.getWidth(),
                    srcImage.getHeight(), srcImage.getType());
            Graphics2D g2 = dstImage.createGraphics();
            RenderingHints hints = g2.getRenderingHints();
            g2.dispose();
            ColorSpace grayCS = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            ColorConvertOp colorConvertOp = new ColorConvertOp(grayCS, hints);
            colorConvertOp.filter(srcImage, dstImage);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(dstImage);
            ImageIO.write(dstImage, "jpg", out);
            return out.toByteArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取灰度图文件
     * 
     * @param srcFile
     * @param destFile
     * @return
     */
    // public static void grayImageFile(String srcFile, String destFile)
    // throws IOException {
    // writeImageFile(destFile, gray(ImageIO.read(new File(srcFile))));
    // }
    
    /**
     * 写图片文件
     * 
     * @param fileName
     * @param image
     * @return
     */
    // public static void writeImageFile(String fileName, BufferedImage image)
    // throws IOException {
    // FileOutputStream fos = new FileOutputStream(fileName);
    // if (fileName.toLowerCase().endsWith(".gif")) {
    // GIFEncoder gif = new GIFEncoder(image, fos);
    // gif.encode();
    // // Jimi.createJimiWriter(arg0, arg1)
    // }
    // if ((fileName.toLowerCase().endsWith(".jpg"))
    // || (fileName.toLowerCase().endsWith(".jpeg"))) {
    // JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fos);
    // JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
    // param.setQuality(1.0F, false);
    // encoder.encode(image);
    // }
    // fos.flush();
    // fos.close();
    // }
    
    /**
     * 获取图片尺寸（高、宽）
     * 
     * @param fileName
     * @return
     */
    public static Dimension getDimension(String fileName) throws IOException
    {
        File f = new File(fileName);
        return getDimension(f);
    }
    
    /**
     * 获取图片尺寸（高、宽）
     * 
     * @param f
     * @return
     */
    public static Dimension getDimension(File f) throws IOException
    {
        BufferedImage image = ImageIO.read(f);
        if (image == null)
        {
            return new Dimension(0, 0);
        }
        return new Dimension(image.getWidth(), image.getHeight());
    }
    
    /**
     * 获取图片尺寸（高、宽）
     * 
     * @param f
     * @return
     */
    public static Dimension getDimension(byte[] buff) throws IOException
    {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buff);
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null)
        {
            return new Dimension(0, 0);
        }
        return new Dimension(image.getWidth(), image.getHeight());
    }
    
    /**
     * 图片水印
     * 
     * @param file
     *            <需要打水印的图片文件>
     * @param pressImg
     *            <水印图片路径>
     * @param position
     *            <水印位置,1:左上,2:上中,3:上右,4:左中,5:中,6:右中,7:左下,8:下中,9:右下>
     */
    // public static final void pressImage(File file, String pressImg, int
    // position) {
    // try {
    // Image src = ImageIO.read(file);
    // int width = src.getWidth(null);
    // int height = src.getHeight(null);
    // if ((width <= 300) && (height <= 300)) {
    // return;
    // }
    // BufferedImage image = new BufferedImage(width, height, 1);
    // Graphics g = image.createGraphics();
    // g.drawImage(src, 0, 0, width, height, null);
    // File file_press = new File(pressImg);
    // if (!(file_press.exists())) {
    // com.eqt.base.utils.Logger.warn("水印图片不存在:" + pressImg,
    // ImageUtil.class);
    // return;
    // }
    // Image src_press = ImageIO.read(file_press);
    // int width_press = src_press.getWidth(null);
    // int height_press = src_press.getHeight(null);
    // int x = 0;
    // int y = 0;
    // int bianju = 20;
    // int[][][] positions = {
    // { { bianju, bianju },
    // { (width - width_press) / 2, bianju },
    // { width - width_press - bianju, bianju } },
    // {
    // { bianju, (height - height_press) / 2 },
    // { (width - width_press) / 2,
    // (height - height_press) / 2 },
    // { width - width_press - bianju,
    // (height - height_press) / 2 } },
    // {
    // { bianju, height - height_press - bianju },
    // { (width - width_press) / 2,
    // height - height_press - bianju },
    // { width - width_press - bianju,
    // height - height_press - bianju } } };
    // if (position == 0) {
    // position = NumberUtil.getRandomInt(9) + 1;
    // }
    // x = positions[((position - 1) / 3)][((position - 1) % 3)][0];
    // y = positions[((position - 1) / 3)][((position - 1) % 3)][1];
    // g.drawImage(src_press, x, y, width_press, height_press, null);
    // g.dispose();
    // FileOutputStream out = new FileOutputStream(file);
    // JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
    // encoder.encode(image);
    // out.close();
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    
    /**
     * 图片水印 *
     * 
     * @param targetImg
     *            <需要打水印的图片文件路径>
     * @param pressImg
     *            <水印图片路径>
     * @param position
     *            <水印位置,1:左上,2:上中,3:上右,4:左中,5:中,6:右中,7:左下,8:下中,9:右下>
     * @return
     */
    // public static final void pressImage(String targetImg, String pressImg,
    // int position) {
    // File file = new File(targetImg);
    // pressImage(file, pressImg, position);
    // }
    
    /**
     * 图片水印 默认水印位置为右下
     * 
     * @param targetImg
     *            <需要打水印的图片文件路径>
     * @param pressImg
     *            <水印图片路径>
     * @return
     */
    // public static final void pressImage(String targetImg, String pressImg) {
    // pressImage(targetImg, pressImg, 9);
    // }
    
    /**
     * 图片水印 默认水印位置为右下
     * 
     * @param file
     *            <需要打水印的图片文件>
     * @param pressImg
     *            <水印图片路径>
     */
    // public static final void pressImage(File file, String pressImg) {
    // pressImage(file, pressImg, 9);
    // }
    
    /**
     * 文字水印
     * 
     * @param file
     *            <需要水印的图片文件>
     * @param pressText
     *            <水印文字>
     * @param fontName
     *            <文字字体>
     * @param fontStyle
     *            <文字风格，如:斜体，加粗等>
     * @param color
     *            <文字颜色>
     * @param fontSize
     *            <文字大小>
     * @param position
     *            <水印位置,1:左上,2:上中,3:上右,4:左中,5:中,6:右中,7:左下,8:下中,9:右下>
     */
    public static byte[] pressTextToImage(byte[] imagebuff, String pressText,
            String fontName, int fontStyle, int color, int fontSize,
            int position)
    {
        try
        {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    imagebuff);
            Image src = ImageIO.read(inputStream);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            // if ((width <= 300) && (height <= 300)) {
            // return null;
            // }
            BufferedImage image = new BufferedImage(width, height, 1);
            Graphics g = image.createGraphics();
            g.drawImage(src, 0, 0, null);
            g.setColor(new Color(color));
            g.setFont(new Font(fontName, fontStyle, fontSize));
            int x;
            int y;
            int bianju = 60;
            int[][][] positions = {
                    {
                            { bianju, bianju },
                            { (width - (pressText.length() * fontSize)) / 2,
                                    bianju },
                            { width - (pressText.length() * fontSize) - bianju,
                                    bianju } },
                    {
                            { bianju, (height - fontSize) / 2 },
                            { (width - (pressText.length() * fontSize)) / 2,
                                    (height - fontSize) / 2 },
                            { width - (pressText.length() * fontSize) - bianju,
                                    (height - fontSize) / 2 } },
                    {
                            { bianju, height - fontSize - bianju },
                            { (width - (pressText.length() * fontSize)) / 2,
                                    height - fontSize - bianju },
                            { width - (pressText.length() * fontSize) - bianju,
                                    height - fontSize - bianju } } };
            if (position == 0)
            {
                position = com.sxj.spring.modules.util.NumberUtils.getRandomIntInMax(9) + 1;
            }
            x = positions[((position - 1) / 3)][((position - 1) % 3)][0];
            y = positions[((position - 1) / 3)][((position - 1) % 3)][1];
            g.drawString(pressText, x, y);
            g.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(image);
            ImageIO.write(image, "jpg", out);
            out.close();
            return out.toByteArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 压缩图片文件
     * 
     * @param targetImg
     *            <需要水印的图片文件路径>
     * @param pressText
     *            <水印文字>
     * @param fontName
     *            <文字字体>
     * @param fontStyle
     *            <文字风格，如:斜体，加粗等>
     * @param color
     *            <文字颜色>
     * @param fontSize
     *            <文字大小>
     * @param position
     *            <水印位置,1:左上,2:上中,3:上右,4:左中,5:中,6:右中,7:左下,8:下中,9:右下>
     * @return
     */
    // public static void pressText(String targetImg, String pressText,
    // String fontName, int fontStyle, int color, int fontSize,
    // int position) {
    // try {
    // File file = new File(targetImg);
    // pressText(file, pressText, fontName, fontStyle, color, fontSize,
    // position);
    // } catch (Exception e) {
    // System.out.println(e);
    // }
    // }
    
    public static void main(String[] args)
    {
        try
        {
            long aa = System.currentTimeMillis();
            scaleFixedImageFile("D:/My Documents/My Pictures/279106_hyFFzuu8_o.jpg",
                    "D:/My Documents/My Pictures/dq5nds_001_small2.jpg",
                    100,
                    100,
                    false);
            System.out.println(System.currentTimeMillis() - aa);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
