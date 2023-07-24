package approaches.symbolic.api;

import java.util.Scanner;
import GUI.RecommenderStarter;

/**
 * Wrapper used by the VSCode extension to interact with the recommender system.
 */
public class RecommenderEndpoint {
    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Ready");

        while (sc.hasNextLine()) {
            sc.nextLine();
            new RecommenderStarter();
        }
        sc.close();
    }
}
