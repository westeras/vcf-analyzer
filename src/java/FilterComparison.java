import java.util.*;


public class FilterComparison {

	abstract class Comparison
	{
		public int identifier;
		public List<String> names;
		
		public abstract boolean compareInteger( String testValue, 
												String comparator,
												String comparator2 );
		
		public abstract boolean compareFloat( String testValue, 
												String comparator,
												String comparator2 );
		
		public abstract boolean compareString( String testValue, 
												String comparator,
												String comparator2 );
	}
	
	class EqualsComparison extends Comparison
	{
		
		public EqualsComparison()
		{
			identifier = 4;
			names = Arrays.asList("=", "equal to", "equals");
		}
		
		@Override
		public boolean compareInteger( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return (Integer.parseInt(testValue) == Integer.parseInt(comparator));
		}
		
		@Override
		public boolean compareFloat( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			float test = Float.parseFloat(testValue);
			float comp = Float.parseFloat(comparator);
			return (Float.compare(test, comp) == 0);
		}
		
		@Override
		public boolean compareString( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return testValue.equals(comparator);
		}
		
	}
	
	class NotEqualsComparison extends Comparison
	{
		
		public NotEqualsComparison()
		{
			identifier = 11;
			names = Arrays.asList("!=", "not equal to", "not equals");
		}
		
		@Override
		public boolean compareInteger( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return (Integer.parseInt(testValue) != Integer.parseInt(comparator));
		}
		
		@Override
		public boolean compareFloat( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			float test = Float.parseFloat(testValue);
			float comp = Float.parseFloat(comparator);
			return (Float.compare(test, comp) != 0);
		}
		
		@Override
		public boolean compareString( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return !testValue.equals(comparator);
		}
		
	}	

	class LessThanComparison extends Comparison
	{
		
		public LessThanComparison()
		{
			identifier = 0;
			names = Arrays.asList("<", "less than");
		}
		
		@Override
		public boolean compareInteger( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return (Integer.parseInt(testValue) < Integer.parseInt(comparator));
		}
		
		@Override
		public boolean compareFloat( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			float test = Float.parseFloat(testValue);
			float comp = Float.parseFloat(comparator);
			return (Float.compare(test, comp) < 0);
		}
		
		@Override
		public boolean compareString( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return (testValue.compareTo(comparator) <0);
		}
		
	}
	
	class GreaterThanComparison extends Comparison
	{
		
		public GreaterThanComparison()
		{
			identifier = 1;
			names = Arrays.asList(">", "greater than");
		}
		
		@Override
		public boolean compareInteger( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return (Integer.parseInt(testValue) > Integer.parseInt(comparator));
		}
		
		@Override
		public boolean compareFloat( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			float test = Float.parseFloat(testValue);
			float comp = Float.parseFloat(comparator);
			return (Float.compare(test, comp) > 0);
		}
		
		@Override
		public boolean compareString( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return (testValue.compareTo(comparator) > 0);
		}
		
	}	
	
	class LessThanEqualComparison extends Comparison
	{
		
		public LessThanEqualComparison()
		{
			identifier = 2;
			names = Arrays.asList("<=");
		}
		
		@Override
		public boolean compareInteger( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return (Integer.parseInt(testValue) <= Integer.parseInt(comparator));
		}
		
		@Override
		public boolean compareFloat( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			float test = Float.parseFloat(testValue);
			float comp = Float.parseFloat(comparator);
			return (Float.compare(test, comp) <= 0);
		}
		
		@Override
		public boolean compareString( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return (testValue.compareTo(comparator) <= 0);
		}
		
	}	
	
	class GreaterThanEqualComparison extends Comparison
	{
		
		public GreaterThanEqualComparison()
		{
			identifier = 3;
			names = Arrays.asList(">=");
		}
		
		@Override
		public boolean compareInteger( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return (Integer.parseInt(testValue) >= Integer.parseInt(comparator));
		}
		
		@Override
		public boolean compareFloat( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			float test = Float.parseFloat(testValue);
			float comp = Float.parseFloat(comparator);
			return (Float.compare(test, comp) >= 0);
		}
		
