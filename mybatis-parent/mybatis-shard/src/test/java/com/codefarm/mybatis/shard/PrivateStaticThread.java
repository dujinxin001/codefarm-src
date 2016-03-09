package com.codefarm.mybatis.shard;

public class PrivateStaticThread extends Thread
{
    
    private PrivateStatic param;
    
    private Integer value;
    
    public PrivateStaticThread(PrivateStatic param, Integer value)
    {
        super();
        this.param = param;
        this.value = value;
    }
    
    @Override
    public void run()
    {
        param.setA(value);
        param.setB(value);
        System.out.println(param.getA() + "------" + param.getB());
    }
    
    public PrivateStatic getParam()
    {
        return param;
    }
    
    public void setParam(PrivateStatic param)
    {
        this.param = param;
    }
    
    public Integer getValue()
    {
        return value;
    }
    
    public void setValue(Integer value)
    {
        this.value = value;
    }
    
}
