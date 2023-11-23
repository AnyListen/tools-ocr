package com.benjaminwan.ocrlibrary;

public final class Point {
  private int x;
  private int y;

  public Point copy(int x, int y) {
    return new Point(x, y);
  }

  public String toString() {
    return "Point(x=" + this.x + ", y=" + this.y + ')';
  }

  public int hashCode() {
    int result = Integer.hashCode(this.x);
    return (result * 31) + Integer.hashCode(this.y);
  }

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Point)) {
      return false;
    }
    Point point = (Point) other;
    return this.x == point.x && this.y == point.y;
  }

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return this.x;
  }

  public void setX(int i) {
    this.x = i;
  }

  public int getY() {
    return this.y;
  }

  public void setY(int i) {
    this.y = i;
  }
}