package com.digitaljedi.jpalocking.service;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import com.digitaljedi.jpalocking.domain.Person;
import com.digitaljedi.jpalocking.repo.PersonRepository;

@Service
public class PersonService {

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private RetryTemplate retryTemplate;

	private Log LOG = LogFactory.getLog(this.getClass());
	
	@Transactional
	@Retryable(include= {Exception.class}, maxAttempts=10)
	public Person writeRetryAnnotation(Person p) {
		LOG.debug("writeRetryAnnotation");
		
		Person person;
		if (p.getId()==null) {
			person = new Person();
		} else {
			person = personRepository.findOne(p.getId());
		}
		
		person.setFirstname(p.getFirstname());
		person.setLastname(p.getLastname());
		person = personRepository.save(person);
		LOG.info(person);
		return person;
	}
	
	
	@Transactional
//	@Retryable
	public Person writeRetry(final Person p) {
		Person person = retryTemplate.execute(new RetryCallback<Person, RuntimeException>() {
			@Override
			public Person doWithRetry(RetryContext arg0) throws RuntimeException {
				LOG.debug("writeRetry");
				
				Person person;
				if (p.getId()==null) {
					person = new Person();
				} else {
					person = personRepository.findOne(p.getId());
				}
				
				person.setFirstname(p.getFirstname());
				person.setLastname(p.getLastname());
				person = personRepository.save(person);
				LOG.info(person);
				return person;
			}
		});
		return person;
	}
	
	@Transactional 
	@Retryable(RuntimeException.class)
	public Person lockedWrite(Person p) {
		LOG.debug("writeRetry");
		
		Person person;
		if (p.getId()==null) {
			person = new Person();
		} else {
			person = personRepository.findByIdForUpdate(p.getId());
		}
		
		person.setFirstname(p.getFirstname());
		person.setLastname(p.getLastname());
		person = personRepository.save(p);
		LOG.info(person);
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
		LOG.debug("read");
		return personRepository.findOne(id);
	}

	@Transactional
	public Person readForUpdate(Integer id) {
		LOG.debug("findByIdForUpdate");
		return personRepository.findByIdForUpdate(id);
	}

}
