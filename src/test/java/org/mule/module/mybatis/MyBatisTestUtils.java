package org.mule.module.mybatis;
import java.util.ArrayList;

import org.junit.Assert;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.construct.Flow;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mybatis.domain.Address;
import org.mybatis.domain.Person;


public class MyBatisTestUtils{
	
	private static final String NAME = "name";
	private static final String SURNAME = "surname";
	private static final int AGE = 27;
	
	private static final String ADDRESS1 = "address1";
	private static final String ADDRESS2 = "address2";
	
	public static Person createTestPerson(boolean withEmptyAddress){
		Person person = new Person();
		person.setName(NAME);
		person.setSurname(SURNAME);
		person.setAge(AGE);
		
		ArrayList<Address> addresses = new ArrayList<Address>();
		
		if (!withEmptyAddress){
			Address address1 = new Address();
			address1.setAddress(ADDRESS1);
			Address address2 = new Address();
			address2.setAddress(ADDRESS2);
			
			addresses.add(address1);
			addresses.add(address2);
		}
		
		person.setAddresses(addresses);
		
		return person;
	}
	
	/**
    * Run the flow specified by name using the specified payload 
    *
    * @param flowName The name of the flow to run
    * @param payload The payload of the input event
    */
    public static <U> MuleMessage runFlowWithPayload(MuleContext context, String flowName, U payload) throws Exception
    {
        Flow flow = lookupFlowConstruct(context, flowName);
        MuleEvent event = FunctionalTestCase.getTestEvent(payload);
        MuleEvent responseEvent = flow.process(event);

        return responseEvent.getMessage();
    }
	
    /**
    * Run the flow specified by name using the specified payload and assert
    * equality on the expected output
    *
    * @param flowName The name of the flow to run
    * @param expect The expected output
    * @param payload The payload of the input event
    */
    public static <T, U> void runFlowWithPayloadAndExpect(MuleContext context, String flowName, T expect, U payload) throws Exception
    {
        MuleMessage responseMessage = runFlowWithPayload(context, flowName, payload);
        Assert.assertEquals(expect, responseMessage.getPayload());
    }

    /**
     * Retrieve a flow by name from the registry
     *
     * @param name Name of the flow to retrieve
     */
    public static Flow lookupFlowConstruct(MuleContext context, String name)
    {
        return (Flow) context.getRegistry().lookupFlowConstruct(name);
    }

}
