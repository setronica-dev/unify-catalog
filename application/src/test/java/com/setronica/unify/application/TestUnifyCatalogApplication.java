package com.setronica.unify.application;

import org.springframework.boot.SpringApplication;

public class TestUnifyCatalogApplication {

	public static void main(String[] args) {
		SpringApplication.from(UnifyCatalogApplication::main).with(TestcontainersConfiguration.class).run(args);
	}
}
