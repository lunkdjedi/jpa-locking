package com.digitaljedi.jpalocking.service;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.digitaljedi.jpalocking.domain.Person;
import com.digitaljedi.jpalocking.repo.PersonRepository;

@Service
public class PersonService {

	@Autowired
	private PersonRepository personRepository;

	private Log LOG = LogFactory.getLog(this.getClass());

	@Transactional
	@Retryable
	public Person write(Person p) {
		LOG.info("write");
		Person person = p;
		person = personRepository.save(p);
		return person;
	}

	@Transactional
	public Person slowWrite(Person p) {
		LOG.info("slowWrite");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return personRepository.save(p);
	}

	@Transactional
	public Person read(Integer id) {
		LOG.info("read");
		return personRepository.findOne(id);
	}
	
	@Transactional
	public Person readForUpdate(Integer id) {
		LOG.info("findByIdForUpdate");
		return personRepository.findByIdForUpdate(id);
	}

}
