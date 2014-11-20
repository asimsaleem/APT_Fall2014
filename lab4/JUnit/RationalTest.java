//package Junit;

import junit.framework.TestCase;

public class RationalTest extends TestCase {

    protected Rational HALF;

    protected void setUp() {
      HALF = new Rational( 1, 2 );
    }

    // Create new test
    public RationalTest (String name) {
        super(name);
    }


    public void testEquality() {
        assertEquals(new Rational(1,3), new Rational(1,3));
        assertEquals(new Rational(1,3), new Rational(2,6));
        assertEquals(new Rational(3,3), new Rational(1,1));
    }

    // Test for nonequality
    public void testNonEquality() {
        assertFalse(new Rational(2,3).equals(new Rational(1,3)));
    }

    public void testAccessors() {
    	assertEquals(new Rational(2,3).numerator(), 2);
    	assertEquals(new Rational(2,3).denominator(), 3);
    }

    public void testRoot() {
        Rational s = new Rational( 1, 4 );
        Rational sRoot = null;
        try {
            sRoot = s.root();
        } catch (IllegalArgumentToSquareRootException e) {
            e.printStackTrace();
        }
        assertTrue( sRoot.isLessThan( HALF.plus( Rational.getTolerance() ) ) 
                        && HALF.minus( Rational.getTolerance() ).isLessThan( sRoot ) );
    }

    
    /*** Newly Added Test Cases - START ***/
    public void testDivides(){
    	System.out.println("Executing test...");
    	//Simple Starter Test Case to verify if the Divides function works as expected when different combos are used
        Rational top = new Rational(2, 3);
        Rational bottom = new Rational(4, 6); 
        Rational sDivide1 = top.divides(bottom);
        Rational sDivide2 = bottom.divides(top);
        assertEquals ( sDivide1, sDivide2);
        System.out.println("Test Completed.");
    }
    
    public void testZerosInputToGCD(){
    	System.out.println("Executing test...");
    	//Expecting Error because GCD occurs on a non-zero number
    	//Constructor for Rational does not check if the result returned by GCD is 0 or not and tries dividing
    	//the numerator and denominator by the value (0) and hence blows up
    	assertEquals(new ArithmeticException(), new Rational(0,0));
        System.out.println("Test Completed.");
    }

    public void testZerosInputToPlus(){
    	System.out.println("Executing test...");
        Rational a = new Rational(0, 0);
        Rational b = new Rational(0, 0);
         //To calculate the Rational z, it tries to divide numerator and denominator with the value 
        //returned by GCD. It doesn't check to see if the return value is 0 or not. 
        assertEquals(new ArithmeticException(), a.plus(b)); 
        System.out.println("Test Completed.");
    }

    public void testZerosInputToMinus(){
    	System.out.println("Executing test...");
        Rational a = new Rational(0, 1);
        Rational b = new Rational(0, 1);
        //Expected output should be 0 but it throws an Java Lang Exception which is not handled internally
        assertEquals(new ArithmeticException(), a.minus(b)); 
        System.out.println("Test Completed.");
    }
 
    public void testOnesNZerosInputToMinus(){
    	System.out.println("Executing test...");
        Rational a = new Rational(1, 0);
        Rational b = new Rational(1, 0);
        //Expected output should be 0 but it throws an Java Lang Exception which is not handled internally
        assertNotNull(a.minus(b)); 
        assertEquals(0, a.minus(b));
        System.out.println("Test Completed.");
    }
    
    public void testOnesInputToMinus(){
    	System.out.println("Executing test...");
        Rational a = new Rational(1, 1);
        Rational b = new Rational(1, 1);
        //Expected output should be 0 since the GCD for the input params will be 1 each. 
        assertEquals(0, a.minus(b)); 
        System.out.println("Test Completed.");
    }
    
    public void testZerosInputToDivides(){
    	System.out.println("Executing test...");
        Rational a = new Rational(0, 0);
        Rational b = new Rational(0, 0);
        //Expecting an exception or a 0 because the GCD for the above two variables must be 0
        assertEquals( 0 , a.divides(b));
        assertEquals(new ArithmeticException(), a.divides(b));
        System.out.println("Test Completed.");
    }

    public void testOnesInputToDivides(){
    	System.out.println("Executing test...");
    	Rational a = new Rational(1, 1);
        Rational b = new Rational(1, 1);
        //Expected 1 as the output because the GCD for the test variables will be 1
        //This would imply that a value of 1 divided by 1 should return 1 and match the condition
        assertEquals(a.divides(b), 1); 
        System.out.println("Test Completed.");
    }
    
    public void testComboWithNegativeNumbers(){
    	System.out.println("Executing test...");
        Rational a = new Rational(1, 1);
        Rational b = new Rational(-1, 1 );
        //Expected output should be 0 but it throws an Java Lang Exception which is not handled internally
        assertEquals(a.minus(b), 2); 
        System.out.println("Test Completed.");
    }

