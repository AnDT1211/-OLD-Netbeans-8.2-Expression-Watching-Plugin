package service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import model.UTModel;


public class FileService {
    public static boolean writeObjectToFile(File file, UTModel model) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outputStream.writeObject(model);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public static UTModel readObjectFromFile(File file) {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
            UTModel model = (UTModel) inputStream.readObject();
            return model;
        } catch (Exception e) {
            return null;
        }
    }
}
