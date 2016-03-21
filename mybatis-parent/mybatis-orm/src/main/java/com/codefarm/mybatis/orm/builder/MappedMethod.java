package com.codefarm.mybatis.orm.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.codefarm.mybatis.orm.annotations.Table;
import com.codefarm.spring.modules.util.CaseFormatUtils;

/**
 * mybatis注册dao方法
 * @author zhangjian
 *
 */
public class MappedMethod
{
    private Class<?> entityType = null;
    
    private String table;
    
    private java.util.List<Field> columnFields = null;
    
    private Field idField = null;
    
    private Field versionField = null;
    
    private Method method;
    
    public MappedMethod(Class<?> entityType, List<Field> columnFields,
            Field idField)
    {
        super();
        this.entityType = entityType;
        Table t = entityType.getAnnotation(Table.class);
        //默认表名，大写字母转为下划线：TestUserEntity==>test_user_entity
        if (t == null)
        {
            table = CaseFormatUtils
                    .camelToUnderScore(entityType.getSimpleName());
        }
        else
        {
            table = t.name();
        }
        this.columnFields = columnFields;
        this.idField = idField;
    }
    
    public Method getMethod()
    {
        return method;
    }
    
    public void setMethod(Method method)
    {
        this.method = method;
    }
    
    public Class<?> getEntityType()
    {
        return entityType;
    }
    
    public Field getIdField()
    {
        return idField;
    }
    
    public java.util.List<Field> getColumnFields()
    {
        return columnFields;
    }
    
    public void setColumnFields(java.util.List<Field> columnFields)
    {
        this.columnFields = columnFields;
    }
    
    public void setEntityType(Class<?> entityType)
    {
        this.entityType = entityType;
    }
    
    public void setIdField(Field idField)
    {
        this.idField = idField;
    }
    
    public String getTable()
    {
        return table;
    }
    
    public Field getVersionField()
    {
        return versionField;
    }
    
    public void setVersionField(Field versionField)
    {
        this.versionField = versionField;
    }
    
}
