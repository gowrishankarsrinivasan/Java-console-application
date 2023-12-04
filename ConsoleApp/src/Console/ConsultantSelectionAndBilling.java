package Console;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ConsultantSelectionAndBilling {

    public String selectConsultant(Connection connection) {
        Scanner sc = new Scanner(System.in);
        String consultantName = null;

        System.out.println("\nSelect a Consultant:");
        try {
            String selectConsultantQuery = "SELECT Name FROM consultants";
            PreparedStatement preparedStatement = connection.prepareStatement(selectConsultantQuery);
            ResultSet resultSet = preparedStatement.executeQuery();

            int consultantCount = 0;
            while (resultSet.next()) {
                consultantCount++;
                System.out.println(consultantCount + ". " + resultSet.getString("Name"));
            }

            if (consultantCount == 0) {
                System.out.println("No consultants available.");
                return null; // No consultants available
            }

            System.out.print("Enter the number corresponding to your chosen consultant: ");
            int choice = sc.nextInt();

            // Check if the choice is valid
            if (choice < 1 || choice > consultantCount) {
                System.out.println("Invalid choice. Please select a valid consultant.");
                return null; // Invalid choice
            }

            // Get the selected consultant's name
            resultSet.absolute(choice);
            consultantName = resultSet.getString("Name");
        } catch (SQLException e) {
            System.err.println("Error: Unable to retrieve consultants");
            e.printStackTrace();
        } finally {
            sc.close(); // Close the Scanner object
        }

        return consultantName;
    }
}
