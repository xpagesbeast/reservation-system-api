package com.reservation_system.application.services;

import com.reservation_system.domain.Customer;
import com.reservation_system.infrastructure.persistence.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer findOrCreateCustomer(String name, String email, String phone) {
        Optional<Customer> existingCustomer = customerRepository.findByEmail(email);

        if (existingCustomer.isPresent()) {
            return existingCustomer.get();
        }

        Customer newCustomer = new Customer(name, email, phone);
        return customerRepository.save(newCustomer);
    }

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer createCustomer(String name, String email, String phone) {
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalStateException("Customer with email " + email + " already exists");
        }

        Customer customer = new Customer(name, email, phone);
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long customerId, String name, String email, String phone) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Check if email conflicts with another customer
        Optional<Customer> existingCustomer = customerRepository.findByEmail(email);
        if (existingCustomer.isPresent() && !existingCustomer.get().getId().equals(customerId)) {
            throw new IllegalStateException("Customer with email " + email + " already exists");
        }

        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        return customerRepository.save(customer);
    }

    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }
}
