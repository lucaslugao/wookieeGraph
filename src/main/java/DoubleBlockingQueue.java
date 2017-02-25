import java.util.ArrayList;

/**
 * Created by Paulo on 2/24/2017.
 */
public class DoubleBlockingQueue {

    public DoubleBlockingQueue(){

    }

    public String getFromOrigin(){
        return "Yoda";
    }

    public void putInDestiny(ArrayList<String> s){
        System.out.println("Put " + s.size() + " items in destiny!");
    }
    public void putInOrigin(String s){
        System.out.println("Put " + s + " in origin!");
    }

    public void putInSolution(String s){
        System.out.println("Put " + s + " in solution!");
    }
    public ArrayList<String> swapAndDrain(){
        return new ArrayList<String>();
    }
    public void signalAll(){
        System.out.println("All threads signaled!");
    }
}
