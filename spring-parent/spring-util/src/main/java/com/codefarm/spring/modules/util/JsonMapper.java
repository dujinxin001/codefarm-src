package com.codefarm.spring.modules.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

/**
 * 简单封装Jackson，实现JSON String<->Java Object的Mapper.
 * 
 * 封装不同的输出风格, 使用不同的builder函数创建实例.
 * 
 */
public class JsonMapper
{
    
    private static Logger logger = LoggerFactory.getLogger(JsonMapper.class);
    
    private ObjectMapper mapper;
    
    public JsonMapper()
    {
        this(null);
    }
    
    public JsonMapper(Include include)
    {
        mapper = new ObjectMapper();
        // 设置输出时包含属性的风格
        if (include != null)
        {
            mapper.setSerializationInclusion(include);
        }
        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
    
    /**
     * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
     */
    public static JsonMapper nonEmptyMapper()
    {
        return new JsonMapper(Include.NON_EMPTY);
    }
    
    /**
     * 创建只输出初始值被改变的属性到Json字符串的Mapper, 最节约的存储方式，建议在内部接口中使用。
     */
    public static JsonMapper nonDefaultMapper()
    {
        return new JsonMapper(Include.NON_DEFAULT);
    }
    
    /**
     * Object可以是POJO，也可以是Collection或数组。
     * 如果对象为Null, 返回"null".
     * 如果集合为空集合, 返回"[]".
     */
    public String toJson(Object object)
    {
        
        try
        {
            return mapper.writeValueAsString(object);
        }
        catch (IOException e)
        {
            logger.warn("write to json string error:" + object, e);
            return null;
        }
    }
    
    /**
     * 反序列化POJO或简单Collection如List<String>.
     * 
     * 如果JSON字符串为Null或"null"字符串, 返回Null.
     * 如果JSON字符串为"[]", 返回空集合.
     * 
     * 如需反序列化复杂Collection如List<MyBean>, 请使用fromJson(String, JavaType)
     * 
     * @see #fromJson(String, JavaType)
     */
    public <T> T fromJson(String jsonString, Class<T> clazz)
    {
        if (StringUtils.isEmpty(jsonString))
        {
            return null;
        }
        
        try
        {
            return mapper.readValue(jsonString, clazz);
        }
        catch (IOException e)
        {
            logger.warn("parse json string error:" + jsonString, e);
            return null;
        }
    }
    
    /**
     * 反序列化复杂Collection如List<Bean>, 先使用createCollectionType()或contructMapType()构造类型, 然后调用本函数.
     * 
     * @see #createCollectionType(Class, Class...)
     */
    public <T> T fromJson(String jsonString, JavaType javaType)
    {
        if (StringUtils.isEmpty(jsonString))
        {
            return null;
        }
        
        try
        {
            return (T) mapper.readValue(jsonString, javaType);
        }
        catch (IOException e)
        {
            logger.warn("parse json string error:" + jsonString, e);
            return null;
        }
    }
    
    /**
     * 构造Collection类型.
     */
    public JavaType contructCollectionType(
            Class<? extends Collection> collectionClass, Class<?> elementClass)
    {
        return mapper.getTypeFactory().constructCollectionType(collectionClass,
                elementClass);
    }
    
    /**
     * 构造Map类型.
     */
    public JavaType contructMapType(Class<? extends Map> mapClass,
            Class<?> keyClass, Class<?> valueClass)
    {
        return mapper.getTypeFactory().constructMapType(mapClass,
                keyClass,
                valueClass);
    }
    
    /**
     * 当JSON里只含有Bean的部分屬性時，更新一個已存在Bean，只覆蓋該部分的屬性.
     */
    public void update(String jsonString, Object object)
    {
        try
        {
            mapper.readerForUpdating(object).readValue(jsonString);
        }
        catch (JsonProcessingException e)
        {
            logger.warn("update json string:" + jsonString + " to object:"
                    + object + " error.", e);
        }
        catch (IOException e)
        {
            logger.warn("update json string:" + jsonString + " to object:"
                    + object + " error.", e);
        }
    }
    
    /**
     * 輸出JSONP格式數據.
     */
    public String toJsonP(String functionName, Object object)
    {
        return toJson(new JSONPObject(functionName, object));
    }
    
    /**
     * 設定是否使用Enum的toString函數來讀寫Enum,
     * 為False時時使用Enum的name()函數來讀寫Enum, 默認為False.
     * 注意本函數一定要在Mapper創建後, 所有的讀寫動作之前調用.
     */
    public void enableEnumUseToString()
    {
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    }
    
    /**
     * 支持使用Jaxb的Annotation，使得POJO上的annotation不用与Jackson耦合。
     * 默认会先查找jaxb的annotation，如果找不到再找jackson的。
     */
    public void enableJaxbAnnotation()
    {
        JaxbAnnotationModule module = new JaxbAnnotationModule();
        mapper.registerModule(module);
    }
    
    /**
     * 取出Mapper做进一步的设置或使用其他序列化API.
     */
    public ObjectMapper getMapper()
    {
        return mapper;
    }
    
    public static void main(String... args)
            throws JsonProcessingException, IOException
    {
        JsonMapper mapper = JsonMapper.nonEmptyMapper();
        CJ30 cj30 = new CJ30();
        Map<String, String> name = cj30.getName();
        name.put("2", "铜");
        name.put("3", "铝");
        name.put("4", "铅");
        name.put("5", "锌");
        name.put("7", "锡");
        name.put("8", "镍");
        Map<String, Map<String, Data>> data = cj30.getData();
        Map<String, Data> subject1 = new HashMap<String, Data>();
        Data d1 = new Data();
        d1.setDate("01/14");
        d1.setMin(41700);
        d1.setMax(41780);
        d1.setAverage(41740);
        subject1.put("1421164800", d1);
        
        Data d2 = new Data();
        d2.setDate("01/15");
        d2.setMin(41550);
        d2.setMax(41620);
        d2.setAverage(41585);
        subject1.put("1421251200", d2);
        data.put("2", subject1);
        
        Map<String, Data> subject2 = new HashMap<String, Data>();
        Data d3 = new Data();
        d3.setDate("01/14");
        d3.setMin(12450);
        d3.setMax(12490);
        d3.setAverage(12470);
        subject2.put("1421164800", d3);
        
        Data d4 = new Data();
        d4.setDate("01/15");
        d4.setMin(12730);
        d4.setMax(12770);
        d4.setAverage(12750);
        subject2.put("1421251200", d4);
        data.put("3", subject2);
        String json = mapper.toJson(cj30);
        System.out.println(json);
        
        FileReader reader = new FileReader(new File("E:\\cj30.js"));
        char[] buffer = new char[1024];
        int read = 0;
        StringBuilder sb = new StringBuilder();
        while ((read = reader.read(buffer)) > 0)
        {
            sb.append(buffer, 0, read);
        }
        CJ30 fromJson = mapper.fromJson(sb.toString(), CJ30.class);
        System.out.println(fromJson.getName());
    }
    
}

class CJ30
{
    private Map<String, String> name = new HashMap<String, String>();
    
    private Map<String, Map<String, Data>> data = new HashMap<String, Map<String, Data>>();
    
    public Map<String, String> getName()
    {
        return name;
    }
    
    public void setName(Map<String, String> name)
    {
        this.name = name;
    }
    
    public Map<String, Map<String, Data>> getData()
    {
        return data;
    }
    
    public void setData(Map<String, Map<String, Data>> data)
    {
        this.data = data;
    }
    
}

class Data
{
    private String date;
    
    private Integer min;
    
    private Integer max;
    
    private Integer average;
    
    public String getDate()
    {
        return date;
    }
    
    public void setDate(String date)
    {
        this.date = date;
    }
    
    public Integer getMin()
    {
        return min;
    }
    
    public void setMin(Integer min)
    {
        this.min = min;
    }
    
    public Integer getMax()
    {
        return max;
    }
    
    public void setMax(Integer max)
    {
        this.max = max;
    }
    
    public Integer getAverage()
    {
        return average;
    }
    
    public void setAverage(Integer average)
    {
        this.average = average;
    }
    
}