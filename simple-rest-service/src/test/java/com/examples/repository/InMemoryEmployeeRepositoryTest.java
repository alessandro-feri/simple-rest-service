package com.examples.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.examples.model.Employee;

/**
 * In spite of {@link InMemoryEmployeeRepositoryTest} being an in-memory,
 * probably temporary, implementation of a repository, it's better to write
 * tests for that, in order to make sure that if something is not working in the
 * integration tests, then it's not the repository's fault.
 */
public class InMemoryEmployeeRepositoryTest {

	private InMemoryEmployeeRepository repository;

	private Map<String, Employee> map;

	@Before
	public void setup() {
		map = new HashMap<>();
		repository = new InMemoryEmployeeRepository(map);
		// make sure the repository is always empty
		map.clear();
	}

	@Test
	public void testFindAll() {
		Employee e1 = new Employee("ID1", "Test Employee", 0);
		Employee e2 = new Employee("ID2", "Test Employee", 0);
		map.put("ID1", e1);
		map.put("ID2", e2);
		assertThat(repository.findAll())
			.containsExactlyInAnyOrder(e1, e2);
	}

	@Test
	public void testFindOne() {
		assertThat(repository.findOne("ID1")).isEmpty();
		Employee e1 = new Employee("ID1", "Test Employee", 0);
		map.put("ID1", e1);
		assertThat(repository.findOne("ID1")).contains(e1);
	}

	@Test
	public void testSaveWithoutIdCreatesAnIdAutomatically() {
		Employee e = new Employee(null, "Test Employee", 0);
		Employee saved = repository.save(e);
		String generatedId = saved.getEmployeeId();
		assertThat(generatedId).isNotNull();
		assertThat(map.get(generatedId)).isEqualTo(saved);
	}

	@Test
	public void testSaveWithIdReplacesTheExistingEmployee() {
		String id = "ID1";
		Employee original = new Employee(id, "Test Employee", 0);
		map.put(original.getEmployeeId(), original);
		Employee modified = new Employee(id, "Modified", 0);
		Employee saved = repository.save(modified);
		assertThat(saved).isSameAs(modified);
		assertThat(map.get(id))
			.isSameAs(modified)
			.isNotSameAs(original);
	}

	@Test
	public void testDeleteById() {
		String id = "ID1";
		Employee employee = new Employee(id, "Test Employee", 0);
		map.put(employee.getEmployeeId(), employee);
		assertThat(repository.deleteById(id))
			.isSameAs(employee);
		assertThat(repository.deleteById("non-existent"))
			.isNull();
	}
}