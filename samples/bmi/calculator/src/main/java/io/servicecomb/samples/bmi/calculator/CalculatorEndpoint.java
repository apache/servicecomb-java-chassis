package io.servicecomb.samples.bmi.calculator;

/**
 * {@link CalculatorEndpoint} provides the common interface for different endpoint implementations.
 * It needs to be declared as public.
 */
public interface CalculatorEndpoint {
  /**
   * Calculate the BMI(Body Mass Index).
   */
  double calculate(double height, double weight);
}
