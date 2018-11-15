import java.util.*;
import java.io.*;
import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;

public class LargeInteger implements java.io.Serializable {
	
	private final byte[] ONE = {(byte) 1};
	private final byte[] ZERO = {(byte) 0};
	private byte[] val;

	/**
	 * Construct the LargeInteger from a given byte array
	 * @param b the byte array that this LargeInteger should represent
	 */
	public LargeInteger(byte[] b) {
		val = b;
	}

	/**
	 * Construct the LargeInteger by generatin a random n-bit number that is
	 * probably prime (2^-100 chance of being composite).
	 * @param n the bitlength of the requested integer
	 * @param rnd instance of java.util.Random to use in prime generation
	 */
	public LargeInteger(int n, Random rnd) {
		val = BigInteger.probablePrime(n, rnd).toByteArray();
	}
	
	/**
	 * Return this LargeInteger's val
	 * @return val
	 */
	public byte[] getVal() {
		return val;
	}

	/**
	 * Return the number of bytes in val
	 * @return length of the val byte array
	 */
	public int length() {
		return val.length;
	}

	/** 
	 * Add a new byte as the most significant in this
	 * @param extension the byte to place as most significant
	 */
	public void extend(byte extension) {
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
	public boolean isNegative() {
		return (val[0] < 0);
	}

	/**
	 * Computes the sum of this and other
	 * @param other the other LargeInteger to sum with this
	 */
	public LargeInteger add(LargeInteger other) {
		byte[] a, b;
		// If operands are of different sizes, put larger first ...
		if (val.length < other.length()) {
			a = other.getVal();
			b = val;
		}
		else {
			a = val;
			b = other.getVal();
		}

		// ... and normalize size for convenience
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFF;// 32bit 1s
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

	public byte[] getBigIntArray(){
		if(val[0] < 0){
			byte[] temp = new byte[val.length + 1];
			System.arraycopy(val, 0, temp, 1, val.length);
			return temp;
		}
		return val;
	}

	public int compareTo (LargeInteger b){
		return compareTo(b.getVal());
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

	/**
	 * Negate val using two's complement representation
	 * @return negation of this
	 */
	public LargeInteger negate() {
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
		for (int i  = 0; i < val.length; i++) {
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
	public LargeInteger subtract(LargeInteger other) {
		return this.add(other.negate());
	}

	public LargeInteger shift(LargeInteger lint, int amount){
		//shift once each time
		//System.out.print("Shift input:");
		//print(lint);
		int create=amount/8;
		int shl=amount-8*create;
		int len=lint.length()+create+1;
		byte[] arr=lint.getVal();
		byte[] new_arr=new byte[len];
		Arrays.fill(new_arr,(byte)0);
		
		if (len==1){
			System.out.println("Input for shift is wrong");
			return null;
		}
		//System.out.println(len+" "+lint.length());
		//System.out.println(create+" "+shl);
		if (shl>0){
			byte tmp=(byte)((0xff&arr[0])>>>(8-shl));
			//print(tmp);
			new_arr[0]=tmp;
			for (int i=0;i<lint.length()-1;i++ ) {
				//System.out.print("higher: ");
				//print((byte)(arr[i]<<shl));
				//System.out.print("lower: ");
				//print((byte)((0xff&arr[i+1])>>>(8-shl)));
				tmp=(byte)((arr[i]<<shl)+((0xff&arr[i+1])>>>(8-shl)));
				//print(tmp);
				new_arr[i+1]=tmp;
			}
			tmp=(byte)(arr[lint.length()-1]<<shl);
			//print(tmp);
			new_arr[lint.length()]=(byte)(arr[lint.length()-1]<<shl);
		} else {
			for (int i=0;i<lint.length();i++ ) {
				new_arr[i+1]=arr[i];
			}
		}
		//System.out.println("WTF");

		LargeInteger res=new LargeInteger(new_arr);
		//System.out.print("Shift resut:");
		//print(res);
		if  (new_arr[0]==0 && !(new_arr[1]<0)) res=res.reduce(1);
		//System.out.print("Shift resut:");
		//print(res);
		return res;
	}

	public LargeInteger reduce(int num) {

		int len=this.length()-num;
		byte[] new_arr=new byte[len];
		Arrays.fill(new_arr,(byte)0);
		for (int i=0;i<len;i++ ) {
			new_arr[i]=val[i+num];
		}
		LargeInteger res=new LargeInteger(new_arr);
		return res;
	}

	/**
	 * Compute the product of this and other
	 * @param other LargeInteger to multiply by this
	 * @return product of this and other
	 */
	// public LargeInteger multiply(LargeInteger other){
	// 	return karatsuba(this,other);
	// }

	// public LargeInteger karatsuba(LargeInteger x, LargeInteger y){
	// 	int max_size=Math.max(x.length(),y.length());
	// 	if (max_size<2) return (this.mult(other));
	// 	byte[] arr1=x.getVal();
	// 	byte[] arr2=y.getVal();
	// 	int sizeh=max_size/2;
	// 	int sizel=max_size-sizeh;
	// 	byte[] A1=new byte[sizeh*2];//xh
	// 	byte[] A2=new byte[sizeh*2];//yh
	// 	byte[] A3=new byte[sizel*2];//xl
	// 	byte[] A4=new byte[sizel*2];//yl
	// 	for (int i=sizel-1;i>=0;i++) {
	// 		A3[i]=arr1[arr.]
	// 	}
	// 	for (int i=0;i<sizeh;i++) {
	// 		if (size-i<x.length())A1[size-i]=	
	// 	}		
	// }
	public LargeInteger multiply(LargeInteger other) {
		// YOUR CODE HERE (replace the return, too...)
		//implementing grade school
		// System.out.println("Input");
		// print(this);
		// print(other);
		// System.out.println("__________________________________");
		int negative=0;//mod 2 =1: result negative
		LargeInteger mult1=new LargeInteger(this.getVal());
		LargeInteger mult2=new LargeInteger(other.getVal());
		// System.out.println("Multis");
		// print(mult1);
		// print(mult2);
		// System.out.println("__________________________________");
		if (this.isNegative()==true){
			negative++;
			mult1=this.negate();
		}

		if (other.isNegative()==true){
			negative++;
			mult2=other.negate();
		}

		int len=mult1.length()+mult2.length()+1;
		byte[] multi2=other.getVal();
		LargeInteger result=new LargeInteger(ZERO);
		LargeInteger multip=new LargeInteger(mult1.getVal());
		//negative check
		//multiplication
		
		String s="";
		for (int i=0;i<mult2.length();i++) {
			s += String.format("%8s", Integer.toBinaryString(mult2.getVal()[i] & 0xFF)).replace(' ', '0');
		}
		char[] s1=s.toCharArray();
		for (int i=0;i<s1.length;i++){
			if (s1[i]=='0') {s1[i]='*';}
			else break;
		}
		for (int i=s1.length-1;i>=0;i--){
			if (s1[i]=='*') break;
			if (s1[i]=='1') {
				// print(result);
				// print(multip);
				result=result.add(multip);

			}
			multip=multip.shift(multip,1);
			//print(multip);
		}

		int numzero=1;
		for (int i=0;i<result.length()-1;i++) {
			if (result.getVal()[i]>0 || (result.getVal()[i]==0)&&(result.getVal()[i+1]<0)){
				break;
			} else if (result.getVal()[i]==0)numzero++;
		}
		if (numzero>1){
			result=result.reduce(numzero-1);
		}
		//negatvie achieve
		if (negative%2!=0) result=result.negate();
		return result;
	}
	
	/**
	 * Run the extended Euclidean algorithm on this and other
	 * @param other another LargeInteger
	 * @return an array structured as follows:
	 *   0:  the GCD of this and other
	 *   1:  a valid x value
	 *   2:  a valid y value
	 * such that this * x + other * y == GCD in index 0
	 */
	 public LargeInteger[] XGCD(LargeInteger other) {
		// YOUR CODE HERE (replace the return, too...)
		return xgcd(this,other);
	 }


    //p this; q other; 
	public LargeInteger[] xgcd(LargeInteger p,LargeInteger q){
	 	if(q.isZero()){
	 		LargeInteger z=new LargeInteger(ZERO);
	 		LargeInteger o=new LargeInteger(ONE);
	 		LargeInteger[] ret=new LargeInteger[3];
	 		ret[0]=p; ret[1]=o; ret[2]=z;
	 		return ret;
	 	}
	 	// System.out.println("p q:"+str(p)+" "+str(q));

	 	LargeInteger[] div_res=div(p,q);
	 	// System.out.println("para:"+str(div_res[0])+","+str(div_res[1]));
	 	LargeInteger[] val=xgcd(q,div_res[0]);
	 	LargeInteger d=val[0];
	 	LargeInteger a=val[2];
	 	LargeInteger tmp=div_res[1].multiply(a);
	 	// System.out.println("div_res result");
	 	// print(a);
	 	// print(div_res[1]);
	 	// print(tmp);
	 	LargeInteger b=val[1].subtract(tmp);
	 	LargeInteger[] ret=new LargeInteger[3];
	 	ret[0]=d; ret[1]=a; ret[2]=b;
	 	// System.out.println("XGCD result");
	 	// print(ret,p,q);
	 	
	 	// print(ret[0]);
	 	// print(ret[1]);
	 	// print(ret[2]);
	 	return ret;
	}

	 /**
	  * Compute the result of raising this to the power of y mod n
	  * @param y exponent to raise this to
	  * @param n modulus value to use
	  * @return this^y mod n
	  */
	 public LargeInteger modularExp(LargeInteger y, LargeInteger n) {
		// YOUR CODE HERE (replace the return, too...)
		LargeInteger ct=new LargeInteger(y.getVal());
		if (ct.isNegative()==true){
			System.out.println("Negative is not supported!");
			return null;
		} 

		//multiplication
		String s="";
		for (int i=0;i<ct.length();i++) {
			s += String.format("%8s", Integer.toBinaryString(ct.getVal()[i] & 0xFF)).replace(' ', '0');
		}
		char[] s1=s.toCharArray();
		for (int i=0;i<s1.length;i++){
			if (s1[i]=='0') {s1[i]='*';}
			else break;
		}

		LargeInteger res=new LargeInteger(ONE);
		for (int i=0;i<s1.length;i++ ) {
			// System.out.println("res^2");
			// System.out.println(s1[i]);
			res=res.multiply(res);
			if (s1[i]=='1') {
				// System.out.println("times x");
				res=res.multiply(this);
			}
			
			
			res=res.div(res,n)[0];
			// System.out.println("result");
			// print(res);
		}
		return res;
	 }


	 public boolean isZero(){
	 	for (int i=0;i<val.length;i++ ) {
	 		if (val[i]!=0)return false;
	 	}
	 	return true;
	 }

	 public LargeInteger Euler(LargeInteger p,LargeInteger q){
	 	LargeInteger tmp=new LargeInteger(ONE);
	 	LargeInteger mult1=p.subtract(tmp);
		LargeInteger mult2=q.subtract(tmp);
		return mult1.multiply(mult2);
	 }

	 //div: ret[0]: residual; ret[1]:quotient
	 public LargeInteger[] div(LargeInteger x, LargeInteger y){
	 	boolean negativex=false;
	 	boolean negativey=false;
	 	LargeInteger one=new LargeInteger(ONE);
	 	LargeInteger zero=new LargeInteger(ZERO);
	 	LargeInteger y1=new LargeInteger(y.getVal());
	 	String s1,s2;

	 	if (x.isNegative()){
	 		s1=str(x.negate());
	 		negativex=true;
	 	} else {
	 		s1=str(x);
	 	}

	 	if (y.isNegative()){
	 		s2=str(y.negate());
	 		negativey=true;
	 		y1=y.negate();
	 		System.out.println("Something super wrong!!!");
	 	} else {
	 		s2=str(y);
	 	}

	 	char[] ch1=s1.toCharArray();
	 	char[] ch2=s2.toCharArray();

	 	for (int i=0;i<ch1.length;i++) {
	 		if (ch1[i]!='0') break;
	 		ch1[i]='*';
	 	}
	 	for (int i=0;i<ch2.length;i++) {
	 		if (ch2[i]!='0') break;
	 		ch2[i]='*';
	 	}
	 	s1="";
	 	for (int i=0;i<ch1.length;i++ ) {
			if (ch1[i]=='*') continue;
			s1+=ch1[i];
	 	}
	 	s2="";
	 	for (int i=0;i<ch2.length;i++ ) {
			if (ch2[i]=='*') continue;
			s2+=ch2[i];
	 	}
	 	// System.out.println(s1);
	 	// System.out.println("Divided by");
	 	// System.out.println(s2);
	 // 	System.out.println("Division input");
		// System.out.println(s1+"&"+s2);
	 	int len=s1.length()-s2.length();
	 	LargeInteger ans=new LargeInteger(ZERO);
	 	
	 	LargeInteger rem;
	 	if (x.isNegative()) {rem=x.negate();}
	 	else {rem=new LargeInteger(x.getVal());}

	 	LargeInteger[] ret=new LargeInteger[2];
	 	// System.out.println("len: "+len);
	 	if (len<0){
	 		if (!(negativex^negativey)) {
	 			ret[1]=zero;
	 			ret[0]=new LargeInteger(x.getVal());
	 		} else{
	 			ret[1]=zero.subtract(one);
	 			ret[0]=x.add(y);
	 			int numzero=1;
				for (int i=0;i<ret[0].length()-1;i++) {
					if (ret[0].getVal()[i]>0 || (ret[0].getVal()[i]==0)&&(ret[0].getVal()[i+1]<0)){
						break;
					} else if (ret[0].getVal()[i]==0)numzero++;
				}
				if (numzero>1){
					ret[0]=ret[0].reduce(numzero-1);
				}
	 		}
	 		return ret;
	 	} else {
	 		// System.out.println(str(rem));
	 		for (int i=len;i>=0;i--) {
	 			LargeInteger tmp=y1.shift(y1,i);
	 			// System.out.println("shift: "+str(tmp));
	 			if (rem.subtract(tmp).isNegative()) continue;
	 			// System.out.println("- "+str(tmp));
	 			ans=ans.add(one.shift(one,i));
	 			rem=rem.subtract(tmp);
	 			// System.out.println(str(rem));	 			
	 		}
	 		
	 		int numzero=1;
			for (int i=0;i<ans.length()-1;i++) {
				if (ans.getVal()[i]>0 || (ans.getVal()[i]==0)&&(ans.getVal()[i+1]<0)){
					break;
				} else if (ans.getVal()[i]==0)numzero++;
			}
			if (numzero>1){
				ans=ans.reduce(numzero-1);
			}
	 		
	 		numzero=1;
	 		for (int i=0;i<rem.length()-1;i++) {
				if (rem.getVal()[i]>0 || (rem.getVal()[i]==0)&&(rem.getVal()[i+1]<0)){
					break;
				} else if (rem.getVal()[i]==0)numzero++;
			}
			if (numzero>1){
				rem=rem.reduce(numzero-1);
			}

			ret[0]=rem;
			ret[1]=ans;
	 		return ret;
	 	}
	 }
	 public LargeInteger[] divi(LargeInteger x, LargeInteger y){
	 	// System.out.println("Division input");
	 	// print(x);
	 	// print(y);
	 	LargeInteger ct=new LargeInteger(ZERO);
	 	LargeInteger tmp=new LargeInteger(ONE);
	 	LargeInteger res=new LargeInteger(x.getVal());
	 	while (res.subtract(y).isNegative()==false) {
	 		res=res.subtract(y);
	 		ct=ct.add(tmp);
	 	}
	 	if (res.isNegative()) {
	 		res=res.add(y);
	 		ct=ct.subtract(tmp);
	 	}

	 	int numzero=1;
		for (int i=0;i<res.length()-1;i++) {
			if (res.getVal()[i]>0 || (res.getVal()[i]==0)&&(res.getVal()[i+1]<0)){
				break;
			} else if (res.getVal()[i]==0)numzero++;
		}
		if (numzero>1){
			res=res.reduce(numzero-1);
		}

	 	LargeInteger[] ret=new LargeInteger[2];
	 	ret[0]=res;
	 	ret[1]=ct;
	 	// System.out.println("Division result");
	 	// print(res);
	 	// print(ct);
	 	return ret;
	 }
	 public void print(byte b){
	 	System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
	 }

	 public void print(LargeInteger num){
		byte[] tmp=num.getVal();
		String s1="";
		for (int i=0;i<num.length();i++) {
			s1 += String.format("%8s", Integer.toBinaryString(tmp[i] & 0xFF)).replace(' ', '0')+" ";
		}
		System.out.print(s1);
	}

	public String str(LargeInteger num){
		byte[] tmp=num.getVal();
		String s1="";
		for (int i=0;i<num.length();i++) {
			s1 += String.format("%8s", Integer.toBinaryString(tmp[i] & 0xFF)).replace(' ', '0')+" ";
		}
		return s1;
	}
	 
	public void print(LargeInteger[] xgcd,LargeInteger number1, LargeInteger number2){
		System.out.println(str(xgcd[0])+"="+str(xgcd[1])+"*"+str(number1)+"+"+str(xgcd[2])+"*"+str(number2));
	}


    // public byte[] stb(String s){
	// 	int len=string
	// }
}
