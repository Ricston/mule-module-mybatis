package org.mule.module.mybatis;
import org.junit.Assert;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.NullPayload;
import org.mybatis.domain.Person;


public class MyBatisSpringIntegrationTest extends FunctionalTestCase{
	
	public MyBatisSpringIntegrationTest(){
		super();
		this.setDisposeContextPerClass(true);
	}

	
	@Override
	protected String getConfigResources() {
		return "mybatis-spring-integration-test.xml";
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
	
	
}
