package com.financeapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserDto {

    public static class Request {
        private String email;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String password;

        public Request() {
        }

        public String getEmail() {
            return email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public String getPassword() {
            return password;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest() {
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Response {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private LocalDate dateOfBirth;
        private int age;
        private String ageBracket;
        private LocalDateTime createdAt;

        public Response() {
        }

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getFullName() {
            return fullName;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public int getAge() {
            return age;
        }

        public String getAgeBracket() {
            return ageBracket;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setAgeBracket(String ageBracket) {
            this.ageBracket = ageBracket;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}