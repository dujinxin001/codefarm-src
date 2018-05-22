package com.codefarm.jsonrpc.client;

import java.net.MalformedURLException;

import java.net.URL;

import org.junit.AfterClass;
import org.junit.Test;

public class ContractServiceTest
{
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }
    
    @Test
    public void test() throws MalformedURLException
    {
        JsonRpcHttpClient client = new JsonRpcHttpClient(
                new URL(
                        "http://127.0.0.1:8080/supervisor-manager/api/service/contract.json"));
        
        //        IContractService contractService = com.sxj.jsonrpc.client.ProxyUtil2.createClientProxy(getClass().getClassLoader(),
        //                IContractService.class,
        //                client);
//        IContractService contractService = client.createProxy(IContractService.class);
//        ContractModel contract = contractService.getContract("cWFq45GJJSHiNx0M2MgzcT0mjNiu4j8h");
//        ContractEntity entity = contract.getContract();
//        System.out.println(entity.toString());
    }
    
}
