package com.SI.Crud.funcionarios_SI;

import com.SI.Crud.funcionarios_SI.model.Employee;
import com.SI.Crud.funcionarios_SI.service.AuditService;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ManagementSystem {
    private static final Map<String, Employee> database = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final String currentOperator = "SYSTEM_OPERATOR"; 

    public static void main(String[] args) {
        try {
            database.put("201", new Employee("201", "Trabalhador Exemplo", "Recursos Humanos", 150000.00));
            database.get("201").addDocument("BI_Trabalhador.pdf");
        } catch (Exception ignored) {}

        while (true) {
            try {
                showMenu();
                String option = scanner.nextLine().trim();

                switch (option) {
                    case "1":
                        viewEmployeeDetails();
                        break;
                    case "2":
                        updateEmployeeSalary();
                        break;
                    case "3":
                        viewAuditPanel();
                        break;
                    case "4":
                        System.out.println("Saindo do sistema com segurança...");
                        return;
                    default:
                        System.out.println("Erro: Opção inválida! Digite apenas números de 1 a 4.");
                }
            } catch (Exception e) {
                System.out.println("Ocorreu uma falha inesperada no sistema de leitura. Reiniciando menu.");
                AuditService.logAction(currentOperator, "CRITICAL_MENU_ERROR", e.getMessage());
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n==================================================");
        System.out.println("  SISTEMA DE GESTÃO DE FUNCIONÁRIOS - ANGOLA (Kz) ");
        System.out.println("==================================================");
        System.out.println("1. Ver Detalhes do Funcionário (Visão Individual)");
        System.out.println("2. Alterar Salário (Validação de Mínimo Legal)");
        System.out.println("3. Consultar Painel de Auditoria e Logs");
        System.out.println("4. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private static void viewEmployeeDetails() {
        try {
            System.out.print("Digite o código/ID do funcionário: ");
            String id = scanner.nextLine().trim();

            if (id.isEmpty()) {
                System.out.println("Erro: O campo do ID não pode ser enviado em branco.");
                return;
            }

            Employee emp = database.get(id);
            if (emp == null) {
                System.out.println("Erro: Nenhum funcionário localizado com o ID '" + id + "'.");
                return;
            }

            System.out.println("\n------------------------------------------------");
            System.out.println("      DETALHES DO FUNCIONÁRIO (VISÃO INDIVIDUAL) ");
            System.out.println("------------------------------------------------");
            System.out.println("ID:           " + emp.getId());
            System.out.println("Nome:         " + emp.getName());
            System.out.println("Departamento: " + emp.getDepartment());
            System.out.printf("Salário Atual: %,.2f Kz\n", emp.getSalary());
            
            System.out.println("\nDOCUMENTOS ANEXADOS:");
            if (emp.getDocuments().isEmpty()) {
                System.out.println("  [Nenhum documento digitalizado]");
            } else {
                emp.getDocuments().forEach(doc -> System.out.println("  - " + doc));
            }

            System.out.println("\nTEMPO HISTÓRICO COMPLETO:");
            emp.getHistory().forEach(hist -> System.out.println("  * " + hist));
            System.out.println("------------------------------------------------");
        } catch (Exception e) {
            System.out.println("Falha ao exibir os detalhes do funcionário devido a dados corrompidos.");
        }
    }

    private static void updateEmployeeSalary() {
        try {
            System.out.print("Digite o ID do funcionário: ");
            String id = scanner.nextLine().trim();

            Employee emp = database.get(id);
            if (emp == null) {
                System.out.println("Erro: O ID digitado não existe no sistema.");
                return;
            }

            System.out.print("Digite o novo salário em Kz (Mínimo: 100.000,00 Kz): ");
            String salaryInput = scanner.nextLine().trim();

            if (salaryInput.isEmpty()) {
                System.out.println("Erro: Valor salarial não pode ser em branco.");
                return;
            }

            try {
                String cleanValue = salaryInput.replace(".", "").replace(",", ".");
                double parsedSalary = Double.parseDouble(cleanValue);
                
                double oldSalary = emp.getSalary();
                emp.setSalary(parsedSalary); 

                AuditService.logAction(
                    currentOperator, 
                    "SALARY_UPDATE", 
                    String.format("Funcionario ID %s atualizado de %,.2f Kz para %,.2f Kz", id, oldSalary, parsedSalary)
                );
                System.out.println("Salário atualizado com sucesso no sistema!");

            } catch (NumberFormatException e) {
                System.out.println("Erro de Digitação: Você inseriu letras ou símbolos inválidos. Use apenas números (Ex: 125000).");
                AuditService.logAction(currentOperator, "VALIDATION_FAILURE", "Entrada inválida de salário por erro de caractere.");
            } catch (IllegalArgumentException e) {
                System.out.println("Erro de Regra Legal: " + e.getMessage());
                AuditService.logAction(currentOperator, "VALIDATION_FAILURE", "Tentativa de inserir salário abaixo do mínimo de Angola.");
            }
        } catch (Exception e) {
            System.out.println("Erro inesperado ao tentar processar alteração salarial.");
        }
    }

    private static void viewAuditPanel() {
        try {
            System.out.println("\n=================================================================================");
            System.out.println("                       PAINEL DE AUDITORIA E LOGS DE SEGURANÇA                   ");
            System.out.println("=================================================================================");
            var allLogs = AuditService.getAllLogs();
            if (allLogs.isEmpty()) {
                System.out.println("Nenhum log sensível registrado.");
            } else {
                allLogs.forEach(System.out.println);
            }
            System.out.println("=================================================================================");
        } catch (Exception e) {
            System.out.println("Falha ao ler logs de auditoria concorrentes.");
        }
    }
}
