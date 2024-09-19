package com.tweety.SwithT.configs;

import com.siot.IamportRestClient.IamportClient;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class IamportApiProperty {
    private IamportClient iamportClient;

    @Value("${imp.key}")
    private String impkey;

    @Value("${imp.secret}")
    private String impSecret;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(impkey, impSecret);
    }
}
