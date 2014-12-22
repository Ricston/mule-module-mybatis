/**
 * (c) 2003-2014 Ricston, Ltd. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.module.mybatis;
import org.junit.Assert;
import org.junit.Test;
import org.mule.component.ComponentException;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.NullPayload;
import org.mybatis.domain.Address;
import org.mybatis.domain.Person;


public class MyBatisIntegrationTest extends FunctionalTestCase{
	
	public MyBatisIntegrationTest(){
		super();
		this.setDisposeContextPerClass(true);
	}

	
	@Override
	protected String getConfigResources() {
		return "mybatis-common-test.xml, mybatis-integration-test.xml";
	}
	
	@Test
	public void insertTest() throws Exception
	{
		Person person = MyBatisTestUtils.createTestPerson(true);
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", NullPayload.getInstance(), person);
		Assert.assertNotNull(person.getId());
	}
	
	@Test
	public void selectPerson() throws Exception
	{
		Person person = MyBatisTestUtils.createTestPerson(true);
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", NullPayload.getInstance(), person);
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testSelect", person, person.getId());
	}
	
	@Test
	public void transactionSuccess() throws Exception{
		Person person = MyBatisTestUtils.createTestPerson(false);
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testTransactionSuccess", NullPayload.getInstance(), person);
		Assert.assertNotNull(person.getId());
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testSelect", person, person.getId());
		
		for (Address address : person.getAddresses()){
			Assert.assertNotNull(address.getId());
		}
	}
	
	@Test
	public void transactionSuccessWithExceptionStrategy() throws Exception{
		Person person = MyBatisTestUtils.createTestPerson(false);
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testTransactionSuccessWithExceptionStrategy", NullPayload.getInstance(), person);
		Assert.assertNotNull(person.getId());
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testSelect", person, person.getId());
		
		for (Address address : person.getAddresses()){
			Assert.assertNotNull(address.getId());
		}
	}
	
	@Test
	public void transactionRollback() throws Exception{
		Person person = MyBatisTestUtils.createTestPerson(false);
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testTransactionRollback", NullPayload.getInstance(), person);
		Assert.assertNotNull(person.getId());
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testSelect", NullPayload.getInstance(), person.getId());
		
		for (Address address : person.getAddresses()){
			Assert.assertNotNull(address.getId());
		}
	}
	
	@Test
	public void transactionRollbackWithExceptionStrategy() throws Exception{
		Person person = MyBatisTestUtils.createTestPerson(false);
		
		try
		{
			MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testTransactionRollbackWithExceptionStrategy", NullPayload.getInstance(), person);
		}
		catch(ComponentException e){
			//exception is expected, do not fail
			System.out.println(e.getClass());
		}
		Assert.assertNotNull(person.getId());
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testSelect", NullPayload.getInstance(), person.getId());
		
		for (Address address : person.getAddresses()){
			Assert.assertNotNull(address.getId());
		}
	}
}
