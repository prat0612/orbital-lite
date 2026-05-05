package com.orbital.lite.config;

import com.orbital.lite.entity.AppUser;
import com.orbital.lite.entity.AuditLog;
import com.orbital.lite.entity.Employee;
import com.orbital.lite.entity.LeaveRequest;
import com.orbital.lite.entity.LeaveStatus;
import com.orbital.lite.entity.Notification;
import com.orbital.lite.entity.Permission;
import com.orbital.lite.entity.Role;
import com.orbital.lite.repository.AuditLogRepository;
import com.orbital.lite.repository.EmployeeRepository;
import com.orbital.lite.repository.LeaveRequestRepository;
import com.orbital.lite.repository.NotificationRepository;
import com.orbital.lite.repository.PermissionRepository;
import com.orbital.lite.repository.RoleRepository;
import com.orbital.lite.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class DataInitializer {

    private static final List<String> PERMISSIONS = List.of(
            "CREATE_EMPLOYEE",
            "READ_EMPLOYEE",
            "UPDATE_EMPLOYEE",
            "DELETE_EMPLOYEE",
            "MANAGE_USERS",
            "APPLY_LEAVE",
            "VIEW_ALL_LEAVES",
            "APPROVE_LEAVE",
            "VIEW_DASHBOARD",
            "VIEW_REPORTS"
    );

    @Bean
    @Transactional
    public CommandLineRunner seedData(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            LeaveRequestRepository leaveRequestRepository,
            AuditLogRepository auditLogRepository,
            NotificationRepository notificationRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            Map<String, Permission> permissions = seedPermissions(permissionRepository);
            Map<String, Role> roles = seedRoles(roleRepository, permissions);
            Map<String, AppUser> users = seedUsers(userRepository, passwordEncoder, roles);
            List<Employee> employees = seedEmployees(employeeRepository);
            seedLeaveRequests(leaveRequestRepository, users);
            seedAuditLogs(auditLogRepository, users, employees);
            seedNotifications(notificationRepository, users);
        };
    }

    private Map<String, Permission> seedPermissions(PermissionRepository permissionRepository) {
        Map<String, Permission> permissions = new LinkedHashMap<>();
        for (String permissionName : PERMISSIONS) {
            Permission permission = permissionRepository.findByName(permissionName).orElseGet(() -> {
                Permission newPermission = new Permission();
                newPermission.setName(permissionName);
                return permissionRepository.save(newPermission);
            });
            permissions.put(permissionName, permission);
        }
        return permissions;
    }

    private Map<String, Role> seedRoles(RoleRepository roleRepository, Map<String, Permission> permissions) {
        Map<String, Set<String>> rolePermissions = Map.of(
                "ADMIN", new LinkedHashSet<>(PERMISSIONS),
                "MANAGER", Set.of(
                        "CREATE_EMPLOYEE",
                        "READ_EMPLOYEE",
                        "UPDATE_EMPLOYEE",
                        "APPLY_LEAVE",
                        "VIEW_ALL_LEAVES",
                        "APPROVE_LEAVE",
                        "VIEW_DASHBOARD",
                        "VIEW_REPORTS"
                ),
                "EMPLOYEE", Set.of(
                        "READ_EMPLOYEE",
                        "APPLY_LEAVE"
                )
        );

        Map<String, Role> roles = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : rolePermissions.entrySet()) {
            Role role = roleRepository.findByName(entry.getKey()).orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName(entry.getKey());
                return newRole;
            });
            Set<Permission> assignedPermissions = new LinkedHashSet<>();
            for (String permissionName : entry.getValue()) {
                assignedPermissions.add(permissions.get(permissionName));
            }
            role.setPermissions(assignedPermissions);
            roles.put(entry.getKey(), roleRepository.save(role));
        }
        return roles;
    }

    private Map<String, AppUser> seedUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Map<String, Role> roles
    ) {
        Map<String, AppUser> users = new LinkedHashMap<>();
        users.put("admin", createUser(userRepository, passwordEncoder, "admin", "admin", roles.get("ADMIN"), 45));
        users.put("manager", createUser(userRepository, passwordEncoder, "manager", "manager", roles.get("MANAGER"), 40));
        users.put("employee", createUser(userRepository, passwordEncoder, "employee", "employee", roles.get("EMPLOYEE"), 35));
        return users;
    }

    private AppUser createUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String password,
            Role role,
            int daysAgo
    ) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setRoles(Set.of(role));
        user.setCreatedAt(daysAgo(daysAgo));
        return userRepository.save(user);
    }

    private List<Employee> seedEmployees(EmployeeRepository employeeRepository) {
        return List.of(
                saveEmployee(employeeRepository, employee("Aarav Sharma", "aarav.sharma@orbital.test", "Backend Engineer", "IT", 28)),
                saveEmployee(employeeRepository, employee("Maya Iyer", "maya.iyer@orbital.test", "HR Business Partner", "HR", 27)),
                saveEmployee(employeeRepository, employee("Rohan Mehta", "rohan.mehta@orbital.test", "Finance Analyst", "Finance", 25)),
                saveEmployee(employeeRepository, employee("Neha Kapoor", "neha.kapoor@orbital.test", "Product Manager", "Product", 24)),
                saveEmployee(employeeRepository, employee("Kabir Khan", "kabir.khan@orbital.test", "Frontend Engineer", "IT", 22)),
                saveEmployee(employeeRepository, employee("Ananya Rao", "ananya.rao@orbital.test", "Recruiter", "HR", 20)),
                saveEmployee(employeeRepository, employee("Vikram Singh", "vikram.singh@orbital.test", "Accountant", "Finance", 18)),
                saveEmployee(employeeRepository, employee("Sara Thomas", "sara.thomas@orbital.test", "UX Designer", "Product", 16)),
                saveEmployee(employeeRepository, employee("Dev Patel", "dev.patel@orbital.test", "DevOps Engineer", "IT", 14)),
                saveEmployee(employeeRepository, employee("Isha Nair", "isha.nair@orbital.test", "Payroll Specialist", "Finance", 12)),
                saveEmployee(employeeRepository, employee("Arjun Menon", "arjun.menon@orbital.test", "QA Engineer", "IT", 10)),
                saveEmployee(employeeRepository, employee("Priya Das", "priya.das@orbital.test", "Product Analyst", "Product", 8))
        );
    }

    private Employee saveEmployee(EmployeeRepository employeeRepository, Employee employee) {
        return employeeRepository.findByEmailIgnoreCase(employee.getEmail())
                .orElseGet(() -> employeeRepository.save(employee));
    }

    private Employee employee(String name, String email, String role, String department, int daysAgo) {
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setRole(role);
        employee.setDepartment(department);
        employee.setCreatedAt(daysAgo(daysAgo));
        return employee;
    }

    private void seedLeaveRequests(LeaveRequestRepository leaveRequestRepository, Map<String, AppUser> users) {
        List<LeaveRequest> requests = List.of(
                leave(users.get("employee"), "2026-05-07", "2026-05-08", LeaveStatus.APPROVED, "Family function", 21),
                leave(users.get("employee"), "2026-05-15", "2026-05-17", LeaveStatus.PENDING, "Travel plans", 20),
                leave(users.get("employee"), "2026-06-03", "2026-06-03", LeaveStatus.APPROVED, "Medical appointment", 19),
                leave(users.get("employee"), "2026-06-12", "2026-06-18", LeaveStatus.REJECTED, "Extended vacation", 18),
                leave(users.get("manager"), "2026-05-09", "2026-05-10", LeaveStatus.APPROVED, "Personal work", 17),
                leave(users.get("manager"), "2026-05-24", "2026-05-28", LeaveStatus.PENDING, "Conference travel", 16),
                leave(users.get("manager"), "2026-06-09", "2026-06-11", LeaveStatus.REJECTED, "Schedule conflict", 15),
                leave(users.get("admin"), "2026-05-13", "2026-05-13", LeaveStatus.APPROVED, "Bank visit", 14),
                leave(users.get("admin"), "2026-05-30", "2026-06-01", LeaveStatus.PENDING, "Family travel", 13),
                leave(users.get("admin"), "2026-06-20", "2026-06-21", LeaveStatus.APPROVED, "Weekend extension", 12),
                leave(users.get("employee"), "2026-07-01", "2026-07-05", LeaveStatus.PENDING, "Summer break", 11),
                leave(users.get("manager"), "2026-07-10", "2026-07-10", LeaveStatus.APPROVED, "Health checkup", 10)
        );
        leaveRequestRepository.saveAll(requests);
    }

    private LeaveRequest leave(
            AppUser user,
            String startDate,
            String endDate,
            LeaveStatus status,
            String reason,
            int daysAgo
    ) {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setUser(user);
        leaveRequest.setStartDate(LocalDate.parse(startDate));
        leaveRequest.setEndDate(LocalDate.parse(endDate));
        leaveRequest.setStatus(status);
        leaveRequest.setReason(reason);
        leaveRequest.setCreatedAt(daysAgo(daysAgo));
        return leaveRequest;
    }

    private void seedAuditLogs(
            AuditLogRepository auditLogRepository,
            Map<String, AppUser> users,
            List<Employee> employees
    ) {
        auditLogRepository.saveAll(List.of(
                audit(users.get("admin").getUsername(), "LOGIN", "USER", users.get("admin").getId(), 9),
                audit(users.get("manager").getUsername(), "LOGIN", "USER", users.get("manager").getId(), 8),
                audit(users.get("admin").getUsername(), "CREATE_EMPLOYEE", "EMPLOYEE", employees.get(0).getId(), 7),
                audit(users.get("manager").getUsername(), "UPDATE_EMPLOYEE", "EMPLOYEE", employees.get(4).getId(), 6),
                audit(users.get("manager").getUsername(), "APPROVE_LEAVE", "LEAVE", null, 5),
                audit(users.get("admin").getUsername(), "CREATE_EMPLOYEE", "EMPLOYEE", employees.get(7).getId(), 4),
                audit(users.get("employee").getUsername(), "LOGIN", "USER", users.get("employee").getId(), 3)
        ));
    }

    private AuditLog audit(String user, String action, String entity, Long entityId, int daysAgo) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntity(entity);
        auditLog.setEntityId(entityId);
        auditLog.setTimestamp(daysAgo(daysAgo));
        return auditLog;
    }

    private void seedNotifications(NotificationRepository notificationRepository, Map<String, AppUser> users) {
        notificationRepository.saveAll(List.of(
                notification(users.get("employee"), "Leave approved for May 7-8.", 8),
                notification(users.get("employee"), "Leave rejected for June 12-18.", 7),
                notification(users.get("manager"), "Leave approved for May 9-10.", 6),
                notification(users.get("manager"), "Leave rejected for June 9-11.", 5),
                notification(users.get("admin"), "New employee added: Aarav Sharma.", 4),
                notification(users.get("admin"), "New employee added: Sara Thomas.", 3),
                notification(users.get("employee"), "New employee added: Dev Patel.", 2),
                notification(users.get("manager"), "Leave approved for July 10.", 1)
        ));
    }

    private Notification notification(AppUser user, String message, int daysAgo) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(daysAgo(daysAgo));
        return notification;
    }

    private Instant daysAgo(int daysAgo) {
        return Instant.now().minus(daysAgo, ChronoUnit.DAYS);
    }
}
