/**
 * (c) 2003-2014 Ricston, Ltd. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mybatis.sql;

import org.mybatis.domain.Person;

public interface PersonMapper {

	public Person selectPerson(Integer id);
	
	public void insertPerson(Person person);
	
	public void updatePerson(Person person);
	
	public void deletePerson(Integer id);
}
