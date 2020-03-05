/*
 * Copyright 2017-Present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package greeter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerExchangeFilterFunction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@RestController
public class GreeterController {

    @Autowired
	private  GreeterService greeter;

	//https://cloud.spring.io/spring-cloud-commons/reference/html/
    @Autowired
    //private ReactorLoadBalancerExchangeFilterFunction lbFunction;==> error
    private LoadBalancerExchangeFilterFunction lbFunction;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello(@RequestParam(value = "salutation", defaultValue = "Hello") String salutation,
						@RequestParam(value = "name", defaultValue = "Bob") String name) {
		Greeting greeting = greeter.greet(salutation, name);
		return greeting.getMessage();
	}



	@RequestMapping("/hello-webclient")
	public Mono<String> helloWebclient(@RequestParam(value = "salutation", defaultValue = "Hello") String salutation,
									   @RequestParam(value = "name", defaultValue = "Bob") String name) {

		StringBuffer uri= new StringBuffer();
		uri.append("/greeting?salutation=").append(salutation).append("&name=").append(name);

		return WebClient.builder().baseUrl("https://greeter-messages")
				.filter(lbFunction)
				.build().get()
				.uri(uri.toString())
				.retrieve().bodyToMono(String.class);
	}

}
