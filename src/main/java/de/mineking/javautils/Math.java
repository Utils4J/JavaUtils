package de.mineking.javautils;

public class Math {
	public static int gcd(int a, int b) {
		return b == 0 ? a : gcd(b, b % a);
	}

	public static int lcm(int a, int b) {
		return (a * b) / gcd(a, b);
	}
}