		@Override
		public boolean compareString( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null)
				return false;
			return (testValue.compareTo(comparator) >= 0);
		}
		
	}		
	
	class BetweenComparison extends Comparison
	{
		
		public BetweenComparison()
		{
			identifier = 5;
			names = Arrays.asList("between");
		}
		
		@Override
		public boolean compareInteger( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null || comparator2 == null)
				return false;
			int value = Integer.parseInt(testValue);
			return (value >= Integer.parseInt(comparator) ) 
					&& (value <= Integer.parseInt(comparator2) );
		}
		
		@Override
		public boolean compareFloat( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null || comparator2 == null)
				return false;
			float test = Float.parseFloat(testValue);
			float comp = Float.parseFloat(comparator);
			float comp2 = Float.parseFloat(comparator);
			return (Float.compare(test, comp) >= 0) && (Float.compare(test, comp2) <= 0);
		}
		
		@Override
		public boolean compareString( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null || comparator2 == null)
				return false;
			return (testValue.compareTo(comparator) >= 0) && (testValue.compareTo(comparator) <= 0);
		}
		
	}	

	class BetweenExculsiveComparison extends Comparison
	{
		
		public BetweenExculsiveComparison()
		{
			identifier = 6;
			names = Arrays.asList("between exclusive");
		}
		
		@Override
		public boolean compareInteger( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null || comparator2 == null)
				return false;
			int value = Integer.parseInt(testValue);
			return (value > Integer.parseInt(comparator) ) 
					&& (value < Integer.parseInt(comparator2) );
		}
		
		@Override
		public boolean compareFloat( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null || comparator2 == null)
				return false;
			float test = Float.parseFloat(testValue);
			float comp = Float.parseFloat(comparator);
			float comp2 = Float.parseFloat(comparator);
			return (Float.compare(test, comp) > 0) && (Float.compare(test, comp2) < 0);
		}
		
		@Override
		public boolean compareString( String testValue, 
												String comparator,
												String comparator2 )
		{
			if (testValue ==null || comparator == null || comparator2 == null)
				return false;
			return (testValue.compareTo(comparator) > 0) && (testValue.compareTo(comparator) < 0);
		}
		
	}		
	
	class ExistsComparison extends Comparison
	{
		
		public ExistsComparison()
		{
			identifier = 7;
			names = Arrays.asList("exists");
		}
		
		@Override
		public boolean compareInteger( String testValue, 
												String comparator,
												String comparator2 )
		{
			return testValue !=null;
		}
		
		@Override
		public boolean compareFloat( String testValue, 
												String comparator,
												String comparator2 )
		{
			return testValue !=null;
		}
		
		@Override
		public boolean compareString( String testValue, 
												String comparator,
												String comparator2 )
		{
			return testValue !=null;
		}
	}
	
	class NotExistsComparison extends Comparison
	{
		
		public NotExistsComparison()
		{
			identifier = 8;
			names = Arrays.asList("not exists");
		}
		
		@Override
		public boolean compareInteger( String testValue, 
												String comparator,
												String comparator2 )
		{
			return testValue ==null;
		}
		
		@Override
		public boolean compareFloat( String testValue, 
												String comparator,
												String comparator2 )
		{
			return testValue ==null;
		}
		
		@Override
		public boolean compareString( String testValue, 
												String comparator,
												String comparator2 )
		{
			return testValue ==null;
		}
	}
	
	private ArrayList<Comparison> allComparisons;
	
	public FilterComparison()
	{
		this.allComparisons = new ArrayList<Comparison>();
		this.allComparisons.add( new EqualsComparison() );
		this.allComparisons.add( new NotEqualsComparison() );
		this.allComparisons.add( new LessThanComparison() );
		this.allComparisons.add( new GreaterThanComparison() );
		this.allComparisons.add( new LessThanEqualComparison() );
		this.allComparisons.add( new GreaterThanEqualComparison() );
		this.allComparisons.add( new BetweenComparison() );
		this.allComparisons.add( new BetweenExculsiveComparison() );
		this.allComparisons.add( new ExistsComparison() );
		this.allComparisons.add( new NotExistsComparison() );		
	}

	public HashMap<String, Integer> getOperatorList() 
	{
		HashMap<String, Integer> operatorList = new HashMap<String, Integer>();
		
		for ( Comparison operation: this.allComparisons )
		{
			for( String name : operation.names )
			{
				operatorList.put(name, operation.identifier);
			}
		}
		return operatorList;
		
	}
	
	public boolean testFilterComparison( int type, FilterParameter filter, String testValue)
	{
		Comparison operation = null;
		for (Comparison test: this.allComparisons )
		{
			if (test.identifier == filter.comparison)
			{
				operation = test;
				break;
			}
		}
		
		if (operation == null)
		{
			throw new IllegalArgumentException("Invalid comparison");
		}
		
		switch (type)
		{
		case 0:
		{
			//int
			return operation.compareInteger(testValue, filter.comparator, filter.comparator2);
		}
		case 1:
		{
			//float
			return operation.compareFloat(testValue, filter.comparator, filter.comparator2);
		}
		case 5:
		{
			//string
			return operation.compareString(testValue, filter.comparator, filter.comparator2);
		}
		}
		throw new IllegalArgumentException("Invalid data type");
	}
}
