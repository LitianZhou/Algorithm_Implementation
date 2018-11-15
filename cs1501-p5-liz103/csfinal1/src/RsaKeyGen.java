import java.util.Random;
import java.math.BigInteger;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;

public class RsaKeyGen {
    
    public static void main(String[] args){
        LargeInteger p, q, n, PHI,d; 
        LargeInteger one = new LargeInteger(new byte[]{(byte)1});
        LargeInteger two = new LargeInteger(new byte[]{(byte)2});
        LargeInteger e = new LargeInteger(new byte[]{(byte)3});
        Random rnd = new Random();
        boolean eIsValid = false;
        
        System.out.println("Loading...");
        
        //Generate the keys, and make sure I get a 512 bit key
        do{
            p = new LargeInteger(256, rnd);
            q = new LargeInteger(256, rnd);
            n = new LargeInteger(p.multiply(q).getVal());
        }while(n.getVal().length != 64);                         //8 bits per byte * 64 = 512bits
        
        
        p.subtract(one);                                            //Calculating (p - 1)
        q.subtract(one);                                            //Calculating (q - 1)
        
        PHI = new LargeInteger((p.multiply(q)).getVal());         //(p - 1)(q - 1), I can calculate PhiN with my own methods

        do {
            if (e.compareTo(PHI) >= 0) {                       // Breaks out of the loop if e grows too large
                    break;
            }
            else if (e.XGCD(PHI)[0].compareTo(one) != 0) {
                    e = e.add(two);
            }
            else {
                    eIsValid = true;
            }
        } 
        while (!eIsValid);                                       // Loop while e is not co-prime with phi(n)
        
            
        if (!eIsValid) {                                           //Check that a valid e was generated      
                System.out.printf("Unforunately, there was a problem generating the keypairs.  Please try again\n");
                System.exit(1);
        }

        d =  e.XGCD(PHI)[1];                      //Generate d
        writeKeys(n,d,e);                                          //Write keys to pubkey.rsa and privkey.rsa
        System.out.println("Keys written successfully");
    }
    

    public static void writeKeys(LargeInteger n, LargeInteger d, LargeInteger e){
        
        //Writing our pubkey.rsa
        try{
            FileOutputStream fileOut = new FileOutputStream("pubkey.rsa");      
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            
            objectOut.writeObject(e);
            objectOut.writeObject(n);
            
            objectOut.close();
        }catch(IOException exception){
            System.err.println(exception);
            System.exit(1);
        }
        
        //Write our privkey.rsa
        try{
            FileOutputStream fileOut = new FileOutputStream("privkey.rsa");
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(d);
            objectOut.writeObject(n);
            objectOut.close();
        }catch(IOException exception){
            System.err.println(exception);
            System.exit(1);
        }
    }
}
