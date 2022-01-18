package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean
	Hibernate5Module hibernate5Module() {
		Hibernate5Module hibernate5Module = new Hibernate5Module();
		// 강제로 로딩
//		hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true); 쓰면 안됨. 결론적으로 Entity 노출 X
		return hibernate5Module;
	}
	// 이런 방법이 있다 정도만, Entity를 직접 노출할 일이 없기 때문에.

}
