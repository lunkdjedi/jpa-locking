package com.digitaljedi.jpalocking.component.workers;

import java.util.Random;
import java.util.UUID;

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

	Log LOG = LogFactory.getLog(this.getClass());

	@Override
	public void run() {
		for (int i=0; i<100; i++) {
			Person person = personService.read(1);
			person.setFirstname(UUID.randomUUID().toString());
			person = personService.write(person);
			LOG.info(person);
		}
		
	}

}