    public void testZerosInputToTimes(){
    	System.out.println("Executing test...");
    	Rational a = new Rational(0, 1);
        Rational b = new Rational(0, 1);
        //Expected output is 0, however due to the fact that no 0 check is present, this would result in exception
        assertEquals(a.times(b), 0);
        System.out.println("Test Completed.");
    }

    
    public void testOnesInputToTimes(){
    	System.out.println("Executing test...");
    	Rational a = new Rational(1, 1);
        Rational b = new Rational(1, 1);
        //Expected output is 1, however due to the fact that no 0 check is present, this would result in exception
        assertEquals(a.times(b), 1);
        System.out.println("Test Completed.");
    }
    
    public void testOnesNZerosInputToTimes(){
    	System.out.println("Executing test...");
    	Rational a = new Rational(1, 0);
        Rational b = new Rational(1, 0);
        //Expected output is 0, however due to the fact that no 0 check is present, this would result in exception
        assertEquals(a.times(b), 0);
        System.out.println("Test Completed.");
    }
    
    public void testComboInputToTimes(){
    	System.out.println("Executing test...");
    	Rational a = new Rational(-1, -1);
        Rational b = new Rational(1, 0);
        //Expected output is 0, however due to the fact that no 0 check is present, this would result in exception
        assertEquals(a.times(b), 0);
        System.out.println("Test Completed.");
    }
    
    
    public void testIsLessThanWithZerosInput(){
    	System.out.println("Executing test...");
    	Rational a = new Rational(1, 1);
    	Rational b = new Rational(1, 1);
    	//Expected output must be false because the values being used as input are same.
    	assertFalse(a.isLessThan(b));
        System.out.println("Test Completed.");
    }
    
    public void testNegativeInputToTimes(){
    	System.out.println("Executing test...");
        Rational a = new Rational(-1, -1);
        Rational b = new Rational(-1, -1);
        //Expected output to be a positive integer 1 since the input GCDs will return a value of 1 in an actual GCD
        assertEquals(a.times(b), 1);
        System.out.println("Test Completed.");
    }

    public void testComboInputToPlus(){
    	System.out.println("Executing test...");
    	Rational a = new Rational(-1, 1);
        Rational b = new Rational(-1, 1);
        //Expected output to be a positive integer 2 since the input GCDs will return a value of 1 in an actual scenario
        assertEquals(a.plus(b), 2);
        System.out.println("Test Completed.");
    }

    public void testZerosWithSetToleranceMethod(){
    	System.out.println("Executing test...");
        Rational a = new Rational(0, 0);
        Rational.setTolerance(a);
        //Expected output for tolerance is 0 since we are passing the input as 0 for the Rational input
        assertEquals(Rational.getTolerance(), 0);
        System.out.println("Test Completed.");
    }

    public void testAbsWithPosNegComboValues(){
    	System.out.println("Executing test...");
        Rational a = new Rational(27, 54);
        assertEquals(a.abs(), 27);
        System.out.println("Test Completed.");
    }

    public void testRootForException() {
    	System.out.println("Executing test...");
    	Rational s = new Rational(-1, 2147483647);
        Rational sRoot = null;
        try {
            sRoot = s.root();
        } catch (IllegalArgumentToSquareRootException e) {
            //e.printStackTrace();
        }
        //Verifying if Exception is thrown
        assertTrue( sRoot.isLessThan( HALF.plus( Rational.getTolerance() ) ) 
                        && HALF.minus( Rational.getTolerance() ).isLessThan( sRoot ) );
        System.out.println("Test Completed.");
    }

    public void testSetToleranceCover(){
    	System.out.println("Executing test...");
        Rational a = new Rational(1, 2);
        Rational.setTolerance(a);
        //Verify if the Tolerance calculated and the one generated are same
        assertEquals(new Rational(1,2), Rational.getTolerance());
        System.out.println("Test Completed.");
    }

    public void testEqualsWithNull(){
    	System.out.println("Executing test...");
    	//Verifying if the Rational value returned is not NULL
    	//Trying to do this with valid input to Rational
        assertFalse(new Rational(2,3).equals(null));
        System.out.println("Test Completed.");
    }

    public void testPlusForLcm(){
    	System.out.println("Executing test...");
        Rational a = new Rational( 1,  1);	
        Rational b = new Rational(-1, -1);	
        Rational c = new Rational( 1, -1);	
        Rational d = new Rational(-1,  1);	
        
        //Output expected for the value of Plus is 2
        assertEquals(2, a.plus(b));
        assertEquals(2, d.plus(c));
        System.out.println("Test Completed.");
    }
    /*** Newly Added Test Cases - END ***/


    public static void main(String args[]) {
        String[] testCaseName = 
            { RationalTest.class.getName() };
        // junit.swingui.TestRunner.main(testCaseName);
        junit.textui.TestRunner.main(testCaseName);
    }
}
