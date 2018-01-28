package com.digitaljedi.jpalocking.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.digitaljedi.jpalocking.domain.Person;
import com.digitaljedi.jpalocking.repo.PersonRepository;
import com.digitaljedi.jpalocking.service.PersonService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
@SpringBootTest
public class HsqlConfigTest {

	@Autowired
	PersonService personService;

	@Autowired
	PersonRepository personRepository;

	private Log LOG = LogFactory.getLog(this.getClass());

	@Before
//	@Transactional
	public void setUp() {
		Person p = new Person();
		p.setId(1);
		p.setFirstname("test");
		p.setLastname("case");
		personRepository.save(p);
	}

	private void sleep(long millis) {
		try {
			LOG.info("Sleeping "+ millis +"ms...");
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testUpdates() {
		final ExecutorService testThreadPool = Executors.newFixedThreadPool(16);
		testThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				Person p = new Person();
				p.setId(1);
				p.setFirstname("test1");
				p.setLastname("case1");
				LOG.info("Slow Writing Test..." + p);
				Person person = personService.slowWrite(p);
				LOG.info("Slow Writing Test Complete... " + person);
			}
		});
		testThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				Person p = new Person();
				p.setId(1);
				p.setFirstname("test2");
				p.setLastname("case2");
				LOG.info("Writing Test..." + p);
				Person person = personService.write(p);
				LOG.info("Writing Test Complete... " + person);
			}
		});
		
		for (int i=0; i < 100 ; i++) {
			testThreadPool.submit(new Runnable() {
				@Override
				public void run() {
					Person p = new Person();
					p.setId(1);
					p.setFirstname("test1");
					p.setLastname("case1");
					LOG.info("Slow Writing Test..." + p);
					Person person = personService.write(p);
					LOG.info("Slow Writing Test Complete... " + person);
				}
			});
			for (int x=0; x < 100 ; x++) testThreadPool.submit(new Runnable() {
				@Override
				public void run() {
					Person p = new Person();
					p.setId(1);
					p.setFirstname("test"+System.currentTimeMillis());
					p.setLastname("case"+System.currentTimeMillis());
					LOG.info("Writing Test..." + p);
					Person person = personService.write(p);
					LOG.info("Writing Test Complete... " + person);
				}
			});
		}

//		testThreadPool.submit(new Runnable() {
//			@Override
//			public void run() {
//				sleep(5000);
//				LOG.info("Done");
//			}
//		});

		testThreadPool.shutdown();

		try {
			testThreadPool.awaitTermination(60000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		LOG.info("Final Person Object: " + personRepository.findOne(1)); 
	}

}
