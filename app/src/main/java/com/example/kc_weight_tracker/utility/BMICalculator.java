package com.example.kc_weight_tracker.utility;

/**
 * BMICalculator is a class that calculates the BMI of a user.
 * It is used to calculate the BMI of a user based on their weight and height.
 */
public class BMICalculator {

    /**
     * Calculates the BMI using weight in pounds and height in inches
     * 
     * @param weightLb     Weight in pounds
     * @param heightInches Height in inches
     * @return BMI value
     */
    public static double calculateBMI(double weightLb, double heightInches) {
        if (heightInches <= 0)
            return 0;

        // Converts pounds to kg and inches to meters
        // Formula: BMI = weight (kg) / height^2 (m^2)
        // reference:
        // https://www.cdc.gov/growth-chart-training/hcp/using-bmi/calculating-bmi.html
        double weightKg = weightLb * 0.453592;
        double heightMeters = heightInches * 0.0254;

        return weightKg / (heightMeters * heightMeters);
    }

    /**
     * Get BMI category based on BMI value
     * 
     * @param bmi BMI value
     * @return BMI category string
     */
    public static String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi < 25) {
            return "Normal Weight";
        } else if (bmi < 30) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }

    /**
     * Get validation message for target weight
     * 
     * @param currentWeight Current weight in pounds
     * @param targetWeight  Target weight in pounds
     * @param heightInches  Height in inches
     * @return Validation message or null if valid
     */
    public static String getValidationMessage(double currentWeight, double targetWeight, double heightInches) {
        double targetBMI = calculateBMI(targetWeight, heightInches);
        double weightChange = Math.abs(targetWeight - currentWeight);

        if (weightChange > 50) {
            return "Weight change should be 50 lbs or less for safety";
        }

        if (targetBMI > 35.0) {
            return "Target weight results in a very high BMI. Consider consulting a healthcare provider";
        }

        if (targetBMI < 16.0) {
            return "Target weight is dangerously low. Please consult a healthcare provider";
        }

        return null; // Valid
    }
}
