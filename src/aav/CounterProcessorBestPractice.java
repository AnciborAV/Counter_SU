package aav;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class CounterProcessorBestPractice {

		private static final String INCREMENT = "/inc";
		private static final String STOP = "/stop";
		private static final String RESET = "/reset";

		private static final AtomicInteger counter = new AtomicInteger();

		public static void main(String[] args) {

				Scanner scanner = new Scanner(System.in);

				boolean isActive = true;

				while (isActive && scanner.hasNext()) {
						switch (scanner.next()) {
								case INCREMENT -> System.out.printf("New counter value is: %d\n", counter.incrementAndGet());
								case STOP -> {
										System.out.printf("I'm finishing work! Current value is: %d\n", counter.get());
										isActive = false;
								}
								case RESET -> {
										counter.set(0);
										System.out.printf("The counter has been reset! Value is: %d\n", counter.get());
								}
						}
				}
				scanner.close();
		}
}
