package backend.academy.scrapper.config;

import backend.academy.scrapper.api.InformationProvider;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ProviderConfig {

    private final Map<String, InformationProvider> providers;

    @Bean
    public Map<String, InformationProvider> informationProviders() {
        Map<String, InformationProvider> informationProviders = new HashMap<>();
        for (InformationProvider provider : providers.values()) {
            informationProviders.put(provider.getType(), provider);
        }
        return informationProviders;
    }
}
