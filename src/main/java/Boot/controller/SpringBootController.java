package Boot.controller;

import Boot.domain.User;
import Boot.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*  springBoot已整合的基本功能：
 *  mybatis
 *  springMVC视图解析器
 *  spring generator代码生成插件
 *  Redis数据数据缓存
 */


@RequestMapping("/springBoot")
@Controller
public class SpringBootController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisTemplate<String,String> redisTemplatestr;

   /*实现数据库读取Redis缓存*/
    @RequestMapping("/findAll")
    @ResponseBody
    public List<User> findAll() throws JsonProcessingException {
        List<User> all;
        String userlist = redisTemplatestr.boundValueOps("user.finaAll").get();
        if(userlist == null){    //这里有个第一次读取后，数据库变动，Redis不会改变对的bug
            all = userMapper.queryUserList();
            /*转化json*/
            ObjectMapper objectMapper = new ObjectMapper();
            userlist = objectMapper.writeValueAsString(all);
            redisTemplatestr.boundValueOps("user.finaAll").set(userlist);
            System.out.println("从数据库获取");
        }
        else{
            Gson gson = new Gson();
            /*将json封装为实例化对象数组*/
            all = gson.fromJson(userlist, new TypeToken<List<User>>() {
            }.getType());
            for(User users:all){
                System.out.println(users);
            }
            System.out.println("从redis中获取");
        }
        System.out.println(userlist);
        return all;
    }

    @RequestMapping("/findById/{id}")
    @ResponseBody
    public User findById(@PathVariable("id") int id){
           // User user = (User) redisTemplate.opsForValue().get("user");
           User user = (User) redisTemplate.opsForValue().get("user");
           if(user ==  null){
               user = userMapper.findById(id);
               redisTemplate.opsForValue().set("user",user);
               System.out.println("redis中不存user,从数据库读取");
           }
           else{
               System.out.println("redis中存在user为:"+user);
           }
           return user;
        }

     /*springMVC视图解析器*/
    @RequestMapping("/show")
    public String getIndex() {
        return "index";
    }

}
