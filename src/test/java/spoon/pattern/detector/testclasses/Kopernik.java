package spoon.pattern.detector.testclasses;

public class Kopernik {
	class A {
		void mars() {
			System.out.println("I am Mars");
		}
	}
	class B {
		String saturn() {
			System.out.println("I am Saturn");
			return "Saturn";
		}
	}
	class C {
		int saturn() {
			System.out.println("I am Saturn");
			return "Saturn".length();
		}
	}
	class D {
		String saturn() {
			System.out.println("I am Saturn");
			return null;
		}
	}
	class E {
		Integer saturn() {
			System.out.println("I am Saturn");
			return null;
		}
	}
}
