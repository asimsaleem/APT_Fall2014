
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class TemperatureConversionFF {
   public static void main(String[] args) throws Exception {
        // The Firefox driver supports javascript 
        WebDriver driver = new FirefoxDriver();
        
        // Go to the TESTING LAB Login HTML
        driver.get("http://apt-public.appspot.com/testing-lab-login.html");
        
        
        //Req: There are 3 valid users, andy, bob and charley, whose passwords are apple, bathtub and china. 
        //Test Case 1:
        // Test Case for User "andy" with Password "apple"
        System.out.println("Test Case: Login with UserId/Password: andy/apple");
        boolean isTestForAndySuccessful = isUserIdPwdComboSuccessful(driver, "andy", "apple");
        if(isTestForAndySuccessful){
        	System.out.println("UserId/Pwd test with User \"andy\" was SUCCESSFUL as EXPECTED");
        }else if(!isTestForAndySuccessful){
        	System.out.println("UserId/Pwd test with User \"andy\" was a FAILURE");
        }

        //Need to reset the test by going back
        driver.navigate().back();

        //Test Case 2:
        // Test Case for User "bob" with Password "bathtub"
        System.out.println("Test Case: Login with UserId/Password: bob/bathtub");
        boolean isTestForBobSuccessful = isUserIdPwdComboSuccessful(driver, "bob", "bathtub");
        if(isTestForBobSuccessful){
        	System.out.println("UserId/Pwd test with User \"bob\" was SUCCESSFUL as EXPECTED");
        }else if(!isTestForBobSuccessful){
        	System.out.println("UserId/Pwd test with User \"bob\" was a FAILURE");
        }      
        
        //Need to reset the test by going back
        driver.navigate().back();

        //Test Case 3:
        //Test Case for User "charley" with Password "china"
        System.out.println("Test Case: Login with UserId/Password: charley/china");
        boolean isTestForCharleySuccessful = isUserIdPwdComboSuccessful(driver, "charley", "china");
        if(isTestForCharleySuccessful){
        	System.out.println("UserId/Pwd test with User \"charley\" was SUCCESSFUL as EXPECTED");
        }else if(!isTestForCharleySuccessful){
        	System.out.println("UserId/Pwd test with User \"charley\" was a FAILURE");
        }        
       
      	System.out.println("****************************************");
        
        //Need to reset the test by going back
        driver.navigate().back();

        System.out.println("Sleeping for 10 Seconds to ensure that the User can login again to test other scenarios..");
        Thread.sleep(10000);
          
        //Req: User names are case-insensitive, but passwords are case-sensitive. Leading and trailing white space should be immaterial for both.
        //Test Case 4:
        //Test case with MixedCase UserId and lowercase Password 
        System.out.println("Test Case: Login with UserId/Pwd: AnDy/apple");
        boolean isTestForMixedCaseUserIdSuccessful = isUserIdPwdComboSuccessful(driver, "AnDy", "apple");
        if(isTestForMixedCaseUserIdSuccessful){
        	System.out.println("UserId/Pwd test with Mixed Case User \"AnDy\" was SUCCESSFUL as EXPECTED");
        }else if(!isTestForMixedCaseUserIdSuccessful){
        	System.out.println("UserId/Pwd test with Mixed Case User \"AnDy\" was a FAILURE");
        }      
      
        //Need to reset the test by going back
        driver.navigate().back();

        //Test Case 5:
        //Test case with normal UserId and mixed case Password 
        System.out.println("Test Case: Login with UserId/Pwd: bob/BaThTub");
        boolean isTestForMixedCaseUserPwdSuccessful = isUserIdPwdComboSuccessful(driver, "bob", "BaThTub");
        if(isTestForMixedCaseUserPwdSuccessful){
        	System.out.println("UserId/Pwd test with Mixed Case User Pwd \"BaThTub\" was SUCCESSFUL");
        }else if(!isTestForMixedCaseUserPwdSuccessful){
        	System.out.println("UserId/Pwd test with Mixed Case User Pwd \"BaThTub\" was a FAILURE as EXPECTED");
        }      
 
        //Need to reset the test by going back
        driver.navigate().back();

        //Test Case 6:
        //Test case with normal UserId and Password, but with leading and trailing white spaces for both
        System.out.println("Test Case: Login with UserId/Pwd: bob/BaThTub");
        boolean isTestForSpacesLTSuccessful = isUserIdPwdComboSuccessful(driver, "  charley  ", "  china  ");
        if(isTestForSpacesLTSuccessful){
        	System.out.println("UserId/Pwd test with Trailing/Leading Spaces Case was SUCCESSFUL as EXPECTED");
        }else if(!isTestForSpacesLTSuccessful){
        	System.out.println("UserId/Pwd test with Trailing/Leading Spaces was a FAILURE");
        } 
        
    	System.out.println("****************************************");
        System.out.println("Sleeping for 10 Seconds to ensure that the User can login again to test other scenarios..");
        Thread.sleep(10000);

        //Need to reset the test by going back
        driver.navigate().back();

        //Three failed logins for a user in ten seconds should lead to a 1 minute lockout.
        
    	//Test Case 7:
        System.out.println("Test Case: Login with UserId/Pwd: andy/app");
        boolean isTestForLockoutSuccessful1 = isUserIdPwdComboSuccessful(driver, "andy  ", "app");
        if(isTestForLockoutSuccessful1){
        	System.out.println("UserId/Pwd test with Wrong Password Case was SUCCESSFUL");
        }else if(!isTestForLockoutSuccessful1){
        	System.out.println("UserId/Pwd test with Wrong Password was a FAILURE as EXPECTED. Attempt 1. Trying again...");
        } 
        
        //Need to reset the test by going back
        driver.navigate().back();

        boolean isTestForLockoutSuccessful2 = isUserIdPwdComboSuccessful(driver, "andy  ", "app");
        if(isTestForLockoutSuccessful2){
        	System.out.println("UserId/Pwd test with Wrong Password Case was SUCCESSFUL");
        }else if(!isTestForLockoutSuccessful2){
        	System.out.println("UserId/Pwd test with Wrong Password was a FAILURE as EXPECTED. Attempt 2. Trying again...");
        } 
    	
        //Need to reset the test by going back
        driver.navigate().back();

        boolean isTestForLockoutSuccessful3 = isUserIdPwdComboSuccessful(driver, "andy  ", "app");
        if(isTestForLockoutSuccessful3){
        	System.out.println("UserId/Pwd test with Wrong Password Case was SUCCESSFUL");
        }else if(!isTestForLockoutSuccessful3){
        	System.out.println("UserId/Pwd test with Wrong Password was a FAILURE as EXPECTED. Attempt 3. Trying again...");
        } 
        
        //Need to reset the test by going back
        driver.navigate().back();

        
        boolean isTestForLockoutSuccessful4 = isUserIdPwdComboSuccessful(driver, "andy", "apple");
        if(isTestForLockoutSuccessful4){
        	System.out.println("UserId/Pwd test with Wrong Password Case was SUCCESSFUL");
        }else if(!isTestForLockoutSuccessful4){
        	System.out.println("UserId/Pwd test with Wrong Password was LOCKED OUT as EXPECTED @ Attempt 4");
        } 
        
        //Need to reset the test by going back
        driver.navigate().back();
        
        System.out.println("Putting the process to sleep for 1 minute to attempt logging in again with proper credentials");
        Thread.sleep(60 * 1000);
        System.out.println("Thread is awake. Trying to login again with proper credentials");
   
        boolean isTestForLockoutSuccessful5 = isUserIdPwdComboSuccessful(driver, "andy", "apple");
        if(isTestForLockoutSuccessful5){
        	System.out.println("UserId/Pwd test with proper User Id/Password Case was SUCCESSFUL as EXPECTED");
        }else if(!isTestForLockoutSuccessful5){
        	System.out.println("UserId/Pwd test with proper User Id/Password Case was FAILURE");
        } 
    	System.out.println("****************************************");
   	
        //Need to reset the test by going back
        driver.navigate().back();
 
        System.out.println("Sleeping for 10 Seconds to ensure that the User can login again to test other scenarios..");
        Thread.sleep(10000);

    	//Temperature results should be 2 places of precision for temperatures from 0 to 212 degrees Farenheit, inclusive, and 1 place of precision otherwise.
 
        //Test Case 8:
        System.out.println("Test Case: Login with UserId/Pwd: andy/apple");
        boolean isTestForLoginSuccessful = isUserIdPwdComboSuccessful(driver, "andy", "apple");
        if(isTestForLoginSuccessful){
        	System.out.println("UserId/Pwd test with Wrong Password Case was SUCCESSFUL as EXPECED");
        	
        	//Get a Handler to the Input for Temperature
        	WebElement fahrenheitTempElement =  driver.findElement(By.name("farenheitTemperature"));
        	fahrenheitTempElement.clear();
        	fahrenheitTempElement.sendKeys("150");
        	fahrenheitTempElement.submit();
        	
        	//h2 tag contains the data that needs to be cross-checked
        	String temperatureValue = driver.findElement(By.tagName("h2")).getText();
        	System.out.println("Temperature value is:" + temperatureValue);
        	
        	String[] tempList = temperatureValue.split("=");
           	System.out.println("Farenheit Temperature value is:" + tempList[1]);

        	String[] farenheitValueList = tempList[1].split("Celsius");
        	System.out.println("Farenheit Value is: " + farenheitValueList[0]);
        	
        	float verifyPrecisionOfFloat = Float.parseFloat(farenheitValueList[0]);
        	System.out.println("Value of Precision for Float :" + verifyPrecisionOfFloat);
        	
        	if(verifyPrecisionOfFloat > 65 && verifyPrecisionOfFloat < 66 ){
        		System.out.println("Two Digit Precision Test was SUCCESSFUL as EXPECTED");
            }else {
            	System.out.println("Two Digit Precision Test was FAILURE");
            } 
        }else if(!isTestForLoginSuccessful){
        	System.out.println("UserId/Pwd test with Wrong Password was a FAILURE");
        }    
 
        //Need to reset the test by going back
        driver.navigate().back();
        //Need to reset the test by going back again
        driver.navigate().back();

        System.out.println("Test Case: Login with UserId/Pwd: bob/bathtub");
        isTestForLoginSuccessful = isUserIdPwdComboSuccessful(driver, "bob", "bathtub");
        if(isTestForLoginSuccessful){
        	System.out.println("UserId/Pwd Login was SUCCESSFUL");
        	
        	//Get a Handler to the Input for Temperature
        	WebElement fahrenheitTempElement =  driver.findElement(By.name("farenheitTemperature"));
        	fahrenheitTempElement.clear();
        	fahrenheitTempElement.sendKeys("250");
        	fahrenheitTempElement.submit();
        	
        	//h2 tag contains the data that needs to be cross-checked
        	String temperatureValue = driver.findElement(By.tagName("h2")).getText();
        	System.out.println("Temperature value is:" + temperatureValue);
        	
        	String[] tempList = temperatureValue.split("=");
           	System.out.println("Farenheit Temperature value is:" + tempList[1]);

        	String[] farenheitValueList = tempList[1].split("Celsius");
        	System.out.println("Farenheit Value is: " + farenheitValueList[0]);
        	
        	float verifyPrecisionOfFloat = Float.parseFloat(farenheitValueList[0]);
        	System.out.println("Value of Precision for Float :" + verifyPrecisionOfFloat);
        	
        	if(verifyPrecisionOfFloat > 121 && verifyPrecisionOfFloat < 122 ){
        		System.out.println("One Digit Precision Test was SUCCESSFUL as EXPECTED");
            }else {
            	System.out.println("One Digit Precision Test was FAILURE");
            } 
        }else if(!isTestForLoginSuccessful){
        	System.out.println("UserId/Pwd test with Wrong Password was a FAILURE");
        } 
    
        System.out.println("****************************************");
        System.out.println("Sleeping for 10 Seconds to ensure that the User can login again to test other scenarios..");
        Thread.sleep(10000);

        //Need to reset the test by going back
        driver.navigate().back();
        
        //Need to reset the test by going back again
        driver.navigate().back();
        
        //Temperature inputs are floating point numbers in decimal notation (i.e., 97 or -3.14, but not 9.73E2)
        
        //Test Case 9:
        System.out.println("Test Case: Login with UserId/Pwd: charley/china");
        isTestForLoginSuccessful = isUserIdPwdComboSuccessful(driver, "charley", "china");
        if(isTestForLoginSuccessful){
        	System.out.println("UserId/Pwd Login was SUCCESSFUL. ");
        	
        	//Get a Handler to the Input for Temperature
        	WebElement fahrenheitTempElement =  driver.findElement(By.name("farenheitTemperature"));
        	fahrenheitTempElement.clear();
        	fahrenheitTempElement.sendKeys("150");
        	fahrenheitTempElement.submit();
        	
        	//h2 tag contains the data that needs to be cross-checked
        	String temperatureValue = driver.findElement(By.tagName("h2")).getText();
        	System.out.println("Temperature value is:" + temperatureValue);
        	
        	String[] tempList = temperatureValue.split("=");
           	System.out.println("Farenheit Temperature value is:" + tempList[1]);

        	String[] farenheitValueList = tempList[1].split("Celsius");
        	System.out.println("Farenheit Value is: " + farenheitValueList[0]);
        	
        	if(!farenheitValueList[0].contains("e")){
        		System.out.println("Floating Number Check for Decimal Notation was SUCCESSFUL as EXPECTED");
            }else {
            	System.out.println("Floating Number Check for Decimal Notation was FAILURE");
            } 
        }else if(!isTestForLoginSuccessful){
        	System.out.println("UserId/Pwd used to Login was a FAILURE");
        }    
        
        System.out.println("****************************************");
        
        //Need to reset the test by going back
        driver.navigate().back();
        
        //Need to reset the test by going back again
        driver.navigate().back();
        
        //Temperature inputs that are not valid should return Got a NumberFormatException on (input string)
        
        //Test Case 10:
        System.out.println("Test Case: Login with UserId/Pwd: andy/apple");
        isTestForLoginSuccessful = isUserIdPwdComboSuccessful(driver, "andy", "apple");
        if(isTestForLoginSuccessful){
        	System.out.println("UserId/Pwd Login was SUCCESSFUL. ");
        	
        	//Get a Handler to the Input for Temperature
        	WebElement fahrenheitTempElement =  driver.findElement(By.name("farenheitTemperature"));
        	fahrenheitTempElement.clear();
        	fahrenheitTempElement.sendKeys("abc");
        	fahrenheitTempElement.submit();
        	
        	//h2 tag contains the data that needs to be cross-checked
        	String response = driver.findElement(By.tagName("h2")).getText();
        	System.out.println("Response is:" + response);
        	
        	if(response.contains("NumberFormatException")){
           		System.out.println("NumberFormatException Test for Invalid Input was SUCCESSFUL as EXPECTED");
            }else {
            	System.out.println("NumberFormatException Test for Invalid Input was FAILURE");
            } 
 
        }else if(!isTestForLoginSuccessful){
        	System.out.println("UserId/Pwd used to Login was a FAILURE");
        }    
        
       	System.out.println("****************************************");
        //The temperature parameter should be passed in as farenheitTemperature=-40 in the URL; the parameter “farenheitTemperature” should be case insensitive.
        driver.get("http://apt-public.appspot.com/testing-lab-conversion?FarenheitTemperature=-40");
         if(driver.findElement(By.tagName("h2")).getText().contains("Need to")){
        	System.out.println("URL Parameter Test was FAILURE as EXPECTED");
        }else{
        	System.out.println("URL Parameter Test was SUCCESS");
        }
    	System.out.println("****************************************");

        driver.quit();
    }
   
   
   public static boolean isUserIdPwdComboSuccessful(WebDriver driver, String userId, String userPwd){
	   
	   System.out.println("Processing Test Case for UserId/Pwd Combo: " + userId + "/" + userPwd);
	   
	   boolean isLoginSuccessful = false;
       //Get the Handle to the User Id field in the screen and enter the User Name as "andy"
       WebElement name_userId = driver.findElement(By.name("userId"));
       //There is a place holder in the screen for User Id. Need to clear that else that gets passed around
       name_userId.clear();
       //name_userId.sendKeys("andy");
       name_userId.sendKeys(userId);
       //Get the Handle to the Password field in the screen and enter the User Password as "apple"
       WebElement name_userPwd = driver.findElement(By.name("userPassword"));
       //Clear up Password too because the default value seems to be getting appended to apple and corrupting it
       name_userPwd.clear();
       //name_userPwd.sendKeys("apple");
       name_userPwd.sendKeys(userPwd);
       //Submit now
       name_userPwd.submit();
       //Verify the Outcome now
       if(driver.getTitle().equalsIgnoreCase("Online temperature conversion calculator")){
    	System.out.println("Title of this Page is: " + driver.getTitle());
       	System.out.println("UserId/Pwd for " + userId + "/" + userPwd + " Passed Successfully");
       	return true;
       }else if(driver.getTitle().equalsIgnoreCase("Bad Login")){
    	System.out.println("Title of this Page is: " + driver.getTitle());
       	System.out.println("UserId/Pwd for " + userId + "/" + userPwd + " Failed Miserably");
       	return false;
       }else{
    	   System.out.println("Title of this Page is: " + driver.getTitle());
    	   System.out.println("UserId/Pwd for " + userId + "/" + userPwd + " Failed Miserably");
    	  
       }

	   
	   return isLoginSuccessful;
   }
}
