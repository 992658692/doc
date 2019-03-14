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
//@EnableCachingע�����û��湦��
//������ʵ�ǻ�������Ĺ���ʵ�ֵģ�����Ҫ����ĵط�ʹ�����潫�����
//@Cacheable:����Spring�ڵ��÷���֮ǰ������Ӧ��ȥ�����в����Ƿ���ڶ�Ӧ�Ļ���ֵ��
//������ֵ������ֱ�ӷ��ػ���ֵ���������������ö�Ӧ�ķ�����Ȼ���ٽ�����ֵ�ŵ�������
//��ע��Ҳ���������ڽӿڶ�Ӧ�ķ����ϣ���ôʵ�ָýӿڵķ��������л���Ĺ���
//@CachePut������Springֱ�ӽ��������طŴ󻺴��У�����������ȥ�����в�ѯ�Ƿ���ڣ�����ʼ�ձ�ִ�С�
//@CacheEvict������SpringӦ���ڻ��������һ��������Ŀ
//@Caching:����һ������ע�⣬�ܹ�ͬʱӦ�ö�������Ļ���ע��
@EnableCaching
public class CacheConfig {

//	@Bean
	//�������������
	//���ڸû����������������ϵͳ�ڴ棬����������Ӧ���������Բ����ʺ���ʽ����
//	public CacheManager cacheManager() {
//		return new ConcurrentMapCacheManager();
//	}
	
	@Bean
	//Spring��Ҳ������CacheManager
	//���EhCache�ֶ���һ��CacheManager��2��cache������ͻ��
	//eh��cacheҪע�뵽spring��cache��
	//��EhCacheManagerFactoryBean��ר��Ϊ��ע�뵽spring��cache�е����ģ�
	public EhCacheCacheManager cacheManager(CacheManager cm) {
		return new EhCacheCacheManager(cm);
	}
	
	@Bean
	public EhCacheManagerFactoryBean ehcache() {
		EhCacheManagerFactoryBean ehCacheFactoryBean = new EhCacheManagerFactoryBean();
		ehCacheFactoryBean.setConfigLocation(new ClassPathResource("ehCache.xml"));
		return ehCacheFactoryBean;
	}
	//��redis������
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
		
		//�����໺���������ÿ�β��һ���ʱ�������й�����
		CompositeCacheManager cacheManager = new CompositeCacheManager();
		List<org.springframework.cache.CacheManager> manager = new ArrayList<org.springframework.cache.CacheManager>();
		manager.add(new JCacheCacheManager(jcm));
		manager.add(new EhCacheCacheManager(cm));
		manager.add(new RedisCacheManager(new RedisTemplate<>()));
		cacheManager.setCacheManagers(manager);
		return null;
	}
	
	@Cacheable(value = "findOne", key="#reuslt.xx")
	//findOneΪ������ ��Spring���󷽷�ʱ ��ͨ���������ط�����Ȼ������ȥ�������Ҷ�Ӧ�������Ļ��棬����������Key-value����ʽ�����
	//�������е�key���Ƕ�Ӧ��������β�����id��Ȼ��Ϳ����ҵ���Ӧ��value�˲�ֱ�ӷ���
	//key���Կ��Խ�Ĭ�ϵĻ����ֵ�滻���趨��ֵ
	//@Cacheable��@CachePut���������ڷ���ֵ��void�ķ�����
	public String findOne (String id) {
		return id;
	}
	
	@Cacheable(unless = "#result.message.contains('xxx')",
			condition="#id >= 10")
	//unless��condition��֧��SpEL���ʽ
	//��unlessΪtrueֻ�ǵ����Ĳ�������ŵ������������ִ�з���֮ǰ���ǻ�ȥ��ѯһ�λ����Ƿ����
	//��conditionΪfalse����ֱ�ӽ�������ã��÷���ֱ�ӶԻ�����в�������
	//����һ���������unless������#result���ʽ�����÷��������صĶ����������condition��ֱ�ӿ����Ƿ���û���
	//������2�������ڱ��ʽ�ڵĶ�������Ҳ���������
	public String finTwo (String id) {
		return id;
	}
}
