/**
 * (c) 2003-2014 Ricston, Ltd. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.module.mybatis.component;

import org.mybatis.domain.Address;
import org.mybatis.domain.Person;
import org.mybatis.sql.AddressMapper;
import org.mybatis.sql.PersonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SpringTransactionComponent
{
    @Autowired
    private PersonMapper personMapper;
    
    @Autowired
    private AddressMapper addressMapper;
    
    public static class PersistanceException extends Exception
    {
        private static final long serialVersionUID = -6510435565952353536L;

        public PersistanceException(String message)
        {
            super(message);
        }
    }
    
    @Transactional(rollbackFor=PersistanceException.class)
    public Person persistPerson(Person person) throws PersistanceException
    {
        personMapper.insertPerson(person);
        
        for(Address address : person.getAddresses()){
            address.setPersonId(person.getId());
            addressMapper.insertAddress(address);
        }
        
        throw new PersistanceException("test");
    }

    public PersonMapper getPersonMapper()
    {
        return personMapper;
    }

    public void setPersonMapper(PersonMapper personMapper)
    {
        this.personMapper = personMapper;
    }

}


