package com.codefarm.mybatis.pagination;

public interface IPagable
{
    
    public abstract int getShowCount();
    
    public abstract void setShowCount(int showCount);
    
    public abstract int getTotalPage();
    
    public abstract void setTotalPage(int totalPage);
    
    public abstract int getTotalResult();
    
    public abstract void setTotalResult(int totalResult);
    
    public abstract int getCurrentPage();
    
    public abstract void setCurrentPage(int currentPage);
    
    public abstract boolean isPagable();
    
    public abstract void setPagable(boolean pagable);
    
}