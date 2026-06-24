package com.SI.Crud.funcionarios_SI.model;

import java.util.ArrayList;
import java.util.List;

public class Employee {
    private String id;
    private String name;
    private String department;
    private double salary; 
    private List<String> history;
    private List<String> documents;

    public static final double ANGOLA_MINIMUM_WAGE = 100000.00;

    public Employee(String id, String name, String department, double salary) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID inválido.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Nome não pode ser vazio.");
        if (department == null || department.trim().isEmpty()) throw new IllegalArgumentException("Departamento inválido.");
        
        if (salary < ANGOLA_MINIMUM_WAGE) {
            throw new IllegalArgumentException("O salário inicial não pode ser menor que o salário mínimo nacional (100.000,00 Kz).");
        }

        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
        this.history = new ArrayList<>();
        this.documents = new ArrayList<>();
        this.history.add("Funcionário registrado com o salário base de " + String.format("%,.2f Kz", salary));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public double getSalary() { return salary; }
    public List<String> getHistory() { return new ArrayList<>(history); }
    public List<String> getDocuments() { return new ArrayList<>(documents); }

    public void setSalary(double newSalary) {
        if (newSalary < ANGOLA_MINIMUM_WAGE) {
            throw new IllegalArgumentException("Violação de Lei: Em Angola, o salário mínimo permitido é de 100.000,00 Kz.");
        }
        String logValue = String.format("Alteração salarial: de %,.2f Kz para %,.2f Kz", this.salary, newSalary);
        this.history.add(logValue);
        this.salary = newSalary;
    }

    public void addDocument(String document) {
        if (document == null || document.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do documento inválido.");
        }
        this.documents.add(document);
        this.history.add("Documento anexado: " + document);
    }
}
