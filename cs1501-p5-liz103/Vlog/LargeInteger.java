import java.util.Random;
import java.math.BigInteger;
import java.io.Serializable;

public class LargeInteger implements Serializable {

    private final byte[] ONE;
    private final byte[] ZERO;
    private final static long LONG_MASK = 0xffffffffL;
    byte[] val;

    //Constructor that takes val byte[] and removs the sign bit if necessary
    public LargeInteger(byte[] b) {
        val = b;
        if (val[0] == 0) {
            //System.out.println("size: " + val.length);
            byte[] temp = new byte[val.length - 1];
            System.arraycopy(val, 1, temp, 0, val.length - 1);
            val = temp;
        }
        ONE = new byte[]{(byte) 1};
        ZERO = new byte[]{(byte) 0};
    }

    //Constructor that takes a byte and makes an array of size 1 from it
    public LargeInteger(byte bb) {
        val = new byte[1];
        val[0] = bb;
        ONE = new byte[]{(byte) 1};
        ZERO = new byte[]{(byte) 0};
    }

    //constructor that makes a random prime number in LargeInteger
    public LargeInteger(int n, Random rnd) {
        val = BigInteger.probablePrime(n, rnd).toByteArray();
        ONE = new byte[]{(byte) 1};
        ZERO = new byte[]{(byte) 0};
    }

    /**
     * Return this LargeInteger's val
     * @return val
     */
    public byte[] getVal () {
        return val;
    }

    /**
     * Return the number of bytes in val
     * @return length of the val byte array
     */
    public int length () {
        return val.length;
    }
    /**
     * Add a new byte as the most significant in this
     * @param extension the byte to place as most significant
     */
    public void extend ( byte extension){
        byte[] newv = new byte[val.length + 1];
        newv[0] = extension;
        for (int i = 0; i < val.length; i++) {
            newv[i + 1] = val[i];
        }
        val = newv;
    }

    /**
     * If this is negative, most significant bit will be 1 meaning most
     * significant byte will be a negative signed number
     * @return true if this is negative, false if positive
     */
    public boolean isNegative () {
        return (val[0] < 0);
    }

    public boolean equal ( byte[] b){
        if (val.length != b.length)
            return false;
        for (int i = 0; i < val.length; i++)
            if (val[i] != b[i])
                return false;
        return true;
    }

    public boolean equalsZero ( byte[] a){
        if (val.length == 1 && val[0] == 0)
            return true;
        return false;
    }

    public int compareTo (LargeInteger b){
        return compareTo(b.getArray());
    }


    public int compareTo ( byte[] b){
        if (val.length > b.length)
            return 1;
        if (b.length > val.length)
            return -1;

        for (int i = 0; i < val.length; i++) {
            if ((val[i] & 0xFF) > (b[i] & 0xFF))
                return 1;
            if ((b[i] & 0xFF) > ((val[i]) & 0xFF))
                return -1;
        }

        return 0;
    }


    public byte[] add ( byte[] x){
        return add(val, x);
    }

    private byte[] add ( byte[] x, byte[] y){
        if (x.length < y.length) {
            byte[] tmp = new byte[y.length];
            System.arraycopy(x, 0, tmp, (y.length - x.length), x.length);
            x = tmp;
        } else if (y.length < x.length) {
            byte[] tmp = new byte[x.length];
            System.arraycopy(y, 0, tmp, (x.length - y.length), y.length);
            y = tmp;
        }

        int xIndex = x.length;
        int yIndex = y.length;
        byte result[] = new byte[xIndex];
        int sum = 0;
        int carryBit = 0;

        // Add common parts of both numbers
        while (yIndex > 0) {
            sum = (x[--xIndex] & 0xFF) + (y[--yIndex] & 0xFF) + carryBit;
            if (sum > 255)
                carryBit = 1;
            else
                carryBit = 0;
            result[xIndex] = (byte) sum;
        }

        if (carryBit == 1) {
            byte[] tmp = new byte[result.length];
            for (int i = 0; i < result.length; i++)
                tmp[i] = result[i];

            result = new byte[result.length + 1];
            result[0] = 1;
            for (int i = 1; i < result.length; i++)
                result[i] = tmp[i - 1];
        }
        return result;
    }

    //wrapper
    public byte[] multiply (LargeInteger b){
        return multiply(b.getArray());
    }


    public byte[] multiply ( byte[] y){
        if (val.length < y.length) {
            byte[] tmp = val;
            val = y;
            y = tmp;
        }

        byte[] result = new byte[y.length];
        int xIndex = val.length - 1;
        int shiftCounter = 0;

        while (xIndex >= 0) {
            for (int i = 0; i <= 7; i++) {
                if (((val[xIndex] >> i) & 0x01) == 1) {
                    result = add(result, shift(y, shiftCounter));
                }
                shiftCounter++;
            }
            xIndex--;
        }
        return result;
    }


    public byte[] shift ( int shiftAMT){
        return shift(val, shiftAMT);
    }


