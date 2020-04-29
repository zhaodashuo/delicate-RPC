package zhaoshuo.rpc_server.Demo.Configure;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zhaoshuo.rpc_server.server.RpcServer;
import zhaoshuo.zk.registry.ServiceRegistry;


/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-04-23 21:05
 */
@Configuration
public class Config {
    @Value("${spring.serviceAddress}")
    String serviceAddress;
    @Value("${spring.registerAddress}")
    String registerAddress;
    @Bean
    RpcServer rpcServer(){
        return new RpcServer(serviceAddress,serviceRegistry());
    }
    @Bean
    ServiceRegistry serviceRegistry(){
        return new ServiceRegistry(registerAddress);
    }

}
