package com.digitaljedi.jpalocking.component.workers;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.digitaljedi.jpalocking.domain.Person;
import com.digitaljedi.jpalocking.service.PersonService;

@Component
@Scope("prototype")
public class RandomPersonDataWorker implements Runnable {

	@Autowired
	PersonService personService;

	@Autowired
	Random random;

	String name;

	Log LOG = LogFactory.getLog(this.getClass());

	public RandomPersonDataWorker(String name) {
		this.name = name;
	}
	
	private AtomicInteger loopCounter = new AtomicInteger();
	private AtomicInteger exceptionCounter = new AtomicInteger();
	
	@Override
	public void run() {
		for (int i = 0; i < 100; i++) {
			loopCounter.getAndIncrement();
			try {
				Person person = personService.read(1);
				person.setFirstname(UUID.randomUUID().toString());
				person = personService.writeRetryAnnotation(person);
			} catch (Exception e) {
				LOG.info("Worker Caught Exception" + e.getClass() + " Count:" +exceptionCounter.get());
			}
		}
		LOG.info(name + " Completed "+ loopCounter.get() + " loops");
	}

}