    private byte[] shift ( byte[] array, int shiftAMT){
        if (shiftAMT == 0)
            return array;
        byte[] result;

        int indexesToAdd = shiftAMT / 8;

        result = smallShift(array, shiftAMT % 8);
        if (indexesToAdd != 0) {
            byte[] temp = new byte[result.length + indexesToAdd];
            System.arraycopy(result, 0, temp, 0, result.length);
            result = temp;
        }
        return result;
    }

    private byte[] smallShift ( byte[] array, int shiftAMT){
        byte newByte = 0;
        byte oldByte = 0;
        byte[] result = new byte[array.length];

        for (int i = array.length - 1; i >= 0; i--) {
            if (i == 0) {
                newByte = (byte) (array[i] >> (8 - shiftAMT));
                newByte = (byte) (newByte & getToAND(shiftAMT));
                result[i] = (byte) (array[i] << shiftAMT);
                result[i] = (byte) (result[i] | oldByte);
                if (newByte != 0) {
                    byte[] temp = new byte[array.length + 1];
                    System.arraycopy(result, 0, temp, 1, result.length);
                    result = temp;
                    result[0] = newByte;
                }
            } else {
                newByte = (byte) (array[i] >> (8 - shiftAMT));
                newByte = (byte) (newByte & getToAND(shiftAMT));
                result[i] = (byte) (array[i] << shiftAMT);
                result[i] = (byte) (result[i] | oldByte);
                oldByte = newByte;
            }
        }

        return result;
    }


    public void subtractOne () {
        for (int i = val.length - 1; i >= 0; i--) {
            if (val[i] == 0)
                val[i] = (byte) (val[i] | 0xFF);
            else {
                val[i] = (byte) ((val[i] & 0xFF) - 1);
                break;
            }
        }
        val[val.length - 1] = (byte) (val[val.length - 1] & 0xFE);
    }


    public byte[] getArray () {
        return val;
    }


    public byte[] getBigIntArray () {
        if (val[0] < 0) {
            byte[] temp = new byte[val.length + 1];
            System.arraycopy(val, 0, temp, 1, val.length);
            return temp;
        }
        return val;
    }

    //byte wise subtraction
    public byte[] substract ( byte[] b){
        byte[] result = new byte[val.length];
        byte[] temp = new byte[val.length];
        System.arraycopy(val, 0, result, 0, val.length);
        int resultIndex = result.length - 1;
        int bIndex = b.length - 1;

        while (resultIndex >= 0 && bIndex >= 0) {
            if (((result[resultIndex] & 0xFF) - (b[bIndex] & 0xFF)) < 0) {
                for (int i = resultIndex; i > 0; i--) {
                    if (result[resultIndex - 1] != 0) {
                        result[i - 1] = (byte) ((result[i - 1] & 0xFF) - 1);
                        break;
                    } else
                        result[i - 1] = (byte) (result[i - 1] | 0xFF);
                }
                result[resultIndex] = (byte) ((result[resultIndex] & 0xFF) - (b[bIndex] & 0xFF));
            } else {
                result[resultIndex] = (byte) ((result[resultIndex] & 0xFF) - (b[bIndex] & 0xFF));
            }
            resultIndex--;
            bIndex--;
        }

        return result;
    }

    /**
     * Computes the sum of this and other
     * @param other the other LargeInteger to sum with this
     */
    public LargeInteger add (LargeInteger other){
        byte[] a, b;
        // If operands are of different sizes, put larger first ...
        if (val.length < other.length()) {
            a = other.getVal();
            b = val;
        } else {
            a = val;
            b = other.getVal();
        }

        // ... and normalize size for convenience
        if (b.length < a.length) {
            int diff = a.length - b.length;

            byte pad = (byte) 0;
            if (b[0] < 0) {
                pad = (byte) 0xFF;
            }

            byte[] newb = new byte[a.length];
            for (int i = 0; i < diff; i++) {
                newb[i] = pad;
            }

            for (int i = 0; i < b.length; i++) {
                newb[i + diff] = b[i];
            }

            b = newb;
        }

        // Actually compute the add
        int carry = 0;
        byte[] res = new byte[a.length];
        for (int i = a.length - 1; i >= 0; i--) {
            // Be sure to bitmask so that cast of negative bytes does not
            //  introduce spurious 1 bits into result of cast
            carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

            // Assign to next byte
            res[i] = (byte) (carry & 0xFF);

            // Carry remainder over to next byte (always want to shift in 0s)
            carry = carry >>> 8;
        }

        LargeInteger res_li = new LargeInteger(res);

        // If both operands are positive, magnitude could increase as a result
        //  of addition
        if (!this.isNegative() && !other.isNegative()) {
            // If we have either a leftover carry value or we used the last
            //  bit in the most significant byte, we need to extend the result
            if (res_li.isNegative()) {
                res_li.extend((byte) carry);
            }
        }
        // Magnitude could also increase if both operands are negative
        else if (this.isNegative() && other.isNegative()) {
            if (!res_li.isNegative()) {
                res_li.extend((byte) 0xFF);
            }
        }

        // Note that result will always be the same size as biggest input
        //  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
        return res_li;
    }

