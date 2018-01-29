package com.digitaljedi.jpalocking.config;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.digitaljedi.jpalocking.component.workers.RandomPersonDataWorker;
import com.digitaljedi.jpalocking.domain.Person;
import com.digitaljedi.jpalocking.service.PersonService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
@SpringBootTest
public class PessimisticTest {

	@Autowired
	PersonService personService;

	@Autowired
	ApplicationContext applicationContext;

	Log LOG = LogFactory.getLog(this.getClass());

	@Before
	public void setUp() {
		Person p = new Person();
		p.setFirstname("test1");
		p.setLastname("case1");
		personService.writeRetry(p);
	}

	@Test
	public void testSetup() {
		Person person = personService.read(1);
		Assert.assertNotNull(person);
		LOG.info(person);
	}

	@Test
	public void testWrite() {
		Person person = personService.read(1);
		Assert.assertNotNull(person);
		person.setFirstname("testWrite");
		person = personService.writeRetry(person);
		LOG.info(person);
	}

	@Test
	public void testSlowWrite() {
		Person person = personService.read(1);
		Assert.assertNotNull(person);
		person.setFirstname("testSlowWrite");
		person = personService.slowWrite(person);
		LOG.info(person);
	}

	@Test
	public void testLockedWrite() {
		Person person = personService.readForUpdate(1);
		person = personService.writeRetry(person);
		Assert.assertNotNull(person);
		person.setFirstname("testLockedWrite");
		person = personService.writeRetry(person);
		LOG.info(person);
	}

	@Test
	public void testSlowLockedWrite() {
		Person person = personService.readForUpdate(1);
		person = personService.slowWrite(person);
		Assert.assertNotNull(person);
		person.setFirstname("testSlowLockedWrite");
		person = personService.writeRetry(person);
		LOG.info(person);
	}

	@Test
	public void testGenerateOptimisticLockExceptions() {
		ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) applicationContext.getBean("taskExecutor");

		for (int i = 0; i < 10; i++) {
			RandomPersonDataWorker worker = applicationContext.getBean(RandomPersonDataWorker.class, "worker #" + i);
			taskExecutor.submit(worker);
			LOG.info("Submitted: " + worker);
		}

		taskExecutor.shutdown();

		try {
			taskExecutor.getThreadPoolExecutor().awaitTermination(60, TimeUnit.SECONDS);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		LOG.info(personService.read(1));

	}

}
