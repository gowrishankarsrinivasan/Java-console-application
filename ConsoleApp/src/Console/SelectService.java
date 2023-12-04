package Console;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class SelectService extends JdbcConfiguration{
   
    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Welcome to the Salon and spa Booking System!");
            
            System.out.print("Enter your UserName: ");

            
            
            String userName = sc.nextLine();
            System.out.println("Hi "+userName+"Welcome to our Salon and spa Booking System!!!");

            // Additional requirements
            viewAvailableServices(connection);
            System.out.print("\nEnter the serviceId to select the service: ");
            int selectedServiceId = sc.nextInt();

            // Check if the selected service exists
            if (checkServiceExists(connection, selectedServiceId)) {
                System.out.println("Service selected successfully!");

                // Select a consultant
                int selectedConsultantId = selectConsultant(connection);

                System.out.print("Enter the Appointment Date (YYYY-MM-DD): ");
                String appointmentDate = sc.next();
                System.out.print("Enter the Appointment Time (HH:mm): ");
                String appointmentTime = sc.next();

                // Additional requirements
                double totalPrice = bookAppointment(connection, userName, selectedServiceId, selectedConsultantId, appointmentDate, appointmentTime);
                viewBookedAppointments(connection, userName);
                // viewConsultantList(connection);
                displayBill(selectedServiceId, appointmentDate, appointmentTime, totalPrice);
                giveFeedback(connection, userName);

                // Generate and display the bill
            } else {
                System.out.println("\nService not found for the given Service ID.");
            }
        } catch (SQLException e) {
            System.err.println("Error: Unable to connect to the database");
            e.printStackTrace();
        }
    }

    // Method to view available services
    private static void viewAvailableServices(Connection connection) {
        String selectQuery = "SELECT * FROM services";
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(selectQuery)) {
            System.out.println("\nAvailable Services");
            while (rs.next()) {
                int serviceId = rs.getInt("serviceId");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");

                System.out.println(serviceId + ". " + name + " - " + description + " - $" + price);
            }
        } catch (SQLException e) {
            System.err.println("Error: Unable to execute the query");
            e.printStackTrace();
        }
    }

    // Method to check if the selected service exists
    private static boolean checkServiceExists(Connection connection, int serviceId) throws SQLException {
        String selectServiceQuery = "SELECT * FROM services WHERE serviceId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectServiceQuery)) {
            preparedStatement.setInt(1, serviceId);
            ResultSet selectedResult = preparedStatement.executeQuery();
            return selectedResult.next();
        } catch (SQLException e) {
            System.err.println("Error: Unable to execute the query");
            e.printStackTrace();
            return false;
        }
    }

    // Method to select a consultant
    private static int selectConsultant(Connection connection) throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.println("\nSelect a Consultant:");
        viewConsultantList(connection);

        System.out.print("Enter the Consultant ID: ");
        int selectedConsultantId = sc.nextInt();
        if (checkConsultantExists(connection, selectedConsultantId)) {
            return selectedConsultantId;
        } else {
            System.out.println("Consultant not found for the given ID. Please try again.");
            return selectConsultant(connection); // Recursively call this method to try again
        }
    }

    // Method to check if the selected consultant exists
    private static boolean checkConsultantExists(Connection connection, int consultantId) throws SQLException {
        String selectConsultantQuery = "SELECT * FROM consultants WHERE consultantId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectConsultantQuery)) {
            preparedStatement.setInt(1, consultantId);
            ResultSet selectedResult = preparedStatement.executeQuery();
            return selectedResult.next();
        } catch (SQLException e) {
            System.err.println("Error: Unable to execute the query");
            e.printStackTrace();
            return false;
        }
    }

    // Method to book an appointment and return the total price
    private static double bookAppointment(Connection connection, String userName, int serviceId,
            int consultantId, String appointmentDate, String appointmentTime) {
        double totalPrice = 0.0;
        // Retrieve the price of the selected service
        double servicePrice = getServicePrice(connection, serviceId);
        if (servicePrice > 0) {
            String insertAppointmentQuery = "INSERT INTO appointments (UserName, ServiceId, ConsultantId, AppointmentDate, AppointmentTime) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertAppointmentQuery)) {
                preparedStatement.setString(1, userName);
                preparedStatement.setInt(2, serviceId);
                preparedStatement.setInt(3, consultantId);
                preparedStatement.setString(4, appointmentDate);
                preparedStatement.setString(5, appointmentTime);
                preparedStatement.executeUpdate();
                System.out.println("\nAppointment booked successfully!");
                totalPrice = servicePrice; // Set the total price to the service price
            } catch (SQLException e) {
                System.err.println("Error: Unable to book the appointment");
                e.printStackTrace();
            }
        } else {
            System.out.println("\nService price not found. Appointment booking failed.");
        }
        return totalPrice;
    }

    // Method to view booked appointments for a user
    private static void viewBookedAppointments(Connection connection, String userName) {
        String selectAppointmentsQuery = "SELECT * FROM appointments WHERE UserName = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectAppointmentsQuery)) {
            preparedStatement.setString(1, userName);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("\nBooked Appointments for " + userName);
            while (resultSet.next()) {
                int serviceId = resultSet.getInt("ServiceId");
                int consultantId = resultSet.getInt("ConsultantId");
                String appointmentDate = resultSet.getString("AppointmentDate");
                String appointmentTime = resultSet.getString("AppointmentTime");
                System.out.println(
                        "Service ID: " + serviceId + ", Consultant ID: " + consultantId + ", Date: " + appointmentDate + ", Time: " + appointmentTime);
            }
        } catch (SQLException e) {
            System.err.println("Error: Unable to retrieve booked appointments");
            e.printStackTrace();
        }
    }

    // Method to view a list of consultants
    private static void viewConsultantList(Connection connection) {
        String selectConsultantsQuery = "SELECT * FROM consultants";
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(selectConsultantsQuery)) {
            System.out.println("\nList of Consultants");
            while (resultSet.next()) {
                int consultantId = resultSet.getInt("consultantId");
                String name = resultSet.getString("name");
                String specialization = resultSet.getString("specialization");
                System.out.println(consultantId + ". " + name + " - " + specialization);
            }
        } catch (SQLException e) {
            System.err.println("Error: Unable to retrieve consultant list");
            e.printStackTrace();
        }
    }

    // Method for users to give feedback
    private static void giveFeedback(Connection connection, String userName) {
        Scanner sc = new Scanner(System.in);
        System.out.print("\nDo you want to give feedback? (yes/no): ");
        String feedbackOption = sc.nextLine().trim().toLowerCase();
        if (feedbackOption.equals("yes")) {
            System.out.print("Enter your feedback: ");
            String feedbackMessage = sc.nextLine();
            String insertFeedbackQuery = "INSERT INTO feedback (UserName, FeedbackMessage) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertFeedbackQuery)) {
                preparedStatement.setString(1, userName);
                preparedStatement.setString(2, feedbackMessage);
                preparedStatement.executeUpdate();
                System.out.println("Thank you for your feedback!");
            } catch (SQLException e) {
                System.err.println("Error: Unable to submit feedback");
                e.printStackTrace();
            }
        }
    }

    // Method to get the price of a service
    private static double getServicePrice(Connection connection, int serviceId) {
        String selectServicePriceQuery = "SELECT price FROM services WHERE serviceId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectServicePriceQuery)) {
            preparedStatement.setInt(1, serviceId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("price");
            } else {
                return 0.0;
            }
        } catch (SQLException e) {
            System.err.println("Error: Unable to retrieve service price");
            e.printStackTrace();
            return 0.0;
        }
    }

    // Method to display the bill
    private static void displayBill(int serviceId, String appointmentDate, String appointmentTime, double totalPrice) {
        System.out.println("\nBill Details");
        System.out.println("Service ID: " + serviceId);
        System.out.println("Appointment Date: " + appointmentDate);
        System.out.println("Appointment Time: " + appointmentTime);
        System.out.println("Total Price: $" + totalPrice);
    }
}