    /**
     * Negate val using two's complement representation
     * @return negation of this
     */
    public LargeInteger negate () {
        byte[] neg = new byte[val.length];
        int offset = 0;

        // Check to ensure we can represent negation in same length
        //  (e.g., -128 can be represented in 8 bits using two's
        //  complement, +128 requires 9)
        if (val[0] == (byte) 0x80) { // 0x80 is 10000000
            boolean needs_ex = true;
            for (int i = 1; i < val.length; i++) {
                if (val[i] != (byte) 0) {
                    needs_ex = false;
                    break;
                }
            }
            // if first byte is 0x80 and all others are 0, must extend
            if (needs_ex) {
                neg = new byte[val.length + 1];
                neg[0] = (byte) 0;
                offset = 1;
            }
        }

        // flip all bits
        for (int i = 0; i < val.length; i++) {
            neg[i + offset] = (byte) ~val[i];
        }

        LargeInteger neg_li = new LargeInteger(neg);

        // add 1 to complete two's complement negation
        return neg_li.add(new LargeInteger(ONE));
    }

    /**
     * Implement subtraction as simply negation and addition
     * @param other LargeInteger to subtract from this
     * @return difference of this and other
     */
    public LargeInteger subtract (LargeInteger other){
        return this.add(other.negate());
    }

//    /**
//     * Compute the product of this and other
//     * @param other LargeInteger to multiply by this
//     * @return product of this and other
//     */
//    public LargeInteger multiply(LargeInteger other) {
//        byte[] shortM;
//        byte[] longM;
//        if(this.length() > other.length()) {
//            longM = this.val;
//            shortM = other.val;
//        }
//        else {
//            shortM = this.val;
//            longM = other.val;
//        }
//        LargeInteger product = new LargeInteger(new byte[shortM.length+longM.length]);
//
//        for(int i = shortM.length - 1; i>=0; i--) {
//            int carry = 0;
//            byte[] curr = new byte[shortM.length + longM.length -i];
//            for(int j = longM.length -1; j >= 0; j--) {
//                int temp = shortM[i] * longM[j] + carry;
//                curr[j+1] = (byte) (0xFF & temp);
//                carry = temp >> 8;
//            }
//            LargeInteger curr_shortMi = new LargeInteger(curr);
//            product = product.add(curr_shortMi);
//        }
//
//        return product;
//    }

    /**
     * Run the extended Euclidean algorithm on this and other
     * @param other another LargeInteger
     * @return an array structured as follows:
     *   0:  the GCD of this and other
     *   1:  a valid x value
     *   2:  a valid y value
     * such that this * x + other * y, == GCD in index 0
     */
//    public LargeInteger[] XGCD(LargeInteger other) {
//        LargeInteger s = new LargeInteger(ZERO);
//        LargeInteger old_s = new LargeInteger(ONE);
//        LargeInteger t = new LargeInteger(ONE);
//        LargeInteger old_t = new LargeInteger(ZERO);
//        LargeInteger r = other;
//        LargeInteger old_r = this;
//
//        while(r.isNotZero()) {
//            LargeInteger quotient = old_r.dividedBy(r);
//            LargeInteger temp = old_r;
//            old_r = r;
//            r = temp.subtract(quotient.multiply(r));
//            r.trim();
//
//            temp = old_s;
//            old_s = s;
//            s = temp.subtract(quotient.multiply(s));
//            s.trim();
//
//            temp = old_t;
//            old_t = t;
//            t = temp.subtract(quotient.multiply(t));
//            t.trim();
//        }
//        LargeInteger[] result = {old_r, old_s, old_t};
//        return result;
//    }
//
//    private void trim() {
//        int length = this.getVal().length;
//        int zeroCounter = 0;
//        for(int i=0; i<length; i++) {
//            if(this.getVal()[i] == 0) zeroCounter++;
//        }
//        byte[] newB = new byte[length-zeroCounter];
//        for(int i = 0; i<length - zeroCounter-1; i++) {
//            newB[i] = this.getVal()[i+zeroCounter];
//        }
//
//    /**
//     * modular
//     */
//    public LargeInteger mod(LargeInteger n) {
//        return this.dividedBy(n);
//    }

    private byte getToAND ( int shiftAMT){
        if (shiftAMT == 1)
            return 1;
        else if (shiftAMT == 2)
            return 3;
        else if (shiftAMT == 3)
            return 7;
        else if (shiftAMT == 4)
            return 15;
        else if (shiftAMT == 5)
            return 31;
        else if (shiftAMT == 6)
            return 63;
        else if (shiftAMT == 7)
            return 127;
        return 0;
    }

    public void printByteArray () {
        printByteArray(val);
    }
    public void printByteArray ( byte[] val){
        for (byte b : val)
            System.out.print(b + "  ");
        System.out.println();
    }
}

