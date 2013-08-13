package org.mybatis.sql;

import org.mybatis.domain.Person;

public interface PersonMapper {

	public Person selectPerson(Integer id);
	
	public void insertPerson(Person person);
	
	public void updatePerson(Person person);
	
	public void deletePerson(Integer id);
}
