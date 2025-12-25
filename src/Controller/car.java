package Controller;

import Model.gettersetter;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;

public class car {
    private String name;
    private String brand;
    private String ModelNo;
    private String carType;
    
    private static final String FILE_NAME = "cars.txt";
    
    // ========== ADD THIS: List to track all open tables ==========
    private static List<DefaultTableModel> customerTables = new ArrayList<>();
    private static List<DefaultTableModel> adminTables = new ArrayList<>();
    
    // ========== REGISTRATION METHODS ==========
    public static void registerCustomerTable(DefaultTableModel model) {
        if (!customerTables.contains(model)) {
            customerTables.add(model);
        }
    }
    
    public static void registerAdminTable(DefaultTableModel model) {
        if (!adminTables.contains(model)) {
            adminTables.add(model);
        }
    }
    
    public static void unregisterCustomerTable(DefaultTableModel model) {
        customerTables.remove(model);
    }
    
    public static void unregisterAdminTable(DefaultTableModel model) {
        adminTables.remove(model);
    }
    
    // ========== SYNCHRONIZATION METHOD ==========
    public static void syncAllTables() {
        // Refresh all admin tables registered in the List
        for (DefaultTableModel adminModel : adminTables) {
            loadAllCars(adminModel); // Refreshes Admin view with all columns
        }
        
        // Refresh all customer tables registered in the List
        for (DefaultTableModel customerModel : customerTables) {
            loadAllCarsForCustomer(customerModel); // Refreshes Customer view (hides PlateNo)
        }
    }

    // ========== ADD THIS: Data Structure Helper Method ==========
    // This method reads the file and returns a List of Model objects
    public static List<gettersetter> getAllCars() {
        List<gettersetter> cars = new ArrayList<>();
        File file = new File(FILE_NAME);
        
        if (!file.exists()) return cars;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int sno = 1;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    // Create Model objects from file data
                    cars.add(new gettersetter(sno++, data[0], data[1], data[2], data[3]));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading cars for list: " + e.getMessage());
        }
        return cars;
    }
    
    // ========== MODIFIED ADD CAR METHOD ==========
    public static void addNewCar(String name, String brand, String ModelNo, String carType, DefaultTableModel model) {
        // Validate
        if (name.isEmpty() || brand.isEmpty() || ModelNo.isEmpty() || carType.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields!");
            return;
        }
        
        // Check if plate exists
        if (isPlateExists(ModelNo, model)) {
            JOptionPane.showMessageDialog(null, "Plate number already exists!");
            return;
        }
        
        // Calculate SNo
        int nextSNo = getNextSerialNumber();
        
        // Create Model object
        gettersetter newCar = new gettersetter(nextSNo, name, brand, ModelNo, carType);
        
        // Save to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writer.println(newCar.toCSV());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving to file: " + e.getMessage());
            return;
        }
        
        // ========== SYNC ALL TABLES ==========
        syncAllTables();
        
        JOptionPane.showMessageDialog(null, "Car added and synchronized across all panels!");
    }
    
    // Helper method to get next serial number
    private static int getNextSerialNumber() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return 1;
        
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) count++;
        } catch (IOException e) {
            // Silent fail
        }
        return count + 1;
    }
    
    // ========== MODIFIED DELETE METHOD ==========
    public static void deleteCar(int rowIndex, DefaultTableModel model) {
        if (rowIndex >= 0 && rowIndex < model.getRowCount()) {
            int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete this car?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Remove from table
                model.removeRow(rowIndex);
                
                // Save current table to file
                saveAllCars(model);
                
                // ========== SYNC ALL TABLES ==========
                syncAllTables();
                
                JOptionPane.showMessageDialog(null, "Car deleted and synchronized!");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select a car to delete!");
        }
    }
    
    // ========== MODIFIED UPDATE METHOD ==========
    public static void updateCar(int rowIndex, String name, String brand, String ModelNo, String carType, DefaultTableModel model) {
        if (rowIndex >= 0 && rowIndex < model.getRowCount()) {
            model.setValueAt(name, rowIndex, 1);
            model.setValueAt(brand, rowIndex, 2);
            model.setValueAt(ModelNo, rowIndex, 3);
            model.setValueAt(carType, rowIndex, 4);
            
            // Save to file
            saveAllCars(model);
            
            // ========== SYNC ALL TABLES ==========
            syncAllTables();
            
            JOptionPane.showMessageDialog(null, "Car updated and synchronized!");
        } else {
            JOptionPane.showMessageDialog(null, "Please select a car to update!");
        }
    }
    
    // ========== ORIGINAL METHODS (KEEP THESE) ==========
    // ... Your existing constructor, getters, setters, etc ...
    
    public car(String name, String brand, String ModelNo, String carType) {
        this.name = name;
        this.brand = brand;
        this.ModelNo = ModelNo;
        this.carType = carType;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getPlateNo() { return ModelNo; }
    public void setPlateNo(String ModelNo) { this.ModelNo = ModelNo; }
    
    public String getCarType() { return carType; }
    public void setCarType(String carType) { this.carType = carType; }
    
    // Convert to CSV
    public String toCSV() {
        return name + "," + brand + "," + ModelNo + "," + carType;
    }
    
    // ========== EXISTING FILE METHODS (KEEP THESE) ==========
    public static void saveAllCars(DefaultTableModel model) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < model.getRowCount(); i++) {
                String line = String.join(",",
                    model.getValueAt(i, 1).toString(),
                    model.getValueAt(i, 2).toString(),
                    model.getValueAt(i, 3).toString(),
                    model.getValueAt(i, 4).toString()
                );
                writer.println(line);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving all cars: " + e.getMessage());
        }
    }
    
    public static void loadAllCars(DefaultTableModel model) {
        model.setRowCount(0); // Clear table
        
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int sno = 1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] data = line.split(",");
                    if (data.length == 4) {
                        model.addRow(new Object[]{
                            sno++, data[0], data[1], data[2], data[3]
                        });
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading cars: " + e.getMessage());
        }
    }
    
    public static void loadAllCarsForCustomer(DefaultTableModel model) {
        model.setRowCount(0); // Clear table
        
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int sno = 1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] data = line.split(",");
                    if (data.length == 4) {
                        model.addRow(new Object[]{
                            sno++, data[0], data[1],data[2],data[3] // No ModelNo for customer
                        });
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading cars for customer: " + e.getMessage());
        }
    }
    
    public static boolean isPlateExists(String ModelNo, DefaultTableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 3).toString().equalsIgnoreCase(ModelNo)) {
                return true;
            }
        }
        return false;
    }
    
    public static void fixSerialNumbers(DefaultTableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0);
        }
    }
}