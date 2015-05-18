package com.example.robotwasd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

/**
 * class for holding the information about a contour
 * @author daniel
 *
 */
public class Contour {
	
	private Color color;
	private Scalar colorHSV;
	private Point topmostPoint;
	private Point lowestPoint;
	private Point leftmostPoint;
	private Point rightmostPoint;
	private double middlePointX; 
	
	public Contour() {
		color = Color.Red;
		topmostPoint = new Point(0, 1000);
		lowestPoint = new Point(0,0);
		leftmostPoint = new Point(1000, 0);
		rightmostPoint = new Point(0,0);
		middlePointX = 0;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}

	public Point getTopmostPoint() {
		return topmostPoint;
	}
	
	public Point getLowestPoint() {
		return lowestPoint;
	}

	public Point getLeftmostPoint() {
		return leftmostPoint;
	}

	public Point getRightmostPoint() {
		return rightmostPoint;
	}
	
	public double getMiddlePointX() {
		return middlePointX;
	}

	public Scalar getColorHSV() {
		return colorHSV;
	}

	public void setColorHSV(Scalar colorHSV) {
		this.colorHSV = colorHSV;
	}

	/*
	 * Display cooridnate system:
	 *  (0,0) at top left-hand corner
	 *  horizontal x-axis
	 *  vertical y-axis
	 *  
	 *  topmost point: lowest y value
	 *  lowest point: hightest y value
	 *  leftmost point: lowest x value
	 *  rightmost point: highest x value
	 * 
	 */
	public void calcExtremePoints(MatOfPoint points, Color color) {
		this.color = color;
		
		List<Point> contList = points.toList();
        Iterator<Point> iterator = contList.iterator();
        while(iterator.hasNext()){
        	Point p = iterator.next();
        	
        	if (p.y < topmostPoint.y) {
        		topmostPoint = p;
        	}
        	if (p.y > lowestPoint.y) {
        		lowestPoint = p;
        	}
        	if (p.x < leftmostPoint.x) {
        		leftmostPoint = p;
        	}
        	if (p.x > rightmostPoint.x) {
        		rightmostPoint = p;
        	}
        	
        }
        
        middlePointX = (leftmostPoint.x + rightmostPoint.x ) / 2;
	}
	
	/*
	 * Display cooridnate system:
	 *  (0,0) at top left-hand corner
	 *  horizontal x-axis
	 *  vertical y-axis
	 *  
	 *  topmost point: lowest y value
	 *  lowest point: hightest y value
	 *  leftmost point: lowest x value
	 *  rightmost point: highest x value
	 * 
	 */
	public void calcExtremePoints(MatOfPoint points, Scalar color) {
		this.colorHSV = color;
		
		List<Point> contList = points.toList();
        Iterator<Point> iterator = contList.iterator();
        while(iterator.hasNext()){
        	Point p = iterator.next();
        	
        	if (p.y < topmostPoint.y) {
        		topmostPoint = p;
        	}
        	if (p.y > lowestPoint.y) {
        		lowestPoint = p;
        	}
        	if (p.x < leftmostPoint.x) {
        		leftmostPoint = p;
        	}
        	if (p.x > rightmostPoint.x) {
        		rightmostPoint = p;
        	}
        	
        }
        
        middlePointX = (leftmostPoint.x + rightmostPoint.x ) / 2;
	}
	
	/**
	 * Calculates for a list of image-contours (of 1 color) the interesting extreme points
	 * and returns them as a list of contour objects.
	 * 
	 * @param contours Contours to be examined
	 * @param color Color of the contours
	 * @return
	 */
	public static List<Contour> processContours(List<MatOfPoint> contours, Color color) {
		List<Contour> listOfContours = new ArrayList<Contour>();
		for (MatOfPoint points : contours) {
			Contour contour = new Contour();
			contour.calcExtremePoints(points, color);
			listOfContours.add(contour);
		}
		return listOfContours;
	}
	
	/**
	 * Calculates for a list of image-contours (of 1 color) the interesting extreme points
	 * and returns them as a list of contour objects.
	 * 
	 * @param contours Contours to be examined
	 * @param color Scalar color
	 * @return
	 */
	public static List<Contour> processContours(List<MatOfPoint> contours, Scalar color) {
		List<Contour> listOfContours = new ArrayList<Contour>();
		for (MatOfPoint points : contours) {
			Contour contour = new Contour();
			contour.calcExtremePoints(points, color);
			listOfContours.add(contour);
		}
		return listOfContours;
	}

}
