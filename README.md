# javaassit-interceptor
## 基于javaassit 实现对于方法的拦截，可以处理方法处理前后的拦截逻辑，比如修改方法参数或返回值
> 使用场景：
  比如想要拦截一些中间件，httpClient、dubbo的某些接口，做一些接口拦截前后的操作，比如修改接口的入参和返回值，修改http请求的消息header
  ，又不想业务做升级，不想掌握复杂的java assit的语法或者byte buddy的语法，则可以使用此框架简单定义需要拦截的类和方法。
  然后在interceptor中做拦截的处理。使用java agent的方式启动，即可实现拦截。具体如下：  
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
      1. 定义拦截的器
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
            b. 
