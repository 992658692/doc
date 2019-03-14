package spittr.config.cache;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import net.sf.ehcache.CacheManager;

@Configuration
//@EnableCaching注解启用缓存功能
//缓存其实是基于切面的功能实现的，在需要缓存的地方使用切面将其包裹
//@Cacheable:表明Spring在调用方法之前，首先应该去缓存中查找是否存在对应的缓存值，
//如果这个值存在则直接返回缓存值，如果不存在则调用对应的方法，然后再将返回值放到缓存中
//该注解也可以作用在接口对应的方法上，那么实现该接口的方法都会有缓存的功能
//@CachePut：表明Spring直接将方法返回放大缓存中，并且它不会去缓存中查询是否存在，方法始终被执行。
//@CacheEvict：表明Spring应该在缓存中清除一个或多个条目
//@Caching:这是一个分组注解，能够同时应用多个其他的缓存注解
@EnableCaching
public class CacheConfig {

//	@Bean
	//声明缓存管理器
	//由于该缓存管理器是依赖于系统内存，生命周期与应用捆绑，所以并不适合正式环境
//	public CacheManager cacheManager() {
//		return new ConcurrentMapCacheManager();
//	}
	
	@Bean
	//Spring中也定义了CacheManager
	//这边EhCache又定义一个CacheManager，2个cache并不冲突，
	//eh的cache要注入到spring的cache中
	//而EhCacheManagerFactoryBean是专门为了注入到spring的cache中诞生的！
	public EhCacheCacheManager cacheManager(CacheManager cm) {
		return new EhCacheCacheManager(cm);
	}
	
	@Bean
	public EhCacheManagerFactoryBean ehcache() {
		EhCacheManagerFactoryBean ehCacheFactoryBean = new EhCacheManagerFactoryBean();
		ehCacheFactoryBean.setConfigLocation(new ClassPathResource("ehCache.xml"));
		return ehCacheFactoryBean;
	}
	//用redis做缓存
	@Bean
	public org.springframework.cache.CacheManager cacheManager(RedisTemplate redisTemplate) {	
		return new RedisCacheManager(redisTemplate);
	}
	
	@Bean
	public JedisConnectionFactory redisConnectionFactory() {
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
		jedisConnectionFactory.afterPropertiesSet();
		return jedisConnectionFactory;
	}
	
	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisCF) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
		redisTemplate.setConnectionFactory(redisCF);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}
	
	@Bean
	public org.springframework.cache.CacheManager cacheManager(CacheManager cm, javax.cache.CacheManager jcm) {
		
		//创建多缓存管理器，每次查找缓存时便利所有管理器
		CompositeCacheManager cacheManager = new CompositeCacheManager();
		List<org.springframework.cache.CacheManager> manager = new ArrayList<org.springframework.cache.CacheManager>();
		manager.add(new JCacheCacheManager(jcm));
		manager.add(new EhCacheCacheManager(cm));
		manager.add(new RedisCacheManager(new RedisTemplate<>()));
		cacheManager.setCacheManagers(manager);
		return null;
	}
	
	@Cacheable(value = "findOne", key="#reuslt.xx")
	//findOne为缓存名 当Spring请求方法时 会通过切面拦截方法，然后优先去缓存中找对应缓存名的缓存，而缓存是以Key-value的形式保存的
	//而缓存中的key就是对应方法的入参参数名id，然后就可以找到对应的value了并直接返回
	//key属性可以将默认的缓存键值替换成设定的值
	//@Cacheable与@CachePut必须作用在返回值非void的方法上
	public String findOne (String id) {
		return id;
	}
	
	@Cacheable(unless = "#result.message.contains('xxx')",
			condition="#id >= 10")
	//unless和condition都支持SpEL表达式
	//当unless为true只是单纯的不将结果放到缓存里，但是在执行方法之前还是会去查询一次缓存是否存在
	//而condition为false则是直接将缓存禁用，该方法直接对缓存进行查找与存放
	//还有一个区别就是unless可以用#result表达式来引用方法所返回的对象参数，而condition则直接控制是否禁用缓存
	//所以这2个属性在表达式内的对象引用也是有区别的
	public String finTwo (String id) {
		return id;
	}
}
