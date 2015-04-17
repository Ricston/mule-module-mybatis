/**
 * (c) 2003-2014 Ricston, Ltd. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.module.mybatis;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mule.api.MuleContext;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.param.Optional;
import org.mule.api.annotations.param.Payload;
import org.mule.api.lifecycle.LifecycleException;
import org.mule.config.i18n.MessageFactory;

/**
 * 
 * MyBatis module for Mule
 * 
 * @author Alan Cassar
 * 
 */
@Connector(name="mybatis", friendlyName="MyBatis", schemaVersion="1.0")
public class MyBatisConnector {
	
	protected Log logger = LogFactory.getLog(getClass());
	
	@Inject
	private MuleContext muleContext;

	/**
	 * Reference to MyBatis configuration file
	 */
	@Configurable
	@Optional
	private String configFile;
	
	/**
	 * SqlSessionFactory to use to create sessions
	 */
	@Configurable
	@Optional
	private SqlSessionFactory sqlSessionFactory;
	
	protected ThreadLocal<SqlSession> threadLocalSession = new ThreadLocal<SqlSession>();
	
	/**
	 * Make sure that on start up, at least one of either configFile or sqlSessionFactory is set
	 * 
	 * @throws LifecycleException
	 */
	@Start
	public void validateConfiguration() throws LifecycleException
	{
		if ((configFile == null) && (sqlSessionFactory == null))
		{
			throw new LifecycleException(MessageFactory.createStaticMessage("One of properties 'configFile' or 'sqlSessionFactory' must be set"), this);
		}
	}
	
	/**
	 * Create an SqlSession by first initialising the sqlSessionFactory from the config
	 * file if it is not initialised yet, or not provided.
	 * 
	 * First this method looks for an already open sqlSession by this thread. If there is
	 * one, just return it, otherwise create a new one using the autoCommit setting specified.
	 * 
	 * @param autoCommit
	 * @return the current or a new sqlSession
	 * @throws IOException
	 */
	protected SqlSession createSqlSession(boolean autoCommit) throws IOException
	{
		//create the sql session factory from the configuration file, if not set
		if (sqlSessionFactory == null){
			Reader config = Resources.getResourceAsReader(configFile);
			SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
			sqlSessionFactory = builder.build(config);
		}
		
		SqlSession sqlSession = threadLocalSession.get();
			
		//if we don't have a session, create one with auto commit set to true
		if (sqlSession == null){
			sqlSession = sqlSessionFactory.openSession(autoCommit);
		}
		
		return sqlSession;
	}
	
	/**
	 * Shortcut method to create an sqlSession with autoCommit set
	 * 
	 * @return sqlSession with autoCommit set to true
	 * @throws IOException
	 */
	protected SqlSession createSqlSession() throws IOException
	{
		return this.createSqlSession(true);
	}
	
	/**
	 * Shortcut method to create a new sqlSession with autoCommit set to false.
	 * 
	 * @return sqlSession with autoCommit set to false
	 * @throws IOException
	 */
	protected SqlSession createSqlSessionForTransaction() throws IOException
	{
		return this.createSqlSession(false);
	}
	
	/**
	 * Closes the sqlSession, only if it is not thread local. This means
	 * that the session will be closed if there is no transaction context.
	 * 
	 * @param sqlSession
	 */
	protected void closeSqlSession(SqlSession sqlSession)
	{
		//only close the session if there is no transaction in progress
		if (threadLocalSession.get() == null){
			sqlSession.close();
		}
	}
	
	
	/**
	 * Starts a new transaction by initialising a session, and setting it as thread local.
	 * 
	 * {@sample.xml ../../../doc/mule-mybatis-module.xml.sample mybatis:begin-transaction}
	 * 
	 * @throws IOException Io Error
	 */
	@Processor
	public void beginTransaction() throws IOException{
		SqlSession sqlSession = createSqlSessionForTransaction();
		threadLocalSession.set(sqlSession);
	}
	
