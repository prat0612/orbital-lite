package com.orbital.lite.service;

import com.orbital.lite.audit.Auditable;
import com.orbital.lite.dto.EmployeeRequest;
import com.orbital.lite.dto.EmployeeResponse;
import com.orbital.lite.entity.Employee;
import com.orbital.lite.exception.DuplicateEmailException;
import com.orbital.lite.exception.ResourceNotFoundException;
import com.orbital.lite.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Auditable(action = "CREATE", entity = "EMPLOYEE")
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (employeeRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        Employee employee = new Employee();
        applyRequest(employee, request, normalizedEmail);
        Employee saved = employeeRepository.save(employee);
        log.info("Created employee {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getEmployees(int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return employeeRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return toResponse(findEmployee(id));
    }

    @Override
    @Auditable(action = "UPDATE", entity = "EMPLOYEE")
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        String normalizedEmail = normalizeEmail(request.email());

        employeeRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateEmailException(normalizedEmail);
                });

        applyRequest(employee, request, normalizedEmail);
        Employee saved = employeeRepository.save(employee);
        log.info("Updated employee {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Auditable(action = "DELETE", entity = "EMPLOYEE")
    public void deleteEmployee(Long id) {
        Employee employee = findEmployee(id);
        employeeRepository.delete(employee);
        log.info("Deleted employee {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> searchEmployees(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return List.of();
        }

        return employeeRepository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRoleContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
                        normalizedQuery,
                        normalizedQuery,
                        normalizedQuery,
                        normalizedQuery
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    private void applyRequest(Employee employee, EmployeeRequest request, String normalizedEmail) {
        employee.setName(request.name().trim());
        employee.setEmail(normalizedEmail);
        employee.setRole(trimToNull(request.role()));
        employee.setDepartment(trimToNull(request.department()));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getRole(),
                employee.getDepartment(),
                employee.getCreatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
