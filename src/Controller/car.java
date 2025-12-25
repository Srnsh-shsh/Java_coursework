package Controller;

import Model.gettersetter;  // IMPORT THE MODEL
import javax.swing.table.DefaultTableModel;
import java.io.*;
import javax.swing.JOptionPane;

public class car {
    private static final String FILE_NAME = "cars.txt";
    
    // ========== FILE HANDLING METHODS ==========
    
    // Load all cars from file
    public static void loadAllCars(DefaultTableModel model) {
        model.setRowCount(0); // Clear table
        
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return; // File doesn't exist yet
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int sno = 1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] data = line.split(",");
                    if (data.length == 4) {
                        // Create Model object
                        gettersetter carModel = new gettersetter(
                            sno++, 
                            data[0], // name
                            data[1], // brand
                            data[2], // plateNo
                            data[3]  // carType
                        );
                        // Add to table
                        model.addRow(carModel.toTableRow());
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading cars: " + e.getMessage());
        }
    }
    
    // Add a new car
    public static void addNewCar(String name, String brand, String plateNo, String carType, DefaultTableModel model) {
        // Validate
        if (name.isEmpty() || brand.isEmpty() || plateNo.isEmpty() || carType.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields!");
            return;
        }
        
        // Check if plate exists
        if (isPlateExists(plateNo, model)) {
            JOptionPane.showMessageDialog(null, "Plate number already exists!");
            return;
        }
        
        // Calculate SNo
        int nextSNo = model.getRowCount() + 1;
        
        // Create Model object
        gettersetter newCar = new gettersetter(nextSNo, name, brand, plateNo, carType);
        
        // Add to table
        model.addRow(newCar.toTableRow());
        
        // Save to file
        saveCarToFile(newCar);
        
        JOptionPane.showMessageDialog(null, "Car added successfully!");
    }
    
    // Save single car to file (append)
    private static void saveCarToFile(gettersetter carModel) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writer.println(carModel.toCSV());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving car: " + e.getMessage());
        }
    }
    
    // Save all cars to file (overwrite)
    public static void saveAllCars(DefaultTableModel model) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < model.getRowCount(); i++) {
                // Create Model object from table data
                gettersetter car = new gettersetter(
                    Integer.parseInt(model.getValueAt(i, 0).toString()), // sno
                    model.getValueAt(i, 1).toString(), // name
                    model.getValueAt(i, 2).toString(), // brand
                    model.getValueAt(i, 3).toString(), // plateNo
                    model.getValueAt(i, 4).toString()  // carType
                );
                writer.println(car.toCSV());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving all cars: " + e.getMessage());
        }
    }
    
    // Delete a car
    public static void deleteCar(int rowIndex, DefaultTableModel model) {
        if (rowIndex >= 0 && rowIndex < model.getRowCount()) {
            model.removeRow(rowIndex);
            fixSerialNumbers(model);
            saveAllCars(model);
            JOptionPane.showMessageDialog(null, "Car deleted successfully!");
        } else {
            JOptionPane.showMessageDialog(null, "Please select a car to delete!");
        }
    }
    
    // Update a car
    public static void updateCar(int rowIndex, String name, String brand, String plateNo, String carType, DefaultTableModel model) {
        if (rowIndex >= 0 && rowIndex < model.getRowCount()) {
            // Update table
            model.setValueAt(name, rowIndex, 1);
            model.setValueAt(brand, rowIndex, 2);
            model.setValueAt(plateNo, rowIndex, 3);
            model.setValueAt(carType, rowIndex, 4);
            
            // Save to file
            saveAllCars(model);
            JOptionPane.showMessageDialog(null, "Car updated successfully!");
        } else {
            JOptionPane.showMessageDialog(null, "Please select a car to update!");
        }
    }
    
    // Check if plate exists
    public static boolean isPlateExists(String plateNo, DefaultTableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 3).toString().equalsIgnoreCase(plateNo)) {
                return true;
            }
        }
        return false;
    }
    
    // Fix serial numbers
    public static void fixSerialNumbers(DefaultTableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0);
        }
    }
    
    // Get car object from table row
    public static gettersetter getCarFromRow(int rowIndex, DefaultTableModel model) {
        if (rowIndex >= 0 && rowIndex < model.getRowCount()) {
            return new gettersetter(
                Integer.parseInt(model.getValueAt(rowIndex, 0).toString()),
                model.getValueAt(rowIndex, 1).toString(),
                model.getValueAt(rowIndex, 2).toString(),
                model.getValueAt(rowIndex, 3).toString(),
                model.getValueAt(rowIndex, 4).toString()
            );
        }
        return null;
    }
}