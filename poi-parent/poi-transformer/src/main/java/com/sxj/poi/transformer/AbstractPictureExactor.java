package com.sxj.poi.transformer;

import java.net.URL;

import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.usermodel.PictureType;

/**
 * @author Administrator
 *
 */
public abstract class AbstractPictureExactor implements PicturesManager
{
    
    @Override
    public final String savePicture(byte[] content, PictureType pictureType,
            String suggestedName, float widthInches, float heightInches)
    {
        return save(content,
                pictureType,
                suggestedName,
                widthInches,
                heightInches).toString();
    }
    
    /**
     * 保存图片，并返回绝对路径
     * @param content
     * @param pictureType
     * @param suggestedName
     * @param widthInches
     * @param heightInches
     * @return
     */
    protected abstract URL save(byte[] content, PictureType pictureType,
            String suggestedName, float widthInches, float heightInches);
            
}
