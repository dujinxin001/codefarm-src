package com.codefarm.mybatis.orm.enums;

public enum Operator
{
    EQUAL("="), GREATER(">"), LESS("<"), GREATERANDEQUAL(">="), LESSANDEQUAL(
            "<="), LIKE("like");
    private String operator;
    
    private Operator(String operator)
    {
        this.operator = operator;
    }
    
    public String getOperator()
    {
        return operator;
    }
    
}
