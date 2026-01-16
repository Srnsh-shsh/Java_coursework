package Controller;

import Model.gettersetter;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Queue;

public class car {
    private String name;
    private String brand;
    private String ModelNo;
    private String carType;
    
    private static final String FILE_NAME = "cars.txt";
    
    private static final ArrayList<gettersetter> carList = new ArrayList<>();

// LinkedList → recent add/delete tracking
private static final LinkedList<gettersetter> recentCars = new LinkedList<>();

// Stack → undo delete (LIFO)
private static final Stack<gettersetter> deletedCars = new Stack<>();

// Queue → customer view loading (FIFO)
private static final Queue<gettersetter> customerQueue = new LinkedList<>();

public static void addNewCar(gettersetter car) {
    carList.add(car);              // ArrayList
    recentCars.addFirst(car);      // LinkedList
    refreshAllTables();
}
public static void updateCar(int index, gettersetter updatedCar) {
    if (index >= 0 && index < carList.size()) {
        carList.set(index, updatedCar);
        refreshAllTables();
    }
}

public static void deleteCar(int index) {
    if (index >= 0 && index < carList.size()) {
        gettersetter removed = carList.remove(index);

        deletedCars.push(removed);     // Stack
        recentCars.addFirst(removed);  // LinkedList

        refreshAllTables();
    }
}

public static void undoDelete() {
    if (!deletedCars.isEmpty()) {
        gettersetter restored = deletedCars.pop();
        carList.add(restored);
        refreshAllTables();
    }
}
public static ArrayList<Object[]> getAllCarsAsList() {
    ArrayList<Object[]> list = new ArrayList<>();
    
    try {
        // Assuming you are reading from a file, logic is similar to your load method
        java.io.File file = new java.io.File("car_data.txt"); // Use your actual file path
        if (!file.exists()) return list;

        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.split(","); // Or your specific delimiter
            // storage: {Name, Brand, Model, Type}
            list.add(new Object[]{ data[0], data[1], data[2], data[3] });
        }
        br.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
    return list;
}
public static void loadCarsFromFileIntoList() {
    carList.clear(); // avoid duplicates
    File file = new File(FILE_NAME);
    if (!file.exists()) return;

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line;
        int sno = 1;
        while ((line = reader.readLine()) != null) {
            String[] data = line.split(",");
            if (data.length == 4) {
                gettersetter c = new gettersetter(sno++, data[0], data[1], data[2], data[3]);
                carList.add(c);
            }
        }
    } catch (IOException e) {
        System.err.println("Error reading file: " + e.getMessage());
    }
}

public static void loadCustomerTable(DefaultTableModel model) {
    model.setRowCount(0);

    customerQueue.clear();
    customerQueue.addAll(carList); // Queue FIFO

    while (!customerQueue.isEmpty()) {
        model.addRow(customerQueue.poll().toTableRow());
    }
}
public static void loadAdminTable(DefaultTableModel model) {
    model.setRowCount(0);
    for (gettersetter car : carList) {
        model.addRow(car.toTableRow());
    }
}

