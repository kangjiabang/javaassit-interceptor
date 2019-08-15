# javaassit-interceptor
## 基于javaassit 实现对于 接口的 拦截，可以处理方法处理前后的拦截逻辑
 * 使用方法  
  1） 定义拦截的器  
  ```
  public class MybatisDataSourceDefine extends AbstractInterceptorDefine {
  
  
      private String className = "org.apache.ibatis.mapping.Environment";
      private String methodName = "getDataSource";
  
      private String intercetor = "com.souche.perf.db.interceptor.MybatisDataSourceInterceptor";
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
          return new String[0];
      }
  
  }
```