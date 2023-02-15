import java.io.*;
import java.util.concurrent.Semaphore;

public class OrderThread implements Runnable {
    private final String folderName; // the name of the input folder
    private final BufferedReader bufferedReader;
    // the buffered reader from which the threads get each line of the "orders.txt" file
    private final BufferedWriter bufferOrders; // the buffered writer for the shipped orders
    private final BufferedWriter bufferProducts; // the buffered writer for the shipped products
    private final Semaphore secondLevelOfParallelism; // the semaphore for the second level of parallelism

    // the constructor of this class that stores the information needed by each order thread - the buffered reader
    // and the buffered writer - and also the information needed for generating the threads that handle each product
    // of an order - the semaphore, the folder name and the buffered writer for the product threads
    public OrderThread(BufferedReader bufferedReader,
                       Semaphore secondLevelOfParallelism,
                       String folderName,
                       BufferedWriter bufferOrders,
                       BufferedWriter bufferProducts) {
        this.bufferedReader = bufferedReader;
        this.secondLevelOfParallelism = secondLevelOfParallelism;
        this.folderName = folderName;
        this.bufferOrders = bufferOrders;
        this.bufferProducts = bufferProducts;
    }

    @Override
    public void run() {
        while (true) { // this while ensures that the entire "orders.txt" file is read concurrently by all the threads
            String line = null;
            synchronized (this) { // reading the lines must be synchronized
                try { // each thread gets a line from the input file
                    line = bufferedReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (line != null) { // the line is valid
                String[] arrayString = line.split(","); // the line is split by ','
                String orderID = arrayString[0]; // gets the order id
                int numberOfProducts = Integer.parseInt(arrayString[1]); // the number of products

                if (numberOfProducts > 0) { // checks if the command is valid
                    // initialises the FileReader and BufferedReader for the "order_products.txt" file
                    FileReader productFileReader = null;
                    BufferedReader productBufferedReader = null;
                    // builds the string representing the path to "order_products.txt" file
                    String fileName = "./" + folderName + "/order_products.txt";
                    // the array of threads for the second level of parallelism
                    Thread[] p = new Thread[numberOfProducts];

                    try { // creates instances for the FileReader and BufferedReader
                        productFileReader = new FileReader(fileName);
                        productBufferedReader = new BufferedReader(productFileReader);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    // generates the threads - instanced by the ProductThread class
                    for (int i = 0; i < numberOfProducts; i++) {
                        // makes sure that at the second level of parallelism
                        // there are no more than the allowed number of threads
                        try {
                            secondLevelOfParallelism.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        p[i] = new Thread(new ProductThread(orderID, productBufferedReader,
                                                bufferProducts, this.secondLevelOfParallelism));
                        p[i].start();
                    }

                    for (int i = 0; i < numberOfProducts; i++) { // joins the product threads
                        try {
                            p[i].join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    try { // closes the FileReader and BufferedReader
                        if (productBufferedReader != null) productBufferedReader.close();
                        if (productFileReader != null) productFileReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // all the products have been processed, so each order
                    // thread must write 'shipped' in the required output file
                    synchronized (this) {
                        try {
                            bufferOrders.write(orderID + "," + numberOfProducts + ",shipped\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else break; // the line is null, meaning that there are no more lines to read
        }
    }
}
