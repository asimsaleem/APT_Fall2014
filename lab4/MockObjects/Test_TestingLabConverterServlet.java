//package MockObjects;

import junit.framework.TestCase;

import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;

public class Test_TestingLabConverterServlet extends TestCase {

	//Test case: Don't pass any parameter to the Servlet
	//Verify if it returns an Error message indicating the need to enter a temperature
	public void test_null_parameter() throws Exception {
		System.out.println("Testing for NULL or No parameter being passed...");
		System.out.println("Setting up the Mock HttpServlet Request and Response objects");
		TestingLabConverterServlet s = new TestingLabConverterServlet();
		MockHttpServletRequest request = new MockHttpServletRequest();
	    MockHttpServletResponse response = new MockHttpServletResponse();
	    System.out.println("Setting the expected Content type in return when the code is executed to HTML");
		response.setExpectedContentType("text/html");
		System.out.println("Invoking the GET operation...");
		s.doGet(request,response);
		response.verify();
		System.out.println("Verifying if the Response returned is as expected..");
		assertEquals("<html><head><title>No Temperature</title>"
					+ "</head><body><h2>Need to enter a temperature!"
					+ "</h2></body></html>\n",
					response.getOutputStreamContents());
		System.out.println("Test Completed");
	}
  
	//Temperature inputs that are not valid should return Got a NumberFormatException on (input string)
	public void test_invalid_temperature_value() throws Exception {
		
		System.out.println("Testing for Invalid Number in order to verify if a NumberFormatException is thrown....");
		TestingLabConverterServlet s = new TestingLabConverterServlet();
		System.out.println("Setting up the Mock HttpServlet Request and Response objects");
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		System.out.println("Setting up the parameter value for farenheitTemperature to a non-numerical value..");
		request.setupAddParameter("farenheitTemperature", "abcd");
	    System.out.println("Setting the expected Content type in return when the code is executed to HTML");
		response.setExpectedContentType("text/html");
		System.out.println("Invoking the GET operation...");
		s.doGet(request,response);
		response.verify();
		System.out.println("Verifying if the Response returned is as expected..");
		assertEquals("<html><head><title>Bad Temperature</title>"
					+ "</head><body><h2>Need to enter a valid temperature!"
				    + "Got a NumberFormatException on " 
					+ "abcd" 
					+ "</h2></body></html>\n",
					response.getOutputStreamContents());
		System.out.println("Test Completed");
	}
	
	public void test_valid_temperature_value() throws Exception {
		
		System.out.println("Testing for the Valid Temperature response scenario...");
		System.out.println("Setting up the Mock HttpServlet Request and Response objects");
		TestingLabConverterServlet s = new TestingLabConverterServlet();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		System.out.println("Setting up the parameter value for farenheitTemperature to a numerical value..");
		request.setupAddParameter("farenheitTemperature", "50");
	    System.out.println("Setting the expected Content type in return when the code is executed to HTML");
		response.setExpectedContentType("text/html");
		System.out.println("Invoking the GET operation...");
		s.doGet(request,response);
		response.verify();
		System.out.println("Verifying if the Response returned is as expected..");
		assertEquals("<html><head><title>Temperature Converter Result</title>"
					+ "</head><body><h2>50 Farenheit = 10 Celsius </h2>\n" 
					+ "<p><h3>The temperature in Austin is 451 degrees Farenheit</h3>\n"
					+ "</body></html>\n",
					response.getOutputStreamContents());
		System.out.println("Test Completed");
	}
	
