package com.librarysystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryManagementSystemApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
    void applicationStarts() {
        // Test to ensure the application starts without exceptions
        LibraryManagementSystemApplication.main(new String[] {});
    }

}
