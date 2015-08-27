package com.sxj.poi.transformer;

import org.apache.poi.hwpf.usermodel.PictureType;

public interface IPictureExactor
{
    /**
     * 保存图片，并返回绝对路径
     * @param content
     * @param pictureType
     * @param suggestedName
     * @param widthInches
     * @param heightInches
     * @return
     */
    abstract String save(byte[] content, PictureType pictureType,
            String suggestedName, float widthInches, float heightInches);
}
