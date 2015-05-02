/**
 * (c) 2003-2014 Ricston, Ltd. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.ricston.connector.mybatis.sql;

import java.util.List;

import com.ricston.connector.mybatis.domain.Address;

public interface AddressMapper {

	public Address selectAddress(Integer id);
	
	public List<Address> selectAddressesByPersonId(Integer id);
	
	public void insertAddress(Address person);
	
	public void updateAddress(Address person);
}
