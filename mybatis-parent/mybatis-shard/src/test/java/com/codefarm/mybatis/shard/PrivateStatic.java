package com.codefarm.mybatis.shard;


public class PrivateStatic
{
    private static int a = 0;
    
    private static ThreadLocal<Integer> b = new ThreadLocal<Integer>()
    {
        
        @Override
        protected Integer initialValue()
        {
            return 0;
        }
        
    };
    
    public static int getA()
    {
        return a;
    }
    
    public static void setA(int a)
    {
        PrivateStatic.a = a;
    }
    
    public static Integer getB()
    {
        return PrivateStatic.b.get();
    }
    
    public static void setB(Integer b)
    {
        PrivateStatic.b.set(b);
    }
}
