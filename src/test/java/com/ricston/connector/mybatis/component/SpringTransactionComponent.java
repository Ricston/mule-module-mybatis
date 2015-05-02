/**
 * (c) 2003-2014 Ricston, Ltd. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.ricston.connector.mybatis.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.ricston.connector.mybatis.domain.Address;
import com.ricston.connector.mybatis.domain.Person;
import com.ricston.connector.mybatis.sql.AddressMapper;
import com.ricston.connector.mybatis.sql.PersonMapper;

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


