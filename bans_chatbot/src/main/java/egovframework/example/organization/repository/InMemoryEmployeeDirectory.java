package egovframework.example.organization.repository;

import egovframework.example.organization.domain.Employee;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Synthetic fixture only; never a persistent repository. */
@Repository
@Profile({"local-mock", "test"})
public class InMemoryEmployeeDirectory implements EmployeeDirectory {
    private final List<Employee> employees = IntStream.rangeClosed(1, 20)
            .mapToObj(number -> new Employee(
                    String.format(Locale.ROOT, "EMP-%03d", number),
                    String.format(Locale.ROOT, "가상직원%02d", number),
                    String.format(Locale.ROOT, "D%02d", (number - 1) % 5 + 1)))
            .toList();

    @Override
    public List<Employee> findAll() {
        return employees;
    }
}
