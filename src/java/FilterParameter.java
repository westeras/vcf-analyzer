
public class FilterParameter {

	public String tableName;
	public int comparison;
	public String comparator;
	public String comparator2;
	public int failureAllow;
	public int passExactly;
	
	public FilterParameter(
							String tableName,
							int comparison,
							String comparator,
							String comparator2,
							int failureAllow,
							int passExactly)
	{
		this.tableName = tableName;
		this.comparison = comparison;
		this.comparator = comparator;
		this.comparator2 = comparator2;
		this.failureAllow = failureAllow;
		this.passExactly = passExactly;
	}
	
}