	/**
	 * Commits and closes the current transaction
	 * 
	 * {@sample.xml ../../../doc/mule-mybatis-module.xml.sample mybatis:commit-transaction}
	 * 
	 * @throws IOException Io Error
	 */
	@Processor
	public void commitTransaction() throws IOException{
		SqlSession sqlSession = threadLocalSession.get();
		sqlSession.commit();
		sqlSession.close();
		threadLocalSession.set(null);
	}
	
	/**
	 * Rollbacks and closes the current transaction
	 * 
	 * {@sample.xml ../../../doc/mule-mybatis-module.xml.sample mybatis:rollback-transaction}
	 * 
	 * @throws IOException Io Error
	 */
	@Processor
	public void rollbackTransaction() throws IOException{
		SqlSession sqlSession = threadLocalSession.get();
		sqlSession.rollback();
		sqlSession.close();
		threadLocalSession.set(null);
	}
	
	/**
	 * Execute a Mybatis mapper through interface mapper
     *
     * {@sample.xml ../../../doc/mule-mybatis-module.xml.sample mybatis:execute}
	 * 
	 * @param mapper Mapper class to use
	 * @param method Method to invoke on the mapper
	 * @param payload The payload
	 * @param foreignKeyField If set, the id will be populated using the value form foreignKeyValue
	 * @param foreignKeyValue If set, the id will be populated using method set + foreignKeyField + ()
	 * @return Result of MyBatis call
	 * @throws ClassNotFoundException Mapper not found
	 * @throws IOException Io Error
	 * @throws NoSuchMethodException Method on mapper not found 
	 * @throws SecurityException Security Error
	 * @throws InvocationTargetException Error invocating the method
	 * @throws IllegalAccessException Error accessing method
	 * @throws IllegalArgumentException Error in arguments passed to method
	 */
	@Processor
	public Object execute(String mapper, String method, 
	                      @Payload Object payload,
	                      @Optional String foreignKeyField,
	                      @Optional Object foreignKeyValue) throws ClassNotFoundException, IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Class<?> mapperClass = muleContext.getExecutionClassLoader().loadClass(mapper);
		
		if (foreignKeyField != null && foreignKeyValue != null)
		{
		    setForeignKey(payload, foreignKeyField, foreignKeyValue);
		}
		
		SqlSession sqlSession = createSqlSession();
		Object mapperInstance = sqlSession.getMapper(mapperClass);
		
