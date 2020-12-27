package dsekercioglu.mega.math;

import java.awt.geom.Point2D;

/**
 * Uses Robocode Trigonometry
 */
public class Vec2 extends Point2D {

	private double x;
	private double y;

	public Vec2() {

	}

	public Vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vec2(Point2D.Double point) {
		this.x = point.getX();
		this.y = point.getY();
	}

	@Override
	public String toString() {
		return "Vec2D[x: " + x + ", y: " + y + "]";
	}

	public Vec2 copy() {
		return new Vec2(x, y);
	}

	@Override
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double sqLength() {
		return x * x + y * y;
	}

	public double length() {
		return Math.sqrt(sqLength());
	}

	public double angleTo(Vec2 vec2) {
		return FastTrig.atan2(vec2.x - x, vec2.y - y);
	}

	public Vec2 normalize() {
		double length = length();
		x /= length;
		y /= length;
		return this;
	}

	public Vec2 mul(double d) {
		x *= d;
		y *= d;
		return this;
	}

	public Vec2 div(double d) {
		x /= d;
		y /= d;
		return this;
	}

	public Vec2 add(double d) {
		x += d;
		y += d;
		return this;
	}

	public Vec2 translate(Vec2 vec2) {
		x += vec2.x;
		y += vec2.y;
		return this;
	}

	//TODO: Fix This!
	public void rotate(Vec2 direction) {
		double temp = x;
		x = x * direction.getY() - y * direction.getX();
		y = temp * direction.getX() + y * direction.getY();
	}

	public void project(Vec2 direction, double length) {
		x += direction.x * length;
		y += direction.y * length;
	}


	public Vec2 negate() {
		x = -x;
		y = -y;
		return this;
	}

	public double angle() {
		return FastTrig.atan2(x, y);
	}

	public static double sqLength(Vec2 vec2) {
		return vec2.getX() * vec2.getX() + vec2.getY() * vec2.getY();
	}

	public static double length(Vec2 vec2) {
		return vec2.length();
	}

	public double dot(Vec2 vec2) {
		return x * vec2.x + y * vec2.y;
	}

	public double cross(Vec2 vec2) {
		return x * vec2.getY() - y * vec2.getX();
	}

	public static double angleTo(Vec2 v0, Vec2 v1) {
		return v0.angleTo(v1);
	}

	public static Vec2 normalize(Vec2 vec2) {
		return vec2.copy().normalize();
	}

	public static Vec2 direction(double angle) {
		return new Vec2(FastTrig.sin(angle), FastTrig.cos(angle));
	}

	public static Vec2 project(Vec2 vec2, Vec2 direction, double length) {
		return new Vec2(vec2.getX() + direction.x * length, vec2.getY() + direction.y * length);
	}

	public static Vec2 mul(Vec2 v, double d) {
		return new Vec2(v.getX() * d, v.getY() * d);
	}

	public static Vec2 div(Vec2 v, double d) {
		return new Vec2(v.getX() / d, v.getY() / d);
	}

	public static Vec2 add(Vec2 v, double d) {
		return new Vec2(v.getX() + d, v.getY() + d);
	}

	public static Vec2 translate(Vec2 v0, Vec2 v1) {
		return new Vec2(v0.getX() + v1.getX(), v0.getY() + v1.getY());
	}

	public static Vec2 rotate(Vec2 v, Vec2 direction) {
		return new Vec2(v.getX() * direction.getY() - v.getY() * direction.getX(),
				v.getX() * direction.getX() + v.getY() * direction.getY());
	}

	public static Vec2 negate(Vec2 v) {
		return new Vec2(-v.getX(), -v.getY());
	}

	public static double dot(Vec2 v0, Vec2 v1) {
		return v0.getX() * v1.getX() + v0.getY() * v1.getY();
	}

	public static double cross(Vec2 v0, Vec2 v1) {
		return v0.getX() * v1.getY() - v0.getY() * v1.getX();
	}

	public static double angle(Vec2 v) {
		return v.angle();
	}


}
