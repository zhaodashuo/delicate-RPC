package zhaoshuo.rpc_server.Demo.Configure;


import org.springframework.stereotype.Component;
import zhaoshuo.rpc_server.server.RpcService;
import zhaoshuo.testapi.HelloWorld;

@Component
@RpcService(HelloWorld.class)
public class HelloServiceImpl implements HelloWorld {

    public HelloServiceImpl(){

    }

    @Override
    public String hello(String name) {
        return "Hello?????" + name;
    }

//    @Override
//    public String hello(Person person) {
//        return "Hello! " + person.getFirstName() + " " + person.getLastName();
//    }
}
