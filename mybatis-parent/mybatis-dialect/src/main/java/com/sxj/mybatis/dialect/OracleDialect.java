package com.sxj.mybatis.dialect;

/**
 * 
 * @author tony.li
 * 
 */

public class OracleDialect extends Dialect
{
    
    public String getLimitString(String sql, int offset, int limit)
    {
        
        return OraclePageHelper.getLimitString(sql, offset, limit);
    }
    
    @Override
    public String getCountSQL(String sql)
    {
        return OraclePageHelper.getCountString(sql);
    }
    
    @Override
    public String getSnIncrSQL(SNCfg sn)
    {
        StringBuffer sb = new StringBuffer("update ");
        sb.append(sn.getTableName());
        sb.append(" set ");
        sb.append(sn.getSn());
        sb.append(" = ");
        sb.append(sn.getCurrent() + sn.getStep());
        //        sb.append(" + ");
        //        sb.append(sn.getStep());
        sb.append(" where ");
        sb.append(sn.getStub());
        sb.append(" = '");
        sb.append(sn.getStubValue());
        sb.append("'");
        sb.append(" and ");
        sb.append(sn.getSn());
        sb.append("=");
        sb.append(sn.getCurrent());
        return sb.toString();
    }
    
    public OracleDialect()
    {
        super();
        setType(Dialect.Type.ORACLE);
    }
    
    @Override
    public String getSnInitSQL(SNCfg sn)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("insert into ");
        sb.append(sn.getTableName());
        sb.append(" (");
        sb.append(sn.getStub());
        sb.append(",");
        sb.append(sn.getSn());
        sb.append(") values ('");
        sb.append(sn.getStubValue());
        sb.append("',0");
        sb.append(")");
        return sb.toString();
    }
    
    @Override
    public String getSnSelectSQL(SNCfg sn)
    {
        return "select " + sn.getSn() + " from " + sn.getTableName()
                + " where " + sn.getStub() + "='" + sn.getStubValue() + "'";
    }
    
    @Override
    public String getIdSelectSQL(IDCfg cfg)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getIdInitSQL(IDCfg cfg)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getIdIncrSQL(IDCfg cfg)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
