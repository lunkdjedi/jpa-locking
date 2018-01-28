package com.digitaljedi.jpalocking.component.workers;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
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

    @Autowired
    private RetryTemplate retryTemplate;
    
	String name;

	Log LOG = LogFactory.getLog(this.getClass());

	public RandomPersonDataWorker(String name) {
		this.name = name;
	}

	@Override
	public void run() {
		for (int i = 0; i < 100; i++) {
			retryTemplate.execute(new RetryCallback<Person, RuntimeException>() {
				@Override
				public Person doWithRetry(RetryContext arg0) throws RuntimeException {
					Person person = personService.read(1);
					person.setFirstname(UUID.randomUUID().toString());
					person = personService.write(person);
					LOG.info(person);
					return person; 
				}
				
			});

		}
		LOG.info(name + " Complete");
	}

}
