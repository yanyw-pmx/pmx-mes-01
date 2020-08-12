package Boot;


import Boot.domain.User;
import Boot.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes =  MySpringBootApplication.class)
public class JunitTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Test
    public void test(){
        List<User> users = userMapper.queryUserList();
        System.out.println( users);
    }

    @Test
    public void Redistest() throws JsonProcessingException {
        String userlist = redisTemplate.boundValueOps("user.finaAll").get();
        if(userlist == null){    //这里有个第一次读取后，数据库变动，Redis不会改变对的bug
            List<User> all = userMapper.queryUserList();
            /*转化json*/
            ObjectMapper objectMapper = new ObjectMapper();
            userlist = objectMapper.writeValueAsString(all);
            redisTemplate.boundValueOps("user.finaAll").set(userlist);
            System.out.println("从数据库获取");
        }
        else{
            Gson gson = new Gson();
            /*将json封装为实例化对象数组*/
            List<User> redisuser = gson.fromJson(userlist, new TypeToken<List<User>>() {
            }.getType());
            for(User users:redisuser){
                System.out.println(users);
            }
            System.out.println("从redis中获取");
        }
        System.out.println(userlist);
     }
}


