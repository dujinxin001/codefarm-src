package com.codefarm.mybatis.shard;

public class PrivateStaticTester
{
    
    public static void main(String[] args) throws InterruptedException
    {
        PrivateStatic A = new PrivateStatic();
        PrivateStatic B = new PrivateStatic();
        PrivateStaticThread threadA = new PrivateStaticThread(A, 1);
        PrivateStaticThread threadB = new PrivateStaticThread(B, 2);
        threadA.start();
        threadB.start();
        Thread.currentThread().sleep(1000);
        System.out.println(A.getA());
        System.out.println(B.getB());
    }
    
}
