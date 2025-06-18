package me.shhalex.server;

import me.shhalex.server.ServerOptimizer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ErrorInfo {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void logError(Exception e, Class<?> callingClass) {
        ServerOptimizer pluginInstance = ServerOptimizer.getInstance();

        if (pluginInstance == null) {
            System.err.println("Error: ServerOptimizer not initialized. Could not log error to file.");
            e.printStackTrace();
            return;
        }

        File logsFolder = new File(pluginInstance.getDataFolder(), "logs");
        if (!logsFolder.exists()) logsFolder.mkdirs();

        File errorFile = new File(logsFolder, "error_log.txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(errorFile, true))) {
            writer.println("--- Error in module: " + callingClass.getSimpleName() + " ---");
            writer.println("Date and Time: " + DATE_FORMAT.format(new Date()));
            writer.println("Message: " + e.getMessage());
            writer.println("Exception Type: " + e.getClass().getName());

            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace.length > 0) {
                for (StackTraceElement element : stackTrace) {
                    if (element.getClassName().startsWith("me.shhalex.server.")) {
                        writer.println("Error Location: " + element.getClassName() + " (Line: " + element.getLineNumber() + ")");
                        break;
                    }
                }
            } else {
                writer.println("Error Location: Not determined (empty stacktrace)");
            }

            writer.println("Full Stacktrace:");
            e.printStackTrace(writer);
            writer.println("-------------------------------------\n");
        } catch (IOException ioException) {
            pluginInstance.getLogger().severe("Could not write error to log file: " + ioException.getMessage());
            ioException.printStackTrace();
        }
    }
}