import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileDuplicateRemover {
    public static void main(String[] args) {
        // Check if directory path is provided as an argument
        if (args.length == 0) {
            System.out.println("Please specify a directory path.");
            return;
        }

        // Set the directory path from the argument
        String directoryPath = args[0];
        File directory = new File(directoryPath);

        // Verify if the provided path is a directory
        if (!directory.isDirectory()) {
            System.out.println("The specified path is not a directory.");
            return;
        }

        // Maps to store files by their hash and format
        Map<String, Map<String, File>> hashMap = new HashMap<>();
        List<String> deletedFiles = new ArrayList<>();

        // Process each file in the directory
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                try {
                    String fileHash = hashFile(file);
                    String fileExtension = getFileExtension(file);

                    // Check if the hash already exists for the same file format
                    hashMap.putIfAbsent(fileExtension, new HashMap<>());
                    Map<String, File> formatMap = hashMap.get(fileExtension);

                    if (formatMap.containsKey(fileHash)) {
                        File originalFile = formatMap.get(fileHash);

                        // Check if the current file is a duplicate based on name and timestamp
                        if ((file.getName().contains("copy") || file.lastModified() > originalFile.lastModified())) {
                            // Delete the duplicate file
                            deletedFiles.add(file.getName());
                            Files.delete(file.toPath());
                        } else {
                            // Replace the older file if current file is likely the original
                            deletedFiles.add(originalFile.getName());
                            Files.delete(originalFile.toPath());
                            formatMap.put(fileHash, file); // Store the current file as the original
                        }
                    } else {
                        // Store file as original in map if no duplicate found
                        formatMap.put(fileHash, file);
                    }
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.out.println("Error processing file: " + file.getName());
                    e.printStackTrace();
                }
            }
        }

        // Print deleted files
        if (!deletedFiles.isEmpty()) {
            System.out.println("Deleted Files:");
            for (String deletedFile : deletedFiles) {
                System.out.println(deletedFile);
            }
        } else {
            System.out.println("No duplicate files found.");
        }
    }

    // Generates the MD5 hash of a file
    private static String hashFile(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md5Digest = MessageDigest.getInstance("MD5");
        try (var inputStream = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[65536];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md5Digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = md5Digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Helper method to get the file extension
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf(".");
        return (lastDotIndex == -1) ? "" : name.substring(lastDotIndex + 1);
    }
}
/*
 TO RUN THE PROGRAM
 1. Navigate to the project directory:
    cd "D:\Personal use\SEM III\OOP\namma proj\Duplicate files remover"

 2. Compile the program:
    javac FileDuplicateRemover.java

 3. Run the program with the Dataset path:
    java FileDuplicateRemover "D:\Personal use\SEM III\OOP\namma proj\Duplicate files remover\Dataset"
 */
