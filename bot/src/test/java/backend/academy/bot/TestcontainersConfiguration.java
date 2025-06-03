package backend.academy.bot;

import org.springframework.boot.test.context.TestConfiguration;

// isolated from the "scrapper" module's containers!
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {}
