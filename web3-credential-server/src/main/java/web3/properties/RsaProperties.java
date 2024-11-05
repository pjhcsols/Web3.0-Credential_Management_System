package web3.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "rsa")
public record RsaProperties (
        String publicKey //사용안함
){
}
