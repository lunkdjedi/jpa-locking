package com.digitaljedi.jpalocking.service;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.digitaljedi.jpalocking.domain.Person;
import com.digitaljedi.jpalocking.repo.PersonRepository;

@Service
public class PersonService {

	@Autowired
	private PersonRepository personRepository;
	
	private Log LOG = LogFactory.getLog(this.getClass());
	
	@Transactional
	public Person slowWrite(Person p) {
		Person person = personRepository.findById(p.getId());
		if (person==null) person = new Person();
		person.setFirstname(p.getFirstname());
		person.setLastname(p.getLastname());
		try {
			LOG.info("slowWriteSleeping...");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOG.info("Slow Wrote " + personRepository.save(person));
		return person;
	}
	
	@Transactional
	public Person write(Person p) {
		Person person = personRepository.findById(p.getId());
		if (person==null) person = new Person();
		person.setFirstname(p.getFirstname());
		person.setLastname(p.getLastname());
		person = personRepository.save(person);
		LOG.info("Wrote " + person);
		return person;
	}
}
