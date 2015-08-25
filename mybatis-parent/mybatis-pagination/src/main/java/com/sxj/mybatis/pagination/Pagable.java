package com.sxj.mybatis.pagination;

public abstract class Pagable implements IPagable {
	private int showCount = 10; // 每页显示记录数
	private int totalPage; // 总页数
	private int totalResult; // 总记录数
	private int currentPage=1; // 当前页

	private boolean pagable = false;

	// private int currentResult; // 当前记录起始索引

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sxj.mybatis.pagination.IPagable#getShowCount()
	 */
	public int getShowCount() {
		return showCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sxj.mybatis.pagination.IPagable#setShowCount(int)
	 */
	public void setShowCount(int showCount) {
		this.showCount = showCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sxj.mybatis.pagination.IPagable#getTotalPage()
	 */
	public int getTotalPage() {
		return totalPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sxj.mybatis.pagination.IPagable#setTotalPage(int)
	 */
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sxj.mybatis.pagination.IPagable#getTotalResult()
	 */
	public int getTotalResult() {
		return totalResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sxj.mybatis.pagination.IPagable#setTotalResult(int)
	 */
	public void setTotalResult(int totalResult) {
		this.totalResult = totalResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sxj.mybatis.pagination.IPagable#getCurrentPage()
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sxj.mybatis.pagination.IPagable#setCurrentPage(int)
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sxj.mybatis.pagination.IPagable#isPagable()
	 */
	public boolean isPagable() {
		return pagable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sxj.mybatis.pagination.IPagable#setPagable(boolean)
	 */
	public void setPagable(boolean pagable) {
		this.pagable = pagable;
	}

	public void setPage(Pagable pagable) {
		this.currentPage = pagable.getCurrentPage();
		this.pagable = pagable.isPagable();
		this.showCount = pagable.getShowCount();
		this.totalPage = pagable.getTotalPage();
		this.totalResult = pagable.getTotalResult();

	}

}