		Method methodInstsance = mapperClass.getMethod(method, payload.getClass());
		Object result = methodInstsance.invoke(mapperInstance, payload);
		closeSqlSession(sqlSession);
		return result;
		
	}
	
	/**
	 * Converts the name of an field into a setter method name, example personId will be converted to setPersonId
	 * 
	 * @param idField the name of the id field to be set
	 * @return set + upper case of first character of idField + the rest of idField
	 */
	protected String formatSetterName(String idField)
	{
	    char firstCharacter = idField.charAt(0);
	    firstCharacter = Character.toUpperCase(firstCharacter);
	    
	    return "set" + firstCharacter + idField.substring(1);
	}
	
	/**
	 * Sets the idField with the value passed in idValue. First we get the setter method using reflection.
	 * 
	 * 
	 * @param payload the object on which the id field will be set
	 * @param idField the name of the id field
	 * @param idValue the value of the id field
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	protected void setForeignKey(Object payload, String idField, Object idValue) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
	    Method setter = payload.getClass().getMethod(formatSetterName(idField), idValue.getClass());
	    setter.invoke(payload, idValue);
	}
	
	/**
	 * Execute Mybatis select one function
	 * 
	 * {@sample.xml ../../../doc/mule-mybatis-module.xml.sample mybatis:select-one}
	 * 
	 * @param statement Fully qualified SQL statement ex: org.mybatis.example.BlogMapper.selectBlog
	 * @param payload The parameter to the SQL statement
	 * @return Result of MyBatis call
	 * @throws IOException Io Error
	 */
	@Processor
	public Object selectOne(String statement, @Payload Object payload) throws IOException
	{
		SqlSession sqlSession = createSqlSession();
		
		Object result = sqlSession.selectOne(statement, payload);
		closeSqlSession(sqlSession);
		return result;
	}
	
	/**
	 * Execute Mybatis select list function
	 * 
	 * {@sample.xml ../../../doc/mule-mybatis-module.xml.sample mybatis:select-list}
	 * 
	 * @param statement Fully qualified SQL statement ex: org.mybatis.example.BlogMapper.selectBlog
	 * @param payload The parameter to the SQL statement
	 * @return Result of MyBatis call
	 * @throws IOException Io Error
	 */
	@Processor
	public Object selectList(String statement, @Payload Object payload) throws IOException{
		SqlSession sqlSession = createSqlSession();
		
		Object result = sqlSession.selectList(statement, payload);
		closeSqlSession(sqlSession);
		return result;
	}
	
	/**
	 * Execute Mybatis select map function
	 * 
	 * {@sample.xml ../../../doc/mule-mybatis-module.xml.sample mybatis:select-map}
	 * 
	 * @param statement Fully qualified SQL statement ex: org.mybatis.example.BlogMapper.selectBlog
	 * @param payload The parameter to the SQL statement
	 * @param mapKey The key to use in the result map
	 * @return Result of MyBatis call
	 * @throws IOException Io Error
	 */
	@Processor
	public Object selectMap(String statement, @Payload Object payload, String mapKey) throws IOException{
		SqlSession sqlSession = createSqlSession();
		
		Object result = sqlSession.selectMap(statement, payload, mapKey);
		closeSqlSession(sqlSession);
		return result;
	}
	
	/**
	 * Execute Mybatis update function
	 * 
	 * {@sample.xml ../../../doc/mule-mybatis-module.xml.sample mybatis:update}
	 * 
	 * @param statement Fully qualified SQL statement ex: org.mybatis.example.BlogMapper.selectBlog
	 * @param payload The parameter to the SQL statement
	 * @return Result of MyBatis call
	 * @throws IOException Io Error
	 */
	@Processor
	public Object update(String statement, @Payload Object payload) throws IOException{
		SqlSession sqlSession = createSqlSession();
		
		Object result = sqlSession.update(statement, payload);
		closeSqlSession(sqlSession);
		return result;
	}
	
	/**
	 * Execute Mybatis insert function
	 * 
	 * {@sample.xml ../../../doc/mule-mybatis-module.xml.sample mybatis:insert}
	 * 
	 * @param statement Fully qualified SQL statement ex: org.mybatis.example.BlogMapper.selectBlog
	 * @param payload The parameter to the SQL statement
	 * @return Result of MyBatis call
	 * @throws IOException Io Error
	 */
	@Processor
	public Object insert(String statement, @Payload Object payload) throws IOException{
		SqlSession sqlSession = createSqlSession();
		
		Object result = sqlSession.insert(statement, payload);
		closeSqlSession(sqlSession);
		return result;
	}
	
	/**
	 * Execute Mybatis delete function
	 * 
	 * {@sample.xml ../../../doc/mule-mybatis-module.xml.sample mybatis:delete}
	 * 
	 * @param statement Fully qualified SQL statement ex: org.mybatis.example.BlogMapper.selectBlog
	 * @param  Set if you want to commit immediately
	 * @param payload The parameter to the SQL statement
	 * @return Result of MyBatis call
	 * @throws IOException Io Error
	 */
	@Processor
	public Object delete(String statement, @Payload Object payload) throws IOException{
		SqlSession sqlSession = createSqlSession();
		
		Object result = sqlSession.delete(statement, payload);
		closeSqlSession(sqlSession);
		return result;
	}
	
	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}
	
	public void setMuleContext(MuleContext muleContext)
	{
	      this.muleContext = muleContext;
	}
	
}