private static void refreshAllTables() {
    for (DefaultTableModel model : adminTables) {
        loadAdminTable(model);
    }
    for (DefaultTableModel model : customerTables) {
        loadCustomerTable(model);
    }
}



    
    // Lists to track open tables for synchronization
    private static List<DefaultTableModel> customerTables = new ArrayList<>();
    private static List<DefaultTableModel> adminTables = new ArrayList<>();
    
    // ========== REGISTRATION METHODS ==========
    public static void registerCustomerTable(DefaultTableModel model) {
        if (!customerTables.contains(model)) customerTables.add(model);
    }
    
    public static void registerAdminTable(DefaultTableModel model) {
        if (!adminTables.contains(model)) adminTables.add(model);
    }
    
    public static void unregisterCustomerTable(DefaultTableModel model) {
        customerTables.remove(model);
    }
    
    public static void unregisterAdminTable(DefaultTableModel model) {
        adminTables.remove(model);
    }
    
    // ========== SYNCHRONIZATION ==========
    public static void syncAllTables() {
        for (DefaultTableModel adminModel : adminTables) {
            loadAllCars(adminModel);
        }
        for (DefaultTableModel customerModel : customerTables) {
            loadAllCarsForCustomer(customerModel);
        }
    }

    // ========== VALIDATION METHODS ==========
    
    /**
     * Checks if Model Number already exists in the table
     * @param ModelNo The model number to check
     * @param model The table model
     * @param excludeRow Row index to exclude (for update operations)
     * @return true if duplicate exists, false otherwise
     */
    public static boolean isModelNoExists(String ModelNo, DefaultTableModel model, int excludeRow) {
        String searchModelNo = ModelNo.trim().toLowerCase();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (i == excludeRow) continue; // Skip the row being updated
            
            String existingModelNo = model.getValueAt(i, 3).toString().trim().toLowerCase();
            if (existingModelNo.equals(searchModelNo)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isModelNoExistsInFile(String ModelNo, int excludeSNo) {
        String searchModelNo = ModelNo.trim().toLowerCase();
        File file = new File(FILE_NAME);
        
        if (!file.exists()) return false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int currentSNo = 1;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    // Skip the car we're updating (if excludeSNo > 0)
                    if (excludeSNo > 0 && currentSNo == excludeSNo) {
                        currentSNo++;
                        continue;
                    }
                    
                    String existingModelNo = data[2].trim().toLowerCase(); // ModelNo is 3rd field
                    if (existingModelNo.equals(searchModelNo)) {
                        return true;
                    }
                    currentSNo++;
                }
            }
        } catch (IOException e) {
            System.err.println("File read error: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Gets car details for error message when duplicate is found IN FILE
     */
    public static String getDuplicateCarDetailsFromFile(String ModelNo) {
        String searchModelNo = ModelNo.trim().toLowerCase();
        File file = new File(FILE_NAME);
        
        if (!file.exists()) return "Unknown Car";
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    String existingModelNo = data[2].trim().toLowerCase();
                    if (existingModelNo.equals(searchModelNo)) {
                        String carName = data[0];
                        String carBrand = data[1];
                        String carType = data[3];
                        return carBrand + " " + carName + " (" + carType + ")";
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("File read error: " + e.getMessage());
        }
        return "Unknown Car";
    }

    // ========== CRUD OPERATIONS ==========

public static void addNewCar(String name, String brand, String ModelNo, String carType, DefaultTableModel model) {
    // 1. Empty Field Validation
    if (name.trim().isEmpty() || brand.trim().isEmpty() || ModelNo.trim().isEmpty() || carType.trim().isEmpty()) {
        JOptionPane.showMessageDialog(null, "Please fill all fields!", "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // 2. Unique Model Number Validation - CHECK FILE, NOT TABLE
    if (isModelNoExistsInFile(ModelNo, -1)) {
        String duplicateDetails = getDuplicateCarDetailsFromFile(ModelNo);
        JOptionPane.showMessageDialog(null, 
            "Model number already exists!\n\n" +
            "Existing Car: " + duplicateDetails + "\n" +
            "Please use a different model number.",
            "Duplicate Model Number",
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    int nextSNo = getNextSerialNumber();
    // Create CSV string directly instead of calling toCSV()
    String csvLine = name.trim() + "," + brand.trim() + "," + ModelNo.trim() + "," + carType.trim();
    
    try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
        writer.println(csvLine);
        syncAllTables();
        JOptionPane.showMessageDialog(null, "Car added!");
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Error saving to file: " + e.getMessage());
    }
}

    public static void updateCar(int rowIndex, String name, String brand, String ModelNo, String carType, DefaultTableModel model) {
        if (rowIndex < 0) {
            JOptionPane.showMessageDialog(null, "Please select a car to update!");
            return;
        }

        // 1. Empty Field Validation
        if (name.trim().isEmpty() || brand.trim().isEmpty() || ModelNo.trim().isEmpty() || carType.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get the serial number of the car being updated
        int carSNo = rowIndex + 1; // Since serial numbers are sequential
        
        // 2. Check if the modified ModelNo conflicts with ANOTHER car in FILE
        if (isModelNoExistsInFile(ModelNo, carSNo)) {
            String duplicateDetails = getDuplicateCarDetailsFromFile(ModelNo);
            JOptionPane.showMessageDialog(null, 
                "Update failed: Model Number already exists!\n\n" +
                "Existing Car: " + duplicateDetails + "\n" +
                "Please use a different model number.",
                "Duplicate Model Number",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        model.setValueAt(name.trim(), rowIndex, 1);
        model.setValueAt(brand.trim(), rowIndex, 2);
        model.setValueAt(ModelNo.trim(), rowIndex, 3);
        model.setValueAt(carType.trim(), rowIndex, 4);
        
        saveAllCars(model);
        syncAllTables();
        JOptionPane.showMessageDialog(null, "Car updated successfully!");
    }

    public static void deleteCar(int rowIndex, DefaultTableModel model) {
        if (rowIndex >= 0) {
            // Get car details for confirmation
            String carName = model.getValueAt(rowIndex, 1).toString();
            String carBrand = model.getValueAt(rowIndex, 2).toString();
            String carModelNo = model.getValueAt(rowIndex, 3).toString();
            
            int confirm = javax.swing.JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to delete this car?\n" +
                carBrand + " " + carName + "\n" +
                "Model: " + carModelNo,
                "Confirm Delete", 
                javax.swing.JOptionPane.YES_NO_OPTION);
                
            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                model.removeRow(rowIndex);
                saveAllCars(model);
                syncAllTables();
                javax.swing.JOptionPane.showMessageDialog(null, "deleted from Queue!");
                
            }
        } else {
            
            javax.swing.JOptionPane.showMessageDialog(null, "Please select a car to delete!");
        }
    }

    // ========== FILE I/O ==========

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
            System.err.println("Save error: " + e.getMessage());
        }
    }

    public static void loadAllCars(DefaultTableModel model) {
        model.setRowCount(0);
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int sno = 1;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    model.addRow(new Object[]{sno++, data[0], data[1], data[2], data[3]});
                }
            }
        } catch (IOException e) {
            System.err.println("Load error: " + e.getMessage());
        }
    }

    public static void loadAllCarsForCustomer(DefaultTableModel model) {
        model.setRowCount(0);
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int sno = 1;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    // Customer view: Displays Name, Brand, and Type (often hides ID/ModelNo)
                    model.addRow(new Object[]{sno++, data[0], data[1],data[2], data[3]});
                }
            }
        } catch (IOException e) {
            System.err.println("Customer Load error: " + e.getMessage());
        }
    }

    private static int getNextSerialNumber() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return 1;
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) count++;
        } catch (IOException e) {}
        return count + 1;
    }

    // Constructor and Getters/Setters
    public car(String name, String brand, String ModelNo, String carType) {
        this.name = name;
        this.brand = brand;
        this.ModelNo = ModelNo;
        this.carType = carType;
    }
}