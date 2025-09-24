package voting.system.example.demo.security;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import voting.system.example.demo.dto.UserDto;
import voting.system.example.demo.entities.UserEntity;

@Configuration
public class AppConfig {

    @Value("${AWS_SECRET_KEY:}")
    private String awsSecretKey;

    @Value("${AWS_ACCESS_KEY:}")
    private String awsAccessKey;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

//    @Bean
//    public ModelMapper modelMapper() {
//        ModelMapper mapper = new ModelMapper();
//        mapper.typeMap(UserDto.class, UserEntity.class)
//                .addMappings(m -> m.skip(UserEntity::setId));
//        return mapper;
//    }


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public String secretKey( @Value("${token.secret:}") String key) {
        return key;
    }

    @Bean
    public AmazonSimpleEmailService awsSimpleEmailService () {
        return AmazonSimpleEmailServiceClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
                .withRegion(Regions.US_EAST_1)
                .build();
    }
}
