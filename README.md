# javaassit-interceptor
## 基于javaassit 实现对于方法的拦截，可以处理方法处理前后的拦截逻辑，比如修改方法参数或返回值
> 使用场景：
  比如想要拦截一些中间件，httpClient、dubbo的某些方法，做一些方法拦截前后的修改操作，比如修改方法的入参或者返回值。
  例如想要在请求http接口时新增http请求的消息header。
  ，又不想业务做升级，不想掌握复杂的java assit的语法或者byte buddy的语法，则可以使用此框架简单定义需要拦截的类和方法。使用java agent的方式启动，即可实现拦截。具体如下：  
  ---  
   1. 建立拦截的plugins模块  
     在souche-plugins模块下面新建一个新的模块。比如souche-plugins-performance,创建后将模块名字添加到souche-plugins的pom.xml文件中
     如下所示：
     
 ``` 
        <modules>
           <module>souche-plugins-performance</module>
        </modules>
 ```  
    
   2. 创建拦截逻辑    
      1. 实现Define，定义拦截的类和方法  
            比如想要拦截HttpClient的execute方法，并在拦截的对象中获取headers，新增一个新的header，则可以通过如下方式完成。  
            
        ```
              public class HttpClientRequestDefine extends AbstractInterceptorDefine {
              
              
                  private String className = "org.apache.http.impl.client.CloseableHttpClient";
                  private String methodName = "execute";
              
                  private String intercetor = "com.souche.perf.http.interceptor.HttpClientRequestInterceptor";
                  @Override
                  public String matchedClassName() {
                      return className;
                  }
              
                  @Override
                  public String methodName() {
                      return methodName;
                  }
              
                  @Override
                  public String interceptor() {
                      return intercetor;
                  }
              
                  @Override
                  public String[] methodParams() {
                      return new String[] {"org.apache.http.client.methods.HttpUriRequest"};
                  }
              
             }
        ```  
        
      2. 实现interceptor，定义拦截的处理逻辑
       
       自定义的拦截器要扩展 AgentMethodInterceptor接口，如下的拦截器实现了在执行http请求时自动添加header 的key="myheader"、value="test"的效果。
       
       ``` 
           @Slf4j
            public class HttpClientRequestInterceptor extends AgentMethodInterceptor {
            
            
                public static final HttpClientRequestInterceptor instance = new HttpClientRequestInterceptor();
            
                private  MockService mockService = MockService.getInstance();
            
                public HttpClientRequestInterceptor() {
            
                }
            
                public  static HttpClientRequestInterceptor getInstance() {
                    return instance;
                }
            
                /**
                 * 方法执行之前的拦截
                 * @param args
                 * @param instance
                 * @param clazz
                 * @param methodName
                 */
                @Override
                public BeforeMethodResult beforeMethod(Object instance, Class clazz, String methodName, Object[] args) {
                    
                        HttpUriRequest request = (HttpUriRequest)args[0];
                        request.setHeader("myheader","test");
                        return null;
                }
            
                /**
                 * 方法执行之后的拦截
                 * @param args
                 * @param instance
                 * @param clazz
                 * @param methodName
                 *
                 * @return Object 返回值
                 */
                @Override
                public Object afterMethod(Object instance,Class clazz,String methodName,Object[] args,Object result) {
            
                    return result;
                }
            
                @Override
                public void handleException(Object instance, Class clazz, String methodName, Object[] args, Throwable e) {
                    log.error("fail to execute HttpClientRequestInterceptor interceptor.",e);
                }
            }
        ```
       3. 将定义的拦截Define配置到properties文件中  
        在souche-plugins-performace模块resources目录下的servie-plugins.properties 文件中添加如下  
       
        ```
            HttpClientRequestDefine=com.souche.perf.http.define.HttpClientRequestDefine
        
        ```
        
       4. 打包
       在 javaassit-interceptor项目的目录下运行
       ```
       mvn clean install -Dmaven.test.skip 

       ```
       进行打包操作, 打包完成后会在souche-dist模块的target目录下面生成souche-agent-dist.zip文件
       
       5. 使用javaagent方式运行
           将打包后的文件souche-agent-dist.zip放入到指定位置，进行解压操作，在需要拦截的项目中启动时，添加 -javaagent=xx目录/souche-agent/agent/javaassit-agent-1.0-SNAPSHOT.jar
       然后项目进行http请求，就会自动添加header "myheader"实现拦截效果
       
       
