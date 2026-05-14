package com.example.PasswordHasher;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Scanner;

public class PasswordHasherApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a raw password to hash:");
        String password = scanner.nextLine();
        System.out.println(new BCryptPasswordEncoder().encode(password));
    }
}
