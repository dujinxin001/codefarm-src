package com.codefarm.mybatis.dialect;

public class MySql5Dialect extends Dialect
{
    
    protected static final String SQL_END_DELIMITER = ";";
    
    public String getLimitString(String sql, boolean hasOffset)
    {
        return MySql5PageHepler.getLimitString(sql, -1, -1);
    }
    
    public String getLimitString(String sql, int offset, int limit)
    {
        return MySql5PageHepler.getLimitString(sql, offset, limit);
    }
    
    public boolean supportsLimit()
    {
        return true;
    }
    
    @Override
    public String getCountSQL(String sql)
    {
        return MySql5PageHepler.getCountString(sql);
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
    
    public MySql5Dialect()
    {
        super();
        setType(Dialect.Type.MYSQL);
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
        StringBuffer sql = new StringBuffer();
        sql.append("select ");
        sql.append(cfg.getIdColumn());
        sql.append(" from ");
        sql.append(cfg.getTable());
        sql.append(" where ");
        sql.append(cfg.getDelimiterColumn());
        sql.append("=?");
        return sql.toString();
    }
    
    @Override
    public String getIdInitSQL(IDCfg cfg)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("insert into ");
        sql.append(cfg.getTable());
        sql.append(" (");
        sql.append(cfg.getIdColumn());
        sql.append(",");
        sql.append(cfg.getDelimiterColumn());
        sql.append(") values (?,?)");
        return sql.toString();
    }
    
    @Override
    public String getIdIncrSQL(IDCfg cfg)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("update ");
        sql.append(cfg.getTable());
        sql.append(" set ");
        sql.append(cfg.getIdColumn());
        sql.append("=");
        sql.append(cfg.getIdColumn());
        sql.append("+?");
        sql.append(" where ");
        sql.append(cfg.getDelimiterColumn());
        sql.append("=? and ");
        sql.append(cfg.getIdColumn());
        sql.append("=?");
        return sql.toString();
    }
}
