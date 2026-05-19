package com.SI.Crud.funcionarios_SI.controller;

import com.SI.Crud.funcionarios_SI.model.dto.request.EmployeeRequest;
import com.SI.Crud.funcionarios_SI.model.dto.request.RegisterRequest;
import com.SI.Crud.funcionarios_SI.model.entity.Department;
import com.SI.Crud.funcionarios_SI.model.entity.Employee;
import com.SI.Crud.funcionarios_SI.model.entity.User;
import com.SI.Crud.funcionarios_SI.repository.DepartmentRepository;
import com.SI.Crud.funcionarios_SI.repository.EmployeeRepository;
import com.SI.Crud.funcionarios_SI.repository.UserRepository;
import com.SI.Crud.funcionarios_SI.service.DepartmentService;
import com.SI.Crud.funcionarios_SI.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home(Model model, @RequestParam(value = "logout", required = false) String logout) {
        if (logout != null) {
            model.addAttribute("message", "Sessao terminada com sucesso.");
        }
        model.addAttribute("totalEmployees", employeeRepository.count());
        model.addAttribute("departmentsCount", departmentRepository.count());
        model.addAttribute("monthlyPayroll", formatCurrency(employeeRepository.sumSalary()));
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model, @RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            model.addAttribute("message", "Email ou senha invalidos.");
            model.addAttribute("messageType", "error");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String createAccount(
            @Valid @ModelAttribute RegisterRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (userRepository.existsByEmail(request.getEmail())) {
            bindingResult.rejectValue("email", "email.exists", "Este email ja esta registado.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "Revise os dados da conta e tente novamente.");
            model.addAttribute("messageType", "error");
            return "register";
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "Conta criada. Entre para continuar.");
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @ModelAttribute("message") String message) {
        List<Employee> employees = employeeRepository.findAllWithDepartment().stream()
                .sorted(Comparator.comparing(Employee::getName))
                .toList();
        List<DepartmentView> departmentViews = departmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Department::getName))
                .map(department -> new DepartmentView(
                        department.getId(),
                        department.getName(),
                        department.getDescription(),
                        employeeRepository.countByDepartmentId(department.getId()),
                        0
                ))
                .toList();

        long maxDepartmentEmployees = departmentViews.stream()
                .mapToLong(DepartmentView::employees)
                .max()
                .orElse(0);
        List<DepartmentView> departments = departmentViews.stream()
                .map(department -> new DepartmentView(
                        department.id(),
                        department.name(),
                        department.description(),
                        department.employees(),
                        maxDepartmentEmployees == 0 ? 0 : Math.round((department.employees() * 100.0) / maxDepartmentEmployees)
                ))
                .toList();

        model.addAttribute("employees", employees);
        model.addAttribute("departments", departments);
        model.addAttribute("departmentOptions", departmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Department::getName))
                .toList());
        model.addAttribute("employeeRequest", new EmployeeRequest());
        model.addAttribute("department", new Department());
        model.addAttribute("totalEmployees", employeeRepository.count());
        model.addAttribute("departmentsCount", departmentRepository.count());
        model.addAttribute("monthlyPayroll", formatCurrency(employeeRepository.sumSalary()));
        model.addAttribute("averageSalary", formatAverageSalary());
        model.addAttribute("message", message);
        return "dashboard";
    }

    @PostMapping("/employees")
    public String createEmployee(
            @Valid @ModelAttribute EmployeeRequest employeeRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("message", "Nao foi possivel cadastrar o funcionario. Verifique os campos.");
            return "redirect:/dashboard";
        }

        employeeService.create(employeeRequest);
        redirectAttributes.addFlashAttribute("message", "Funcionario cadastrado e vinculado ao departamento.");
        return "redirect:/dashboard#employees";
    }

    @PostMapping("/employees/{id}/update")
    public String updateEmployee(
            @PathVariable Long id,
            @Valid @ModelAttribute EmployeeRequest employeeRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("message", "Nao foi possivel actualizar o funcionario. Verifique os campos.");
            return "redirect:/dashboard#employees";
        }

        employeeService.update(id, employeeRequest);
        redirectAttributes.addFlashAttribute("message", "Funcionario actualizado com sucesso.");
        return "redirect:/dashboard#employees";
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Funcionario enviado para a lixeira.");
        return "redirect:/dashboard#employees";
    }

    @PostMapping("/departments")
    public String createDepartment(
            @Valid @ModelAttribute Department department,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("message", "Informe o nome do departamento.");
            return "redirect:/dashboard";
        }

        departmentService.create(department);
        redirectAttributes.addFlashAttribute("message", "Departamento criado com sucesso.");
        return "redirect:/dashboard#departments";
    }

    @PostMapping("/departments/{id}/update")
    public String updateDepartment(
            @PathVariable Long id,
            @Valid @ModelAttribute Department department,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("message", "Nao foi possivel actualizar o departamento. Informe o nome.");
            return "redirect:/dashboard#departments";
        }

        departmentService.update(id, department);
        redirectAttributes.addFlashAttribute("message", "Departamento actualizado com sucesso.");
        return "redirect:/dashboard#departments";
    }

    @PostMapping("/departments/{id}/delete")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (employeeRepository.countByDepartmentId(id) > 0) {
            redirectAttributes.addFlashAttribute("message", "Nao e possivel apagar um departamento com funcionarios vinculados.");
            return "redirect:/dashboard#departments";
        }

        departmentService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Departamento enviado para a lixeira.");
        return "redirect:/dashboard#departments";
    }

    private String formatAverageSalary() {
        long totalEmployees = employeeRepository.count();
        if (totalEmployees == 0) {
            return formatCurrency(BigDecimal.ZERO);
        }
        return formatCurrency(employeeRepository.sumSalary().divide(BigDecimal.valueOf(totalEmployees), 0, java.math.RoundingMode.HALF_UP));
    }

    private String formatCurrency(BigDecimal value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("pt", "AO"));
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(value == null ? BigDecimal.ZERO : value) + " Kz";
    }

    public record DepartmentView(
            Long id,
            String name,
            String description,
            long employees,
            long percentage
    ) {
    }
}
