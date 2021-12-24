package com.playtomic.tests.wallet;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@ActiveProfiles(profiles = "test")
public class WalletApplicationIT {

    @MockBean
    private ModelMapper modelMapper;

	@Test
	public void emptyTest() {
	}
}
