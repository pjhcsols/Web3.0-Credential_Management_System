package web3.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "passport")
public record PassportProperties (
        String clientSecret,
        String publicKeyStr
){
}
