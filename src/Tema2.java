import java.io.*;
import java.util.concurrent.Semaphore;

public class Tema2 {
    public static void main(String[] args) {
        if (args.length != 2)
            System.out.println("Error running the program. " +
                    "You must provide the name of the folder" +
                    " and the number of threads!");
        String folderName = args[0];
        int numberOfThreads = Integer.parseInt(args[1]);

        // initialises the FileReader and BufferedReader for the "orders.txt" file
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        // this line builds the string representing the path to "orders.txt" file
        String fileName = "./" + folderName + "/orders.txt";

        try { // creates instances for the FileReader and BufferedReader
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // initialises the FileWriters and BufferedWriters for the two required output files
        FileWriter writerOrders = null;
        BufferedWriter bufferOrders = null;
        FileWriter writerProducts = null;
        BufferedWriter bufferProducts = null;

        try { // creates the required instances for the above FileWriters and BufferedWriters
            writerOrders = new FileWriter("orders_out.txt");
            bufferOrders = new BufferedWriter(writerOrders);
            writerProducts = new FileWriter("order_products_out.txt");
            bufferProducts = new BufferedWriter(writerProducts);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // the Semaphore is used to make sure that on the second level
        // of parallelism there are no more than the number of threads given
        Semaphore semaphoreForSecondLevel = new Semaphore(numberOfThreads);

        // the array of threads for the first level of parallelism
        Thread[] t = new Thread[numberOfThreads];
        // generates the threads - instanced by the OrderThread class
        for (int i = 0; i < numberOfThreads; i++) {
            t[i] = new Thread(new OrderThread(bufferedReader,
                   semaphoreForSecondLevel, folderName, bufferOrders, bufferProducts));
            t[i].start();
        }

        for (int i = 0; i < numberOfThreads; i++) { // joins the order threads
            try {
                t[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try { // closes all the above instanced objects
            if (bufferedReader != null) bufferedReader.close();
            if (fileReader != null) fileReader.close();
            if (bufferOrders != null) bufferOrders.close();
            if (writerOrders != null) writerOrders.close();
            if (bufferProducts != null) bufferProducts.close();
            if (writerProducts != null) writerProducts.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}