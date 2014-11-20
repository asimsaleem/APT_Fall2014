//package MockObjects;

import junit.framework.TestCase;

import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;

public class Test_TestingLabConverterServlet extends TestCase {

	//Test case: Don't pass any parameter to the Servlet
	//Verify if it returns an Error message indicating the need to enter a temperature
	public void test_null_parameter() throws Exception {
		System.out.println("Testing for NULL or No parameter being passed....");
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
		System.out.println("Passed");
	}
  
	public void test_invalid_number() throws Exception {
		
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
		System.out.println("Passed");
	}
	
	public void test_valid_temperature() throws Exception {
		
		System.out.println("Testing for the Valid Temperature response scenario");
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
		System.out.println("Passed");
	}
	  
}
