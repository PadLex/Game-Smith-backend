package approaches.symbolic.api;

import GUI.RecommenderStarter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Wrapper used by the VSCode extension to interact with the recommender system.
 */
public class DesktopAppEndpoint {

//    @Override
    String respond() {
        ProcessBuilder processBuilder = new ProcessBuilder();

        System.out.println(System.getProperty("java.class.path"));
        System.out.println(System.getProperty("user.dir"));

        // Construct the classpath
        String initialClassPath = "/Ludii Recommender/Generation/bin";
        String targetClassPath = "/Ludii Recommender/PlayerDesktop/bin";
        // Add more paths separated by ':' (on Linux/macOS) or ';' (on Windows) if there are more dependencies.
        String classPath = initialClassPath + ":" + targetClassPath;

        // Set the command to start app.StartDesktopApp with the constructed classpath
        processBuilder.command("java", "-cp", classPath, "app.StartDesktopApp");

        // Change the working directory
        processBuilder.directory(Paths.get("./PlayerDesktop").toFile());

        try {
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public static void main(String[] args) {
//        new DesktopAppEndpoint().start();
        new DesktopAppEndpoint().respond();
    }
}
