package com.ioigoume.eketamobilitytool.plot;

import java.util.ArrayList;
import java.util.List;

public class PointClass<T, K> {
	
	private List<Point<T, K>> pointsList = new ArrayList<Point<T, K>>();
	private String description;

	public PointClass(T x,K y, String description){
		pointsList.add(new Point<T, K>(x, y));
		this.description = description;
	}	
	
	/**
	 * @return String, the variable
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * The point list
	 * @return List, the point list
	 */
	public List<Point<T,K>> getPointsList() {
		return pointsList;
	}
	
	/**
	 * Number of Points in the List
	 * @return int, num of points in the list
	 */
	public int numOfPoints(){
		if(pointsList!=null)
			return pointsList.size();
		else
			return 0;
	}
	
	// Holds the points
	public class Point<A, B> {
		
		private A x;
		private B y;
		
		public Point( A x, B y)
		{
			this.x = x;
			this.y = y;
		}
		
		public A getX() {
			return x;
		}

		public B getY() {
			return y;
		}
		
	}
}
