import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class ProductThread implements Runnable{
    private final String orderID; // the id of the order for which each thread must find a product
    private final BufferedReader productBufferReader; // the buffered reader for the input file "order_products.txt"
    private final BufferedWriter bufferProducts; // the buffered writer for the shipped products
    private final Semaphore secondLevelOfParallelism;
    // the semaphore that makes sure there are no more than the
    // required number of threads on the second level of parallelism

    // the constructor of the class that stores the above explained information about each product thread
    public ProductThread(String orderID,
                         BufferedReader productBufferReader,
                         BufferedWriter bufferProducts,
                         Semaphore semaphore) {
        this.orderID = orderID;
        this.productBufferReader = productBufferReader;
        this.bufferProducts = bufferProducts;
        this.secondLevelOfParallelism = semaphore;
    }

    @Override
    public void run() {
        // this while ensures that the entire "order_products.txt" file is read concurrently by all the threads
        while (true) {
            String line = null;
            synchronized (this) { // reading the lines must be synchronized
                try { // each thread gets a line from the input file
                    line = productBufferReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (line != null) { // the line is valid
                String[] arrProducts = line.split(","); // the line is split by ','
                String orderId = arrProducts[0];
                String productId = arrProducts[1];
                // checks if the found order id matches the one given in the constructor
                if (this.orderID.equals(orderId)) {
                    synchronized (this) { // if there's a match, the thread writes 'shipped' for the product
                        try { // the writing process must be synchronized
                            bufferProducts.write(this.orderID + "," + productId + ",shipped\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break; // each thread is only looking for one match on a product
                }
            }
        }
        secondLevelOfParallelism.release(); // the thread has finished and allows another to be executed
    }
}
