package com.digitaljedi.jpalocking.repo;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.digitaljedi.jpalocking.domain.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Person p where id = ?1")
	public Person findByIdForUpdate(Integer id);
	
}
