package org.mybatis.sql;

import java.util.List;

import org.mybatis.domain.Address;

public interface AddressMapper {

	public Address selectAddress(Integer id);
	
	public List<Address> selectAddressesByPersonId(Integer id);
	
	public void insertAddress(Address person);
	
	public void updateAddress(Address person);
}
