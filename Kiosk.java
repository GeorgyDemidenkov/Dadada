package lorek;

public class Kiosk {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        if (dbManager.getConnection() != null) {
            MainApp mainApp = new MainApp(dbManager.getConnection());
            mainApp.start();
            dbManager.closeConnection();
        } else {
            System.out.println("Не удалось подключиться к базе данных. Программа завершена.");
        }
    }
}