	//Temperature results should be 2 places of precision for temperatures from 0 to 212 degrees Fahrenheit, inclusive, and 1 place of precision otherwise.
	public void test_two_digit_precision() throws Exception {
		
		System.out.println("Testing for the Two Digit Temperature Precision scenario");
		System.out.println("Setting up the Mock HttpServlet Request and Response objects");
		TestingLabConverterServlet s = new TestingLabConverterServlet();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		System.out.println("Setting up the parameter value for farenheitTemperature to a numerical value..");
		request.setupAddParameter("farenheitTemperature", "200");
	    System.out.println("Setting the expected Content type in return when the code is executed to HTML");
		response.setExpectedContentType("text/html");
		System.out.println("Invoking the GET operation...");
		s.doGet(request,response);
		response.verify();
		System.out.println("Response is" + response.getOutputStreamContents());
		System.out.println("Verifying if the Response returned is as expected with a Two Digit Precision..");
		assertTrue(response.getOutputStreamContents().contains("93.33 Celsius"));
		System.out.println("Two Digit Precision Test Test Completed");
	}
	
	
	//Temperature results should be 2 places of precision for temperatures from 0 to 212 degrees Farenheit, inclusive, and 1 place of precision otherwise.
	public void test_one_digit_precision() throws Exception {
		
		System.out.println("Testing for the One Digit Temperature Precision scenario...");
		System.out.println("Setting up the Mock HttpServlet Request and Response objects");
		TestingLabConverterServlet s = new TestingLabConverterServlet();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		System.out.println("Setting up the parameter value for farenheitTemperature to a numerical value..");
		request.setupAddParameter("farenheitTemperature", "451");
	    System.out.println("Setting the expected Content type in return when the code is executed to HTML");
		response.setExpectedContentType("text/html");
		System.out.println("Invoking the GET operation...");
		s.doGet(request,response);
		response.verify();
		System.out.println("Verifying if the Response returned is as expected with a One digit precision..");
		System.out.println("Response is: "  + response.getOutputStreamContents());
		//Expecting the Output temperature to be 1 Digit precision for value outside the range (0 to 212) as per the Req
		//However the output comes as 451 Farenheit = 232.78 Celsius which is wrong
		assertTrue(response.getOutputStreamContents().contains("451 Farenheit = 232.7 Celsius"));
		System.out.println("Test Completed");
	}
	
	//Temperature inputs are floating point numbers in decimal notation (i.e., 97 or -3.14, but not 9.73E2)
	public void test_floating_point_numbers() throws Exception {
		
		System.out.println("Testing for the Floating Point Number with Decimal Notation scenario");
		System.out.println("Setting up the Mock HttpServlet Request and Response objects");
		TestingLabConverterServlet s = new TestingLabConverterServlet();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		System.out.println("Setting up the parameter value for farenheitTemperature to a numerical value..");
		request.setupAddParameter("farenheitTemperature", "150");
	    System.out.println("Setting the expected Content type in return when the code is executed to HTML");
		response.setExpectedContentType("text/html");
		System.out.println("Invoking the GET operation...");
		s.doGet(request,response);
		response.verify();
		System.out.println("Verifying if the Response returned is as expected with appropriate Deciman Notation..");
		System.out.println("Response is: "  + response.getOutputStreamContents());
		//Expecting the Output temperature to be with Decimal Notation
		//If the below condition is met, then it confirms that Decimal Notation is used
		assertTrue(response.getOutputStreamContents().contains("150 Farenheit = 65.56 Celsius"));
		System.out.println("Test Completed");
	}
	
	//the parameter “farenheitTemperature” should be case insensitive.
	public void test_case_insensitive_parameter() throws Exception {
		
		System.out.println("Testing for the Case Insensitive Parameter scenario");
		System.out.println("Setting up the Mock HttpServlet Request and Response objects");
		TestingLabConverterServlet s = new TestingLabConverterServlet();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		System.out.println("Setting up the parameter value for farenheitTemperature to be mixed Case..");
		request.setupAddParameter("FaReNhEitTeMperature", "150");
	    System.out.println("Setting the expected Content type in return when the code is executed to HTML");
		response.setExpectedContentType("text/html");
		System.out.println("Invoking the GET operation...");
		s.doGet(request,response);
		response.verify();
		System.out.println("Verifying if the Response returned is as expected..");
		System.out.println("Response is: "  + response.getOutputStreamContents());
		//Expecting the Output temperature given below even though the Parameter has been made CamelCase
		//As per the requirement, a Case Insensitive Parameter should be allowed and handled properly but this fails
		assertTrue(response.getOutputStreamContents().contains("150 Farenheit = 65.56 Celsius"));
		System.out.println("Test Completed");
	}
	  
}
