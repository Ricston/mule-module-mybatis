package org.mule.module.mybatis;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.NullPayload;
import org.mybatis.domain.Person;


public class MyBatisMapperOnlyIntegrationTest extends FunctionalTestCase{
	
	public MyBatisMapperOnlyIntegrationTest(){
		super();
		this.setDisposeContextPerClass(true);
	}

	@Override
	protected String getConfigResources() {
		return "mybatis-mapper-only-integration-test.xml";
	}
	
	@Test
	public void insertTest() throws Exception
	{
		Person person = MyBatisTestUtils.createTestPerson(true);
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", 1, person);
		Assert.assertNotNull(person.getId());
	}
	
	
	@Test
	public void selectOnePerson() throws Exception
	{
		Person person = MyBatisTestUtils.createTestPerson(true);
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", 1, person);
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testSelectOne", person, person.getId());
	}
	
	@Test
	public void selectAllPersonsList() throws Exception
	{
		Person person = MyBatisTestUtils.createTestPerson(true);
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", 1, person);
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", 1, person);
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", 1, person);
		MuleMessage response = MyBatisTestUtils.runFlowWithPayload(muleContext, "testSelectList", null);
		Assert.assertTrue(response.getPayload() instanceof List);
		@SuppressWarnings("unchecked")
		List<Person> responseList = (List<Person>) response.getPayload();
		Assert.assertTrue(responseList.size() >= 3);
	}
	
	@Test
	public void selectAllPersonsMap() throws Exception
	{
		Person person = MyBatisTestUtils.createTestPerson(true);
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", 1, person);
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", 1, person);
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", 1, person);
		MuleMessage response = MyBatisTestUtils.runFlowWithPayload(muleContext, "testSelectMap", null);
		Assert.assertTrue(response.getPayload() instanceof Map);
		@SuppressWarnings("unchecked")
		Map<Integer, Person> responseMap = (Map<Integer, Person>) response.getPayload();
		Assert.assertTrue(responseMap.size() >= 3);
	}
	
	@Test
	public void updatePerson() throws Exception
	{
		Person person = MyBatisTestUtils.createTestPerson(true);
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", 1, person);
		person.setName(person.getName() + ".1");
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testUpdate", 1, person);
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testSelectOne", person, person.getId());
	}
	
	@Test
	public void deletePerson() throws Exception
	{
		Person person = MyBatisTestUtils.createTestPerson(true);
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testInsert", 1, person);
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testSelectOne", person, person.getId());
		
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testDelete", 1, person.getId());
		MyBatisTestUtils.runFlowWithPayloadAndExpect(muleContext, "testSelectOne", NullPayload.getInstance(), person.getId());
	}
	

}
